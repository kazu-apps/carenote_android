package com.carenote.app.ui.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * スナックバーイベントを表すデータクラス
 *
 * @param message 表示メッセージ
 * @param actionLabel アクションボタンラベル（null の場合アクションなし）
 * @param onAction アクション実行時のコールバック
 */
data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

/**
 * 一元的なスナックバー管理コントローラー
 *
 * ViewModel から UI 層へスナックバーイベントを送信するために使用する。
 * Channel を使用して一度だけ消費されるイベントを保証する。
 *
 * Usage:
 * ```
 * // ViewModel
 * val snackbarController = SnackbarController()
 * snackbarController.showMessage("保存しました")
 *
 * // UI (Composable)
 * val snackbarEvent by snackbarController.events.collectAsState(initial = null)
 * ```
 */
class SnackbarController {

    private val _events = Channel<SnackbarEvent>(Channel.BUFFERED)

    /**
     * スナックバーイベントの Flow
     * UI 層で collect してスナックバーを表示する
     */
    val events = _events.receiveAsFlow()

    /**
     * メッセージのみのスナックバーを表示する
     */
    suspend fun showMessage(message: String) {
        _events.send(SnackbarEvent(message = message))
    }

    /**
     * アクション付きスナックバーを表示する
     */
    suspend fun showMessageWithAction(
        message: String,
        actionLabel: String,
        onAction: () -> Unit
    ) {
        _events.send(
            SnackbarEvent(
                message = message,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }
}
