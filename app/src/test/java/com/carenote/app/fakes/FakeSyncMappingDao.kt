package com.carenote.app.fakes

import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.entity.SyncMappingEntity

/**
 * SyncMappingDao のテスト用 Fake 実装
 *
 * インメモリでマッピングを管理。EntitySyncer のテストで使用。
 */
class FakeSyncMappingDao : SyncMappingDao {

    private val mappings = mutableListOf<SyncMappingEntity>()
    private var autoIncrementId = 1L

    /** 格納されているマッピング数 */
    val size: Int
        get() = mappings.size

    /** 全マッピングを取得（テスト検証用） */
    fun all(): List<SyncMappingEntity> = mappings.toList()

    /** 全状態をリセット */
    fun clear() {
        mappings.clear()
        autoIncrementId = 1L
    }

    override suspend fun getByLocalId(entityType: String, localId: Long): SyncMappingEntity? {
        // Note: Production DAO does not filter by isDeleted
        return mappings.find { it.entityType == entityType && it.localId == localId }
    }

    override suspend fun getByRemoteId(entityType: String, remoteId: String): SyncMappingEntity? {
        // Note: Production DAO does not filter by isDeleted
        return mappings.find { it.entityType == entityType && it.remoteId == remoteId }
    }

    override suspend fun getAllByType(entityType: String): List<SyncMappingEntity> {
        return mappings.filter { it.entityType == entityType && !it.isDeleted }
    }

    override suspend fun getAllByTypeIncludingDeleted(entityType: String): List<SyncMappingEntity> {
        return mappings.filter { it.entityType == entityType }
    }

    override suspend fun upsert(mapping: SyncMappingEntity): Long {
        val existing = if (mapping.id != 0L) {
            mappings.find { it.id == mapping.id }
        } else {
            mappings.find {
                it.entityType == mapping.entityType && it.localId == mapping.localId
            }
        }

        return if (existing != null) {
            val index = mappings.indexOf(existing)
            mappings[index] = mapping.copy(id = existing.id)
            existing.id
        } else {
            val newId = autoIncrementId++
            mappings.add(mapping.copy(id = newId))
            newId
        }
    }

    override suspend fun upsertAll(mappings: List<SyncMappingEntity>): List<Long> {
        return mappings.map { upsert(it) }
    }

    override suspend fun markDeleted(entityType: String, localId: Long, syncedAt: String) {
        val existing = mappings.find { it.entityType == entityType && it.localId == localId }
        if (existing != null) {
            val index = mappings.indexOf(existing)
            mappings[index] = existing.copy(isDeleted = true, lastSyncedAt = syncedAt)
        }
    }

    override suspend fun purgeDeleted(entityType: String) {
        mappings.removeAll { it.entityType == entityType && it.isDeleted }
    }

    override suspend fun deleteAllByType(entityType: String) {
        mappings.removeAll { it.entityType == entityType }
    }
}
