package com.carenote.app.ui.screens.auth

import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.ui.util.SnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ForgotPasswordFormHandler(
    private val authRepository: AuthRepository,
    val snackbarController: SnackbarController = SnackbarController(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {

    private val _formState = MutableStateFlow(ForgotPasswordFormState())
    val formState: StateFlow<ForgotPasswordFormState> = _formState.asStateFlow()

    fun updateEmail(email: String) {
        _formState.value = _formState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun sendPasswordResetEmail() {
        val current = _formState.value

        val emailError = AuthValidators.validateEmail(current.email)
        if (emailError != null) {
            _formState.value = current.copy(emailError = emailError)
            return
        }

        _formState.value = current.copy(isLoading = true)

        scope.launch {
            authRepository.sendPasswordResetEmail(current.email.trim())
                .onSuccess {
                    Timber.d("Password reset email sent")
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        emailSent = true
                    )
                    snackbarController.showMessage(R.string.auth_password_reset_sent)
                }
                .onFailure { error ->
                    Timber.w("Password reset failed: $error")
                    _formState.value = _formState.value.copy(isLoading = false)
                    handleAuthError(error)
                }
        }
    }

    fun resetState() {
        _formState.value = ForgotPasswordFormState()
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
}
