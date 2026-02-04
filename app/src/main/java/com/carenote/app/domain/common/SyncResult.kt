package com.carenote.app.domain.common

/**
 * 同期操作の結果を表す sealed class
 *
 * ローカル (Room) とリモート (Firestore) 間のデータ同期結果を型安全に表現する。
 * 成功・部分成功・失敗の3パターンを区別し、詳細な同期統計情報を提供する。
 *
 * Usage:
 * ```
 * when (val result = syncRepository.syncMedications(careRecipientId)) {
 *     is SyncResult.Success -> {
 *         Timber.d("Sync complete: ${result.uploadedCount} uploaded, ${result.downloadedCount} downloaded")
 *     }
 *     is SyncResult.PartialSuccess -> {
 *         Timber.w("Partial sync: ${result.failedEntities.size} failed")
 *         result.errors.forEach { Timber.w("Error: $it") }
 *     }
 *     is SyncResult.Failure -> {
 *         Timber.e("Sync failed: ${result.error}")
 *     }
 * }
 * ```
 */
sealed class SyncResult {

    /**
     * 同期が完全に成功した場合
     *
     * @param uploadedCount ローカルからリモートにアップロードされたエンティティ数
     * @param downloadedCount リモートからローカルにダウンロードされたエンティティ数
     * @param conflictCount LWW で解決された競合の数
     */
    data class Success(
        val uploadedCount: Int,
        val downloadedCount: Int,
        val conflictCount: Int = 0
    ) : SyncResult() {
        /** 同期されたエンティティの合計数 */
        val totalSynced: Int
            get() = uploadedCount + downloadedCount
    }

    /**
     * 一部のエンティティの同期に失敗した場合
     *
     * 成功したエンティティは同期され、失敗したエンティティのみ再試行が必要。
     *
     * @param successCount 同期に成功したエンティティ数
     * @param failedEntities 同期に失敗したエンティティの識別子リスト (localId)
     * @param errors 各失敗の詳細エラー
     */
    data class PartialSuccess(
        val successCount: Int,
        val failedEntities: List<Long>,
        val errors: List<DomainError>
    ) : SyncResult()

    /**
     * 同期が完全に失敗した場合
     *
     * ネットワークエラー、認証エラー等で同期処理自体が実行できなかった場合。
     *
     * @param error 失敗の原因
     */
    data class Failure(
        val error: DomainError
    ) : SyncResult()

    /**
     * 成功かどうかを返す（部分成功は含まない）
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 失敗かどうかを返す（部分成功は含まない）
     */
    val isFailure: Boolean
        get() = this is Failure

    /**
     * 部分成功かどうかを返す
     */
    val isPartialSuccess: Boolean
        get() = this is PartialSuccess
}
