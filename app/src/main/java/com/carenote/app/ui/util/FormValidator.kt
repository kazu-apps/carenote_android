package com.carenote.app.ui.util

import androidx.annotation.StringRes
import com.carenote.app.R
import com.carenote.app.ui.common.UiText

object FormValidator {

    inline fun validateRequired(value: String, @StringRes errorResId: Int): UiText? =
        if (value.isBlank()) UiText.Resource(errorResId) else null

    inline fun validateMaxLength(value: String, maxLength: Int): UiText? =
        if (value.length > maxLength) {
            UiText.ResourceWithArgs(R.string.ui_validation_too_long, listOf(maxLength))
        } else {
            null
        }

    inline fun combineValidations(vararg errors: UiText?): UiText? =
        errors.firstOrNull { it != null }
}
