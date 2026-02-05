package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.common.SyncResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * EntitySyncer のテスト用具象クラス
 *
 * シンプルな TestEntity / TestDomain を使用して
 * EntitySyncer の抽象メソッドを最小実装でテストする。
 */

/**
 * テスト用ローカルエンティティ
 */
data class TestEntity(
    val id: Long,
    val name: String,
    val updatedAt: LocalDateTime
)

/**
 * テスト用ドメインモデル
 */
data class TestDomain(
    val id: Long,
    val name: String,
    val updatedAt: LocalDateTime
)

/**
 * EntitySyncer のテスト用具象実装
 *
 * インメモリでローカルストレージを模擬し、
 * Firestore 呼び出しはモック経由で制御する。
 */
class TestEntitySyncer(
    firestore: FirebaseFirestore,
    syncMappingDao: SyncMappingDao,
    timestampConverter: FirestoreTimestampConverter
) : EntitySyncer<TestEntity, TestDomain>(firestore, syncMappingDao, timestampConverter) {

    override val entityType: String = "test_entity"

    /** インメモリローカルストレージ */
    private val localStorage = mutableMapOf<Long, TestEntity>()

    /** 自動採番 ID */
    private var nextId = 1L

    /** pushLocalChanges の結果をオーバーライド可能 */
    var overridePushResult: SyncResult? = null

    /** pullRemoteChanges の結果をオーバーライド可能 */
    var overridePullResult: SyncResult? = null

    /** push 時に例外をスロー */
    var throwOnPush: Exception? = null

    /** pull 時に例外をスロー */
    var throwOnPull: Exception? = null

    /**
     * true の場合、pushLocalChanges で Firestore を呼ばずにシミュレート実行する。
     * 実際のアップロードロジック（needsUpload 判定、マッピング作成）をテスト可能。
     */
    var simulatePushWithoutFirestore = false

    /** uploadEntity が呼ばれた回数 */
    var uploadCount = 0
        private set

    /** downloadEntity が呼ばれた回数 */
    var downloadCount = 0
        private set

    /** 全状態をリセット */
    fun clear() {
        localStorage.clear()
        nextId = 1L
        overridePushResult = null
        overridePullResult = null
        throwOnPush = null
        throwOnPull = null
        simulatePushWithoutFirestore = false
        uploadCount = 0
        downloadCount = 0
    }

    /** ローカルエンティティを追加 */
    fun addLocalEntity(entity: TestEntity) {
        localStorage[entity.id] = entity
        if (entity.id >= nextId) {
            nextId = entity.id + 1
        }
    }

    /** ローカルエンティティを取得（検証用） */
    fun getLocalEntities(): List<TestEntity> = localStorage.values.toList()

    // ========== EntitySyncer 抽象メソッドの実装 ==========

    override fun collectionPath(careRecipientId: String): String {
        return "careRecipients/$careRecipientId/test_entities"
    }

    override suspend fun getAllLocal(): List<TestEntity> {
        return localStorage.values.toList()
    }

    override suspend fun getLocalById(id: Long): TestEntity? {
        return localStorage[id]
    }

    override suspend fun saveLocal(entity: TestEntity): Long {
        downloadCount++
        val id = if (entity.id == 0L) nextId++ else entity.id
        localStorage[id] = entity.copy(id = id)
        return id
    }

    override suspend fun deleteLocal(id: Long) {
        localStorage.remove(id)
    }

    override fun entityToDomain(entity: TestEntity): TestDomain {
        return TestDomain(
            id = entity.id,
            name = entity.name,
            updatedAt = entity.updatedAt
        )
    }

    override fun domainToEntity(domain: TestDomain): TestEntity {
        return TestEntity(
            id = domain.id,
            name = domain.name,
            updatedAt = domain.updatedAt
        )
    }

    override fun domainToRemote(domain: TestDomain, syncMetadata: SyncMetadata): Map<String, Any?> {
        uploadCount++
        val instant = domain.updatedAt.atZone(ZoneId.systemDefault()).toInstant()
        return mapOf(
            "name" to domain.name,
            "updatedAt" to Timestamp(instant.epochSecond, instant.nano),
            "localId" to syncMetadata.localId,
            "syncedAt" to timestampConverter.toTimestamp(syncMetadata.syncedAt),
            "deletedAt" to syncMetadata.deletedAt?.let { timestampConverter.toTimestamp(it) }
        )
    }

    override fun remoteToDomain(data: Map<String, Any?>): TestDomain {
        val id = (data["localId"] as? Number)?.toLong() ?: 0L
        val name = data["name"] as? String ?: ""
        val updatedAt = timestampConverter.toLocalDateTimeFromAny(data["updatedAt"])
        return TestDomain(
            id = id,
            name = name,
            updatedAt = updatedAt
        )
    }

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata {
        val localId = (data["localId"] as? Number)?.toLong() ?: 0L
        val syncedAt = timestampConverter.toLocalDateTimeFromAny(data["syncedAt"])
        val deletedAt = timestampConverter.toLocalDateTimeFromAnyOrNull(data["deletedAt"])
        return SyncMetadata(
            localId = localId,
            syncedAt = syncedAt,
            deletedAt = deletedAt
        )
    }

    override fun getLocalId(entity: TestEntity): Long = entity.id

    override fun getUpdatedAt(entity: TestEntity): LocalDateTime = entity.updatedAt

    // ========== オーバーライド可能なテスト用メソッド ==========

    override suspend fun pushLocalChanges(
        careRecipientId: String,
        lastSyncTime: LocalDateTime?,
        syncTime: LocalDateTime
    ): SyncResult {
        throwOnPush?.let { throw it }
        overridePushResult?.let { return it }

        // Firestore を呼ばずにシミュレート実行
        if (simulatePushWithoutFirestore) {
            return simulatePush(lastSyncTime, syncTime)
        }

        return super.pushLocalChanges(careRecipientId, lastSyncTime, syncTime)
    }

    /**
     * Firestore を呼ばずに pushLocalChanges のロジックをシミュレート
     */
    private suspend fun simulatePush(
        lastSyncTime: LocalDateTime?,
        syncTime: LocalDateTime
    ): SyncResult {
        val localEntities = getAllLocal()
        val mappings = syncMappingDao.getAllByTypeIncludingDeleted(entityType)
        val mappingsByLocalId = mappings.associateBy { it.localId }

        var uploadedCount = 0

        for (entity in localEntities) {
            val localId = getLocalId(entity)
            val updatedAt = getUpdatedAt(entity)
            val existingMapping = mappingsByLocalId[localId]

            val needsUpload = existingMapping == null ||
                (lastSyncTime != null && updatedAt.isAfter(lastSyncTime))

            if (needsUpload) {
                // domainToRemote を呼んで uploadCount をインクリメント（Firestore は呼ばない）
                val domain = entityToDomain(entity)
                domainToRemote(domain, SyncMetadata(localId, syncTime, null))

                // マッピングをシミュレート作成
                val remoteId = existingMapping?.remoteId ?: "simulated-remote-$localId"
                val mapping = com.carenote.app.data.local.entity.SyncMappingEntity(
                    id = existingMapping?.id ?: 0,
                    entityType = entityType,
                    localId = localId,
                    remoteId = remoteId,
                    lastSyncedAt = syncTime.toString(),
                    isDeleted = false
                )
                syncMappingDao.upsert(mapping)
                uploadedCount++
            }
        }

        return SyncResult.Success(uploadedCount = uploadedCount, downloadedCount = 0)
    }

    override suspend fun pullRemoteChanges(
        careRecipientId: String,
        lastSyncTime: LocalDateTime?,
        syncTime: LocalDateTime
    ): SyncResult {
        throwOnPull?.let { throw it }
        overridePullResult?.let { return it }
        return super.pullRemoteChanges(careRecipientId, lastSyncTime, syncTime)
    }
}
