package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.data.local.entity.SyncMappingEntity
import com.carenote.app.data.mapper.MedicationLogMapper
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.mapper.remote.MedicationLogRemoteMapper
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.model.MedicationLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy as DaggerLazy

/**
 * MedicationLog エンティティの同期処理を担当
 *
 * 特殊ケース: medications/{medicationId}/logs サブコレクションとして同期
 */
@Singleton
class MedicationLogSyncer @Inject constructor(
    firestore: DaggerLazy<FirebaseFirestore>,
    syncMappingDao: SyncMappingDao,
    timestampConverter: FirestoreTimestampConverter,
    private val medicationLogDao: MedicationLogDao,
    private val entityMapper: MedicationLogMapper,
    private val remoteMapper: MedicationLogRemoteMapper
) : EntitySyncer<MedicationLogEntity, MedicationLog>(firestore, syncMappingDao, timestampConverter) {

    override val entityType: String = "medicationLog"

    /**
     * MedicationLog は medications/{id}/logs サブコレクション構造のため、
     * 単一引数の collectionPath は使用不可。
     * 代わりに [collectionPath(careRecipientId, medicationRemoteId)] を使用すること。
     */
    override fun collectionPath(careRecipientId: String): String {
        throw UnsupportedOperationException("Use collectionPath(careRecipientId, medicationRemoteId)")
    }

    /**
     * 服薬 ID を指定してコレクションパスを生成
     */
    fun collectionPath(careRecipientId: String, medicationRemoteId: String): String =
        "careRecipients/$careRecipientId/medications/$medicationRemoteId/logs"

    /**
     * MedicationLog は服薬 ID ごとに取得する必要があるため、
     * 引数なしの getAllLocal は使用不可。
     * 代わりに [getAllLocalForMedication] を使用すること。
     */
    override suspend fun getAllLocal(): List<MedicationLogEntity> {
        throw UnsupportedOperationException("Use getAllLocalForMedication(medicationId)")
    }

    /**
     * 特定の服薬の全ログを取得
     */
    suspend fun getAllLocalForMedication(medicationId: Long): List<MedicationLogEntity> =
        medicationLogDao.getLogsForMedication(medicationId).first()

    override suspend fun getLocalById(id: Long): MedicationLogEntity? = null

    override suspend fun saveLocal(entity: MedicationLogEntity): Long =
        medicationLogDao.insertLog(entity)

    override suspend fun deleteLocal(id: Long) =
        medicationLogDao.deleteLog(id)

    override fun entityToDomain(entity: MedicationLogEntity): MedicationLog =
        entityMapper.toDomain(entity)

    override fun domainToEntity(domain: MedicationLog): MedicationLogEntity =
        entityMapper.toEntity(domain)

    override fun domainToRemote(domain: MedicationLog, syncMetadata: SyncMetadata): Map<String, Any?> =
        remoteMapper.toRemote(domain, syncMetadata)

    override fun remoteToDomain(data: Map<String, Any?>): MedicationLog =
        remoteMapper.toDomain(data)

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata =
        remoteMapper.extractSyncMetadata(data)

    override fun getLocalId(entity: MedicationLogEntity): Long = entity.id

    override fun getUpdatedAt(entity: MedicationLogEntity): LocalDateTime =
        LocalDateTime.parse(entity.recordedAt)

    /**
     * 特定の服薬のログを同期
     *
     * @param careRecipientId 被介護者 ID
     * @param medicationLocalId Room の服薬 ID
     * @param medicationRemoteId Firestore の服薬ドキュメント ID
     * @param lastSyncTime 前回同期日時
     * @return 同期結果
     */
    suspend fun syncForMedication(
        careRecipientId: String,
        medicationLocalId: Long,
        medicationRemoteId: String,
        lastSyncTime: LocalDateTime?
    ): SyncResult {
        return try {
            val now = LocalDateTime.now()
            val pushResult = pushLogsForMedication(
                careRecipientId,
                medicationLocalId,
                medicationRemoteId,
                lastSyncTime,
                now
            )
            val pullResult = pullLogsForMedication(
                careRecipientId,
                medicationRemoteId,
                medicationLocalId,
                lastSyncTime,
                now
            )
            mergeResults(pushResult, pullResult)
        } catch (e: Exception) {
            Timber.e(e, "Sync failed for medicationLog medicationId=$medicationLocalId")
            SyncResult.Failure(mapException(e))
        }
    }

    private suspend fun pushLogsForMedication(
        careRecipientId: String,
        medicationLocalId: Long,
        medicationRemoteId: String,
        lastSyncTime: LocalDateTime?,
        syncTime: LocalDateTime
    ): SyncResult {
        val logs = getAllLocalForMedication(medicationLocalId)
        val mappings = syncMappingDao.getAllByTypeIncludingDeleted(entityType)
        val mappingsByLocalId = mappings.associateBy { it.localId }

        var uploadedCount = 0
        val failedEntities = mutableListOf<Long>()
        val errors = mutableListOf<DomainError>()

        for (log in logs) {
            val localId = getLocalId(log)
            val updatedAt = getUpdatedAt(log)
            val existingMapping = mappingsByLocalId[localId]

            val needsUpload = existingMapping == null ||
                (lastSyncTime != null && updatedAt.isAfter(lastSyncTime))

            if (needsUpload) {
                try {
                    uploadLog(careRecipientId, medicationRemoteId, log, existingMapping, syncTime)
                    uploadedCount++
                } catch (e: Exception) {
                    Timber.w("Failed to upload medicationLog localId=$localId: $e")
                    failedEntities.add(localId)
                    errors.add(mapException(e))
                }
            }
        }

        return if (failedEntities.isEmpty()) {
            SyncResult.Success(uploadedCount = uploadedCount, downloadedCount = 0)
        } else {
            SyncResult.PartialSuccess(uploadedCount, failedEntities, errors)
        }
    }

    private suspend fun uploadLog(
        careRecipientId: String,
        medicationRemoteId: String,
        entity: MedicationLogEntity,
        existingMapping: SyncMappingEntity?,
        syncTime: LocalDateTime
    ) {
        val localId = getLocalId(entity)
        val domain = entityToDomain(entity)

        val remoteId = existingMapping?.remoteId ?: UUID.randomUUID().toString()
        val syncMetadata = SyncMetadata(localId = localId, syncedAt = syncTime, deletedAt = null)
        val remoteData = domainToRemote(domain, syncMetadata)

        val path = collectionPath(careRecipientId, medicationRemoteId)
        val docRef = firestore.get().collection(path).document(remoteId)
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

        Timber.d("Uploaded medicationLog localId=$localId remoteId=$remoteId")
    }

    private suspend fun pullLogsForMedication(
        careRecipientId: String,
        medicationRemoteId: String,
        medicationLocalId: Long,
        lastSyncTime: LocalDateTime?,
        syncTime: LocalDateTime
    ): SyncResult {
        val path = collectionPath(careRecipientId, medicationRemoteId)
        val query = firestore.get().collection(path).whereEqualTo("deletedAt", null)
        val querySnapshot = query.get().await()

        var downloadedCount = 0
        val failedEntities = mutableListOf<Long>()
        val errors = mutableListOf<DomainError>()

        for (doc in querySnapshot.documents) {
            try {
                val data = doc.data ?: continue
                val existingMapping = syncMappingDao.getByRemoteId(entityType, doc.id)

                if (existingMapping == null) {
                    val domain = remoteToDomain(data)
                    val entityWithCorrectMedicationId = entityMapper.toEntity(
                        domain.copy(medicationId = medicationLocalId)
                    )
                    val newLocalId = saveLocal(entityWithCorrectMedicationId)

                    val mapping = SyncMappingEntity(
                        entityType = entityType,
                        localId = newLocalId,
                        remoteId = doc.id,
                        lastSyncedAt = syncTime.toString()
                    )
                    syncMappingDao.upsert(mapping)
                    downloadedCount++
                }
            } catch (e: Exception) {
                val localId = (doc.data?.get("localId") as? Number)?.toLong() ?: -1
                Timber.w("Failed to process remote medicationLog: $e")
                failedEntities.add(localId)
                errors.add(mapException(e))
            }
        }

        return if (failedEntities.isEmpty()) {
            SyncResult.Success(uploadedCount = 0, downloadedCount = downloadedCount)
        } else {
            SyncResult.PartialSuccess(downloadedCount, failedEntities, errors)
        }
    }

    private fun mergeResults(push: SyncResult, pull: SyncResult): SyncResult {
        return when {
            push is SyncResult.Failure -> push
            pull is SyncResult.Failure -> pull
            push is SyncResult.Success && pull is SyncResult.Success -> {
                SyncResult.Success(
                    uploadedCount = push.uploadedCount,
                    downloadedCount = pull.downloadedCount
                )
            }
            else -> {
                val totalSuccess = (push as? SyncResult.Success)?.uploadedCount
                    ?: (push as? SyncResult.PartialSuccess)?.successCount
                    ?: 0
                val pullSuccess = (pull as? SyncResult.Success)?.downloadedCount
                    ?: (pull as? SyncResult.PartialSuccess)?.successCount
                    ?: 0

                val allFailed = mutableListOf<Long>()
                val allErrors = mutableListOf<DomainError>()

                (push as? SyncResult.PartialSuccess)?.let {
                    allFailed.addAll(it.failedEntities)
                    allErrors.addAll(it.errors)
                }
                (pull as? SyncResult.PartialSuccess)?.let {
                    allFailed.addAll(it.failedEntities)
                    allErrors.addAll(it.errors)
                }

                SyncResult.PartialSuccess(totalSuccess + pullSuccess, allFailed, allErrors)
            }
        }
    }
}
