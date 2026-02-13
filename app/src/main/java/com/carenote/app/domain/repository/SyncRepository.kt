package com.carenote.app.domain.repository

import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.common.SyncState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * ローカル (Room) とリモート (Firestore) 間のデータ同期を行う Repository インターフェース
 *
 * ## 同期方針
 * - **オフラインファースト**: Room をプライマリ、Firestore を同期先として設計
 * - **双方向同期**: ローカル変更のプッシュ、リモート変更のプルの両方をサポート
 * - **競合解決**: Last-Write-Wins (LWW) 戦略を採用（updatedAt タイムスタンプ比較）
 *
 * ## エンティティタイプ
 * 同期対象のエンティティ:
 * - medications: 服薬管理
 * - medicationLogs: 服薬記録
 * - notes: メモ・申し送り
 * - healthRecords: 健康記録
 * - calendarEvents: カレンダーイベント
 * - tasks: タスク
 *
 * ## 使用例
 * ```kotlin
 * @Inject lateinit var syncRepository: SyncRepository
 *
 * // 同期状態の観察
 * syncRepository.syncState.collect { state ->
 *     when (state) {
 *         is SyncState.Syncing -> updateProgress(state.progress)
 *         is SyncState.Success -> showLastSynced(state.lastSyncedAt)
 *         is SyncState.Error -> showError(state.error)
 *         is SyncState.Idle -> hideProgress()
 *     }
 * }
 *
 * // 全データの同期
 * when (val result = syncRepository.syncAll(careRecipientId)) {
 *     is SyncResult.Success -> Timber.d("Sync complete")
 *     is SyncResult.PartialSuccess -> Timber.w("Partial sync")
 *     is SyncResult.Failure -> Timber.e("Sync failed: ${result.error}")
 * }
 * ```
 *
 * @see SyncResult
 * @see SyncState
 */
interface SyncRepository {

    /**
     * 現在の同期状態を観察する Flow
     *
     * UI レイヤーで collect して同期の進捗状況を表示するために使用。
     */
    val syncState: Flow<SyncState>

    // ========== 全体同期 ==========

    /**
     * 全エンティティタイプを同期する
     *
     * medications → medicationLogs → notes → healthRecords → calendarEvents → tasks
     * の順序で同期を実行。途中でエラーが発生した場合は PartialSuccess または Failure を返す。
     *
     * @param careRecipientId 被介護者 ID (Firestore の careRecipients ドキュメント ID)
     * @return 同期結果
     */
    suspend fun syncAll(careRecipientId: String): SyncResult

    // ========== 個別エンティティ同期 ==========

    /**
     * 服薬管理データを同期する
     *
     * @param careRecipientId 被介護者 ID
     * @return 同期結果
     */
    suspend fun syncMedications(careRecipientId: String): SyncResult

    /**
     * 特定の服薬の服薬記録を同期する
     *
     * @param careRecipientId 被介護者 ID
     * @param medicationId 服薬 ID (Room の localId)
     * @return 同期結果
     */
    suspend fun syncMedicationLogs(careRecipientId: String, medicationId: Long): SyncResult

    /**
     * メモ・申し送りデータを同期する
     *
     * @param careRecipientId 被介護者 ID
     * @return 同期結果
     */
    suspend fun syncNotes(careRecipientId: String): SyncResult

    /**
     * 健康記録データを同期する
     *
     * @param careRecipientId 被介護者 ID
     * @return 同期結果
     */
    suspend fun syncHealthRecords(careRecipientId: String): SyncResult

    /**
     * カレンダーイベントデータを同期する
     *
     * @param careRecipientId 被介護者 ID
     * @return 同期結果
     */
    suspend fun syncCalendarEvents(careRecipientId: String): SyncResult

    /**
     * タスクデータを同期する
     *
     * @param careRecipientId 被介護者 ID
     * @return 同期結果
     */
    suspend fun syncTasks(careRecipientId: String): SyncResult

    /**
     * メモコメントデータを同期する
     *
     * @param careRecipientId 被介護者 ID
     * @return 同期結果
     */
    suspend fun syncNoteComments(careRecipientId: String): SyncResult

    // ========== 同期メタデータ ==========

    /**
     * 最終同期日時を取得する
     *
     * @return 最終同期日時。一度も同期していない場合は null
     */
    suspend fun getLastSyncTime(): LocalDateTime?

    // ========== 方向指定同期 ==========

    /**
     * ローカルの変更をリモートにプッシュする
     *
     * Room で変更されたデータ（新規作成・更新・削除）を Firestore にアップロード。
     * syncedAt < updatedAt のエンティティが対象。
     *
     * @param careRecipientId 被介護者 ID
     * @return 同期結果
     */
    suspend fun pushLocalChanges(careRecipientId: String): SyncResult

    /**
     * リモートの変更をローカルにプルする
     *
     * Firestore で変更されたデータを Room にダウンロード。
     * サーバー側の updatedAt が lastSyncTime より新しいエンティティが対象。
     *
     * @param careRecipientId 被介護者 ID
     * @return 同期結果
     */
    suspend fun pullRemoteChanges(careRecipientId: String): SyncResult
}
