package com.carenote.app.ui.screens.auth

import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.validator.InputValidator
import com.carenote.app.ui.common.UiText

object AuthValidators {

    fun validateEmail(email: String): UiText? {
        if (email.isBlank()) {
            return UiText.Resource(R.string.auth_email_required)
        }
        val domainError = InputValidator.validateEmail(email)
        if (domainError != null && domainError != "Email is required") {
            return UiText.Resource(R.string.auth_email_invalid)
        }
        if (email.length > AppConfig.Auth.EMAIL_MAX_LENGTH) {
            return UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Auth.EMAIL_MAX_LENGTH)
            )
        }
        return null
    }

    fun validatePasswordForLogin(password: String): UiText? {
        if (password.isBlank()) {
            return UiText.Resource(R.string.auth_password_required)
        }
        return null
    }

    fun validatePassword(password: String): UiText? {
        if (password.isBlank()) {
            return UiText.Resource(R.string.auth_password_required)
        }
        if (password.length < AppConfig.Auth.PASSWORD_MIN_LENGTH) {
            return UiText.ResourceWithArgs(
                R.string.auth_password_too_short,
                listOf(AppConfig.Auth.PASSWORD_MIN_LENGTH)
            )
        }
        if (password.length > AppConfig.Auth.PASSWORD_MAX_LENGTH) {
            return UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Auth.PASSWORD_MAX_LENGTH)
            )
        }
        return null
    }

    fun validateDisplayName(displayName: String): UiText? {
        if (displayName.isBlank()) {
            return UiText.Resource(R.string.auth_display_name_required)
        }
        if (displayName.length > AppConfig.Auth.DISPLAY_NAME_MAX_LENGTH) {
            return UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Auth.DISPLAY_NAME_MAX_LENGTH)
            )
        }
        return null
    }
}
