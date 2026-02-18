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

    @PublishedApi
    internal val PHONE_PATTERN = Regex("^[0-9+\\-() ]{0,20}$")

    inline fun validatePhoneFormat(value: String): UiText? =
        if (value.isEmpty() || PHONE_PATTERN.matches(value)) {
            null
        } else {
            UiText.Resource(R.string.ui_validation_invalid_phone_format)
        }
}
