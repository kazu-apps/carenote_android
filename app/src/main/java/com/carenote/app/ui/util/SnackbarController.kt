package com.carenote.app.ui.util

import androidx.annotation.StringRes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * スナックバーイベントを表す sealed interface
 *
 * メッセージは文字列リソース ID（StringRes）または直接文字列で指定可能。
 * UI 層で resolve してスナックバーに表示する。
 */
sealed interface SnackbarEvent {
    val actionLabel: String?
    val onAction: (() -> Unit)?

    data class WithResId(
        @StringRes val messageResId: Int,
        override val actionLabel: String? = null,
        override val onAction: (() -> Unit)? = null
    ) : SnackbarEvent

    data class WithString(
        val message: String,
        override val actionLabel: String? = null,
        override val onAction: (() -> Unit)? = null
    ) : SnackbarEvent
}

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
 * snackbarController.showMessage(R.string.saved)
 *
 * // UI (Composable)
 * LaunchedEffect(Unit) {
 *     viewModel.snackbarController.events.collect { event ->
 *         val message = event.resolveMessage(context)
 *         snackbarHostState.showSnackbar(message)
 *     }
 * }
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
     * 文字列リソース ID でスナックバーを表示する（推奨）
     */
    suspend fun showMessage(@StringRes messageResId: Int) {
        _events.send(SnackbarEvent.WithResId(messageResId = messageResId))
    }

    /**
     * 直接文字列でスナックバーを表示する
     */
    suspend fun showMessage(message: String) {
        _events.send(SnackbarEvent.WithString(message = message))
    }

    /**
     * アクション付きスナックバーを表示する
     */
    suspend fun showMessageWithAction(
        @StringRes messageResId: Int,
        actionLabel: String,
        onAction: () -> Unit
    ) {
        _events.send(
            SnackbarEvent.WithResId(
                messageResId = messageResId,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }
}
