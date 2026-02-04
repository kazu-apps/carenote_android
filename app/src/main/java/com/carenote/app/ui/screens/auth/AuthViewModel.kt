package com.carenote.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ログインフォームの状態
 */
data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val isLoading: Boolean = false
)

/**
 * 新規登録フォームの状態
 */
data class RegisterFormState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val displayNameError: UiText? = null,
    val isLoading: Boolean = false
)

/**
 * パスワードリセットフォームの状態
 */
data class ForgotPasswordFormState(
    val email: String = "",
    val emailError: UiText? = null,
    val isLoading: Boolean = false,
    val emailSent: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState.asStateFlow()

    private val _registerFormState = MutableStateFlow(RegisterFormState())
    val registerFormState: StateFlow<RegisterFormState> = _registerFormState.asStateFlow()

    private val _forgotPasswordFormState = MutableStateFlow(ForgotPasswordFormState())
    val forgotPasswordFormState: StateFlow<ForgotPasswordFormState> =
        _forgotPasswordFormState.asStateFlow()

    private val _authSuccessEvent = MutableSharedFlow<Boolean>(replay = 1)
    val authSuccessEvent: SharedFlow<Boolean> = _authSuccessEvent.asSharedFlow()

    val snackbarController = SnackbarController()

    // ====== Login Functions ======

    fun updateLoginEmail(email: String) {
        _loginFormState.value = _loginFormState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun updateLoginPassword(password: String) {
        _loginFormState.value = _loginFormState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun signIn() {
        val current = _loginFormState.value

        val emailError = validateEmail(current.email)
        val passwordError = validatePasswordForLogin(current.password)

        if (emailError != null || passwordError != null) {
            _loginFormState.value = current.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        _loginFormState.value = current.copy(isLoading = true)

        viewModelScope.launch {
            authRepository.signIn(current.email.trim(), current.password)
                .onSuccess { user ->
                    Timber.d("User signed in: ${user.uid}")
                    _authSuccessEvent.emit(true)
                }
                .onFailure { error ->
                    Timber.w("Sign in failed: $error")
                    _loginFormState.value = _loginFormState.value.copy(isLoading = false)
                    handleAuthError(error)
                }
        }
    }

    // ====== Register Functions ======

    fun updateRegisterEmail(email: String) {
        _registerFormState.value = _registerFormState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun updateRegisterPassword(password: String) {
        _registerFormState.value = _registerFormState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun updateDisplayName(displayName: String) {
        _registerFormState.value = _registerFormState.value.copy(
            displayName = displayName,
            displayNameError = null
        )
    }

    fun signUp() {
        val current = _registerFormState.value

        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)
        val displayNameError = validateDisplayName(current.displayName)

        if (emailError != null || passwordError != null || displayNameError != null) {
            _registerFormState.value = current.copy(
                emailError = emailError,
                passwordError = passwordError,
                displayNameError = displayNameError
            )
            return
        }

        _registerFormState.value = current.copy(isLoading = true)

        viewModelScope.launch {
            authRepository.signUp(
                email = current.email.trim(),
                password = current.password,
                displayName = current.displayName.trim()
            )
                .onSuccess { user ->
                    Timber.d("User signed up: ${user.uid}")
                    snackbarController.showMessage(R.string.auth_register_success)
                    _authSuccessEvent.emit(true)
                }
                .onFailure { error ->
                    Timber.w("Sign up failed: $error")
                    _registerFormState.value = _registerFormState.value.copy(isLoading = false)
                    handleAuthError(error)
                }
        }
    }

    // ====== ForgotPassword Functions ======

    fun updateForgotPasswordEmail(email: String) {
        _forgotPasswordFormState.value = _forgotPasswordFormState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun sendPasswordResetEmail() {
        val current = _forgotPasswordFormState.value

        val emailError = validateEmail(current.email)
        if (emailError != null) {
            _forgotPasswordFormState.value = current.copy(emailError = emailError)
            return
        }

        _forgotPasswordFormState.value = current.copy(isLoading = true)

        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(current.email.trim())
                .onSuccess {
                    Timber.d("Password reset email sent")
                    _forgotPasswordFormState.value = _forgotPasswordFormState.value.copy(
                        isLoading = false,
                        emailSent = true
                    )
                    snackbarController.showMessage(R.string.auth_password_reset_sent)
                }
                .onFailure { error ->
                    Timber.w("Password reset failed: $error")
                    _forgotPasswordFormState.value =
                        _forgotPasswordFormState.value.copy(isLoading = false)
                    handleAuthError(error)
                }
        }
    }

    // ====== Validation Helpers ======

    private fun validateEmail(email: String): UiText? {
        if (email.isBlank()) {
            return UiText.Resource(R.string.auth_email_required)
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

    private fun validatePasswordForLogin(password: String): UiText? {
        if (password.isBlank()) {
            return UiText.Resource(R.string.auth_password_required)
        }
        return null
    }

    private fun validatePassword(password: String): UiText? {
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

    private fun validateDisplayName(displayName: String): UiText? {
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

    private suspend fun handleAuthError(error: DomainError) {
        when (error) {
            is DomainError.ValidationError -> {
                snackbarController.showMessage(error.message)
            }

            is DomainError.NetworkError -> {
                snackbarController.showMessage(R.string.ui_error_network)
            }

            is DomainError.UnauthorizedError -> {
                snackbarController.showMessage(error.message)
            }

            else -> {
                snackbarController.showMessage(R.string.ui_error_unknown)
            }
        }
    }

    fun resetLoginState() {
        _loginFormState.value = LoginFormState()
    }

    fun resetRegisterState() {
        _registerFormState.value = RegisterFormState()
    }

    fun resetForgotPasswordState() {
        _forgotPasswordFormState.value = ForgotPasswordFormState()
    }
}
