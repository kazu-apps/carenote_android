package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.entity.SyncMappingEntity
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import com.carenote.app.data.util.ExceptionMasker
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID
import dagger.Lazy as DaggerLazy

/**
 * エンティティタイプ別の同期ロジックを抽象化した基底クラス
 *
 * @param Entity Room Entity 型
 * @param Domain Domain Model 型
 */
abstract class EntitySyncer<Entity, Domain>(
    protected val firestore: DaggerLazy<FirebaseFirestore>,
    protected val syncMappingDao: SyncMappingDao,
    protected val timestampConverter: FirestoreTimestampConverter
) {
    /** エンティティタイプ識別子 */
    abstract val entityType: String

    /** Firestore コレクションパスを生成 */
    abstract fun collectionPath(careRecipientId: String): String

    // ======== ローカルデータアクセス（サブクラスで実装） ========

    /** 全ローカルエンティティを取得 */
    abstract suspend fun getAllLocal(): List<Entity>

    /** ID でローカルエンティティを取得 */
    abstract suspend fun getLocalById(id: Long): Entity?

    /** ローカルエンティティを保存（挿入または更新） */
    abstract suspend fun saveLocal(entity: Entity): Long

    /** ローカルエンティティを削除 */
    abstract suspend fun deleteLocal(id: Long)

    // ======== マッピング（サブクラスで実装） ========

    /** Entity → Domain 変換 */
    abstract fun entityToDomain(entity: Entity): Domain

    /** Domain → Entity 変換 */
    abstract fun domainToEntity(domain: Domain): Entity

    /** Domain → Firestore Map 変換 */
    abstract fun domainToRemote(domain: Domain, syncMetadata: SyncMetadata): Map<String, Any?>

    /** Firestore Map → Domain 変換 */
    abstract fun remoteToDomain(data: Map<String, Any?>): Domain

    /** SyncMetadata を Firestore Map から抽出 */
    abstract fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata

    /** Entity から localId を取得 */
    abstract fun getLocalId(entity: Entity): Long

    /** Entity から updatedAt を取得 */
    abstract fun getUpdatedAt(entity: Entity): LocalDateTime

    /** lastSyncTime 以降に変更されたエンティティのみ取得（未実装なら null） */
    open suspend fun getModifiedSince(lastSyncTime: LocalDateTime): List<Entity>? = null

    /**
     * 双方向同期を実行
     *
     * @param careRecipientId 被介護者 ID
     * @param lastSyncTime 前回同期日時（null の場合は初回同期）
     * @return 同期結果
     */
    suspend fun sync(careRecipientId: String, lastSyncTime: LocalDateTime?): SyncResult {
        return try {
            val now = LocalDateTime.now()
            val pushResult = pushLocalChanges(careRecipientId, lastSyncTime, now)
            val pullResult = pullRemoteChanges(careRecipientId, lastSyncTime, now)

            mergeResults(pushResult, pullResult)
        } catch (e: Exception) {
            Timber.e("Sync failed for $entityType: ${ExceptionMasker.mask(e)}")
            SyncResult.Failure(mapException(e))
        }
    }

    /**
     * ローカル変更をリモートにプッシュ
     */
    protected open suspend fun pushLocalChanges(
        careRecipientId: String,
        lastSyncTime: LocalDateTime?,
        syncTime: LocalDateTime
    ): SyncResult {
        val localEntities = if (lastSyncTime != null) {
            getModifiedSince(lastSyncTime) ?: getAllLocal()
        } else {
            getAllLocal()
        }
        val mappings = syncMappingDao.getAllByTypeIncludingDeleted(entityType)
        val mappingsByLocalId = mappings.associateBy { it.localId }

        var uploadedCount = 0
        val failedEntities = mutableListOf<Long>()
        val errors = mutableListOf<DomainError>()

        for (entity in localEntities) {
            val localId = getLocalId(entity)
            val updatedAt = getUpdatedAt(entity)
            val existingMapping = mappingsByLocalId[localId]

            val needsUpload = existingMapping == null ||
                (lastSyncTime != null && updatedAt.isAfter(lastSyncTime))

            if (needsUpload) {
                try {
                    uploadEntity(careRecipientId, entity, existingMapping, syncTime)
                    uploadedCount++
                } catch (e: Exception) {
                    Timber.w("Failed to upload $entityType: ${ExceptionMasker.mask(e)}")
                    failedEntities.add(localId)
                    errors.add(mapException(e))
                }
            }
        }

        return if (failedEntities.isEmpty()) {
            SyncResult.Success(uploadedCount = uploadedCount, downloadedCount = 0)
        } else {
            SyncResult.PartialSuccess(
                successCount = uploadedCount,
                failedEntities = failedEntities,
                errors = errors
            )
        }
    }

    /**
     * リモート変更をローカルにプル
     */
    protected open suspend fun pullRemoteChanges(
        careRecipientId: String,
        lastSyncTime: LocalDateTime?,
        syncTime: LocalDateTime
    ): SyncResult {
        val collectionRef = firestore.get().collection(collectionPath(careRecipientId))
        var query = collectionRef.whereEqualTo("deletedAt", null)
        if (lastSyncTime != null) {
            query = query.whereGreaterThan("updatedAt", lastSyncTime.toString())
        }

        val querySnapshot = query.get().await()
        val remoteDocs = querySnapshot.documents

        var downloadedCount = 0
        var conflictCount = 0
        val failedEntities = mutableListOf<Long>()
        val errors = mutableListOf<DomainError>()

        for (doc in remoteDocs) {
            try {
                val result = processRemoteDocument(doc, lastSyncTime, syncTime)
                if (result.downloaded) downloadedCount++
                if (result.conflict) conflictCount++
            } catch (e: Exception) {
                val localId = (doc.data?.get("localId") as? Number)?.toLong() ?: -1
                Timber.w("Failed to process remote $entityType: ${ExceptionMasker.mask(e)}")
                failedEntities.add(localId)
                errors.add(mapException(e))
            }
        }

        return if (failedEntities.isEmpty()) {
            SyncResult.Success(
                uploadedCount = 0,
                downloadedCount = downloadedCount,
                conflictCount = conflictCount
            )
        } else {
            SyncResult.PartialSuccess(
                successCount = downloadedCount,
                failedEntities = failedEntities,
                errors = errors
            )
        }
    }

    /**
     * エンティティを Firestore にアップロード
     */
    private suspend fun uploadEntity(
        careRecipientId: String,
        entity: Entity,
        existingMapping: SyncMappingEntity?,
        syncTime: LocalDateTime
    ) {
        val localId = getLocalId(entity)
        val domain = entityToDomain(entity)

        val remoteId = existingMapping?.remoteId ?: UUID.randomUUID().toString()
        val syncMetadata = SyncMetadata(
            localId = localId,
            syncedAt = syncTime,
            deletedAt = null
        )
        val remoteData = domainToRemote(domain, syncMetadata)

        val docRef = firestore.get().collection(collectionPath(careRecipientId)).document(remoteId)
        docRef.set(remoteData, SetOptions.merge()).await()

        val mapping = SyncMappingEntity(
            id = existingMapping?.id ?: 0,
            entityType = entityType,
            localId = localId,
            remoteId = remoteId,
            lastSyncedAt = syncTime.toString(),
            isDeleted = false
        )
        syncMappingDao.upsert(mapping)

        Timber.d("Uploaded $entityType successfully")
    }

    /**
     * リモートドキュメントを処理
     */
    private suspend fun processRemoteDocument(
        doc: DocumentSnapshot,
        lastSyncTime: LocalDateTime?,
        syncTime: LocalDateTime
    ): ProcessResult {
        val data = doc.data ?: return ProcessResult(false, false)
        val syncMetadata = extractSyncMetadata(data)

        val existingMapping = syncMappingDao.getByRemoteId(entityType, doc.id)

        val localEntity = existingMapping?.let { getLocalById(it.localId) }

        if (localEntity != null) {
            val localUpdatedAt = getUpdatedAt(localEntity)
            val domain = remoteToDomain(data)
            val remoteUpdatedAt = getRemoteUpdatedAt(data)

            if (remoteUpdatedAt.isAfter(localUpdatedAt)) {
                val newEntity = domainToEntity(domain)
                saveLocal(newEntity)
                updateMapping(existingMapping, syncTime)
                return ProcessResult(downloaded = true, conflict = true)
            }
            return ProcessResult(downloaded = false, conflict = false)
        }

        if (existingMapping == null) {
            val domain = remoteToDomain(data)
            val newEntity = domainToEntity(domain)
            val newLocalId = saveLocal(newEntity)

            val mapping = SyncMappingEntity(
                entityType = entityType,
                localId = newLocalId,
                remoteId = doc.id,
                lastSyncedAt = syncTime.toString()
            )
            syncMappingDao.upsert(mapping)
            return ProcessResult(downloaded = true, conflict = false)
        }

        return ProcessResult(downloaded = false, conflict = false)
    }

    /**
     * Firestore Map から updatedAt を取得
     */
    protected open fun getRemoteUpdatedAt(data: Map<String, Any?>): LocalDateTime {
        val updatedAt = data["updatedAt"]
            ?: throw IllegalArgumentException("updatedAt is required")
        return timestampConverter.toLocalDateTimeFromAny(updatedAt)
    }

    private suspend fun updateMapping(mapping: SyncMappingEntity, syncTime: LocalDateTime) {
        syncMappingDao.upsert(
            mapping.copy(lastSyncedAt = syncTime.toString())
        )
    }

    /**
     * 例外を DomainError に変換
     */
    protected fun mapException(e: Exception): DomainError {
        return when (e) {
            is com.google.firebase.firestore.FirebaseFirestoreException -> {
                DomainError.NetworkError(
                    message = "Firestore 同期エラー: ${ExceptionMasker.mask(e)}",
                    cause = e
                )
            }
            is IllegalArgumentException -> {
                DomainError.ValidationError(
                    message = "データ形式エラー: ${ExceptionMasker.mask(e)}"
                )
            }
            else -> {
                DomainError.UnknownError(
                    message = "同期エラー: ${ExceptionMasker.mask(e)}",
                    cause = e
                )
            }
        }
    }

    /**
     * プッシュとプルの結果をマージ
     */
    private fun mergeResults(push: SyncResult, pull: SyncResult): SyncResult {
        return when {
            push is SyncResult.Failure -> push
            pull is SyncResult.Failure -> pull
            push is SyncResult.Success && pull is SyncResult.Success -> {
                SyncResult.Success(
                    uploadedCount = push.uploadedCount,
                    downloadedCount = pull.downloadedCount,
                    conflictCount = pull.conflictCount
                )
            }
            else -> {
                val totalSuccess = when (push) {
                    is SyncResult.Success -> push.uploadedCount
                    is SyncResult.PartialSuccess -> push.successCount
                    else -> 0
                } + when (pull) {
                    is SyncResult.Success -> pull.downloadedCount
                    is SyncResult.PartialSuccess -> pull.successCount
                    else -> 0
                }

                val allFailed = mutableListOf<Long>()
                val allErrors = mutableListOf<DomainError>()

                if (push is SyncResult.PartialSuccess) {
                    allFailed.addAll(push.failedEntities)
                    allErrors.addAll(push.errors)
                }
                if (pull is SyncResult.PartialSuccess) {
                    allFailed.addAll(pull.failedEntities)
                    allErrors.addAll(pull.errors)
                }

                SyncResult.PartialSuccess(
                    successCount = totalSuccess,
                    failedEntities = allFailed,
                    errors = allErrors
                )
            }
        }
    }

    private data class ProcessResult(
        val downloaded: Boolean,
        val conflict: Boolean
    )
}
