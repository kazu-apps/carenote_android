package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room ID と Firestore Document ID のマッピングを管理するエンティティ
 *
 * オフラインファースト同期において、ローカル (Room) とリモート (Firestore) 間の
 * ID 対応関係を追跡するために使用される。
 *
 * @property id プライマリキー（Room 自動生成）
 * @property entityType エンティティタイプ ("medication", "note", "healthRecord", etc.)
 * @property localId Room のローカル ID
 * @property remoteId Firestore ドキュメント ID (UUID)
 * @property lastSyncedAt 最終同期日時 (ISO 8601 形式)
 * @property isDeleted 論理削除フラグ（リモートと同期済みの削除を追跡）
 */
@Entity(
    tableName = "sync_mappings",
    indices = [
        Index(value = ["entity_type", "local_id"], unique = true),
        Index(value = ["entity_type", "remote_id"], unique = true)
    ]
)
data class SyncMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "entity_type")
    val entityType: String,

    @ColumnInfo(name = "local_id")
    val localId: Long,

    @ColumnInfo(name = "remote_id")
    val remoteId: String,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: String,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
