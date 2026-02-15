package com.carenote.app.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class Resource(@StringRes val resId: Int) : UiText()
    data class ResourceWithArgs(
        @StringRes val resId: Int,
        val args: List<Any>
    ) : UiText()
    data class DynamicString(val text: String) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is Resource -> stringResource(resId)
        is ResourceWithArgs -> stringResource(resId, *args.toTypedArray())
        is DynamicString -> text
    }
}
