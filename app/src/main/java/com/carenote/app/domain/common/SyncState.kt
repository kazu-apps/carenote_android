package com.carenote.app.domain.common

import java.time.LocalDateTime

/**
 * 同期の進行状態を表す sealed class
 *
 * UI レイヤーで同期状態を観察し、プログレスインジケーターやエラーメッセージを表示するために使用。
 *
 * Usage:
 * ```
 * viewModel.syncState.collect { state ->
 *     when (state) {
 *         is SyncState.Idle -> hideProgress()
 *         is SyncState.Syncing -> showProgress(state.progress, state.currentEntity)
 *         is SyncState.Success -> showSuccess("最終同期: ${state.lastSyncedAt}")
 *         is SyncState.Error -> showError(state.error.message)
 *     }
 * }
 * ```
 */
sealed class SyncState {

    /**
     * 同期していない状態
     *
     * アプリ起動時の初期状態、または同期完了後にリセットされた状態。
     */
    data object Idle : SyncState()

    /**
     * 同期中の状態
     *
     * @param progress 進捗率 (0.0 ~ 1.0)
     * @param currentEntity 現在同期中のエンティティタイプ ("medications", "notes" など)
     */
    data class Syncing(
        val progress: Float,
        val currentEntity: String
    ) : SyncState() {
        init {
            require(progress in 0f..1f) { "Progress must be between 0.0 and 1.0" }
        }

        /** 進捗率をパーセンテージで取得 (0 ~ 100) */
        val progressPercent: Int
            get() = (progress * 100).toInt()
    }

    /**
     * 同期成功状態
     *
     * @param lastSyncedAt 最終同期日時
     */
    data class Success(
        val lastSyncedAt: LocalDateTime
    ) : SyncState()

    /**
     * 同期エラー状態
     *
     * @param error 発生したエラー
     * @param isRetryable エラーが再試行可能かどうか（ネットワークエラー等は再試行可能）
     */
    data class Error(
        val error: DomainError,
        val isRetryable: Boolean = true
    ) : SyncState()

    /**
     * 同期中かどうかを返す
     */
    val isSyncing: Boolean
        get() = this is Syncing

    /**
     * エラー状態かどうかを返す
     */
    val isError: Boolean
        get() = this is Error
}
