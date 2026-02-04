package com.carenote.app.data.remote.model

import java.time.LocalDateTime

/**
 * Firestore 同期メタデータ
 *
 * Firestore ドキュメントに追加される同期関連のメタデータ。
 * Room ID と Firestore 間のマッピング、および削除状態の追跡に使用。
 */
data class SyncMetadata(
    /** Room のローカル ID */
    val localId: Long,
    /** 最終同期日時 */
    val syncedAt: LocalDateTime,
    /** 論理削除日時（null = 有効） */
    val deletedAt: LocalDateTime? = null
)
