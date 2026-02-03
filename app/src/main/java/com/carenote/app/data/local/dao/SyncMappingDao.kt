package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.carenote.app.data.local.entity.SyncMappingEntity

/**
 * 同期マッピングテーブルへのデータアクセスオブジェクト
 *
 * Room ID と Firestore Document ID のマッピング管理に使用。
 */
@Dao
interface SyncMappingDao {

    /**
     * エンティティタイプとローカル ID でマッピングを取得
     *
     * @param entityType エンティティタイプ
     * @param localId Room のローカル ID
     * @return マッピングエンティティ、存在しない場合は null
     */
    @Query("SELECT * FROM sync_mappings WHERE entity_type = :entityType AND local_id = :localId LIMIT 1")
    suspend fun getByLocalId(entityType: String, localId: Long): SyncMappingEntity?

    /**
     * エンティティタイプとリモート ID でマッピングを取得
     *
     * @param entityType エンティティタイプ
     * @param remoteId Firestore ドキュメント ID
     * @return マッピングエンティティ、存在しない場合は null
     */
    @Query("SELECT * FROM sync_mappings WHERE entity_type = :entityType AND remote_id = :remoteId LIMIT 1")
    suspend fun getByRemoteId(entityType: String, remoteId: String): SyncMappingEntity?

    /**
     * エンティティタイプで全マッピングを取得（削除済み除外）
     *
     * @param entityType エンティティタイプ
     * @return マッピングエンティティのリスト
     */
    @Query("SELECT * FROM sync_mappings WHERE entity_type = :entityType AND is_deleted = 0")
    suspend fun getAllByType(entityType: String): List<SyncMappingEntity>

    /**
     * エンティティタイプで全マッピングを取得（削除済み含む）
     *
     * @param entityType エンティティタイプ
     * @return マッピングエンティティのリスト
     */
    @Query("SELECT * FROM sync_mappings WHERE entity_type = :entityType")
    suspend fun getAllByTypeIncludingDeleted(entityType: String): List<SyncMappingEntity>

    /**
     * マッピングを挿入または更新
     *
     * @param mapping マッピングエンティティ
     * @return 挿入された行の ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mapping: SyncMappingEntity): Long

    /**
     * 複数のマッピングを挿入または更新
     *
     * @param mappings マッピングエンティティのリスト
     * @return 挿入された行の ID リスト
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(mappings: List<SyncMappingEntity>): List<Long>

    /**
     * マッピングを論理削除としてマーク
     *
     * @param entityType エンティティタイプ
     * @param localId Room のローカル ID
     * @param syncedAt 同期日時 (ISO 8601 形式)
     */
    @Query("UPDATE sync_mappings SET is_deleted = 1, last_synced_at = :syncedAt WHERE entity_type = :entityType AND local_id = :localId")
    suspend fun markDeleted(entityType: String, localId: Long, syncedAt: String)

    /**
     * 論理削除されたマッピングを物理削除
     *
     * @param entityType エンティティタイプ
     */
    @Query("DELETE FROM sync_mappings WHERE entity_type = :entityType AND is_deleted = 1")
    suspend fun purgeDeleted(entityType: String)

    /**
     * 指定エンティティタイプの全マッピングを削除
     *
     * @param entityType エンティティタイプ
     */
    @Query("DELETE FROM sync_mappings WHERE entity_type = :entityType")
    suspend fun deleteAllByType(entityType: String)
}
