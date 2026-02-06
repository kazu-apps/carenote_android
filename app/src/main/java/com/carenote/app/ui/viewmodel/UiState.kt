package com.carenote.app.ui.viewmodel

import com.carenote.app.domain.common.DomainError

/**
 * UI層の状態を表す sealed class
 *
 * ViewModel から UI に状態を伝達するために使用する。
 * Loading / Success / Error の3状態で画面の表示を管理する。
 *
 * @param T 成功時のデータ型
 *
 * Usage:
 * ```
 * val uiState: StateFlow<UiState<List<Medication>>> = ...
 *
 * when (val state = uiState.collectAsStateWithLifecycle().value) {
 *     is UiState.Loading -> LoadingIndicator()
 *     is UiState.Success -> MedicationList(state.data)
 *     is UiState.Error -> ErrorDisplay(state.error)
 * }
 * ```
 */
sealed class UiState<out T> {

    /**
     * データ読み込み中の状態
     */
    data object Loading : UiState<Nothing>()

    /**
     * データ取得成功の状態
     */
    data class Success<out T>(val data: T) : UiState<T>()

    /**
     * エラー発生の状態
     */
    data class Error<out T>(val error: DomainError) : UiState<T>()

    /**
     * Loading 状態かどうかを返す
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Success 状態かどうかを返す
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Error 状態かどうかを返す
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 成功時のデータを返す。それ以外は null を返す。
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Loading -> null
        is Error -> null
    }

    /**
     * 成功時のデータを返す。それ以外はデフォルト値を返す。
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Loading -> default
        is Error -> default
    }

    /**
     * エラー時のエラーを返す。それ以外は null を返す。
     */
    fun errorOrNull(): DomainError? = when (this) {
        is Error -> error
        is Loading -> null
        is Success -> null
    }
}
