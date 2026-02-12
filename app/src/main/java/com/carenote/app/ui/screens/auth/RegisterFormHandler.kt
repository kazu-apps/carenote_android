package com.carenote.app.ui.screens.auth

import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.SyncWorkSchedulerInterface
import com.carenote.app.ui.util.SnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RegisterFormHandler(
    private val authRepository: AuthRepository,
    private val syncWorkScheduler: SyncWorkSchedulerInterface,
    private val analyticsRepository: AnalyticsRepository,
    val snackbarController: SnackbarController = SnackbarController(),
    private val authSuccessChannel: Channel<Boolean> = Channel(Channel.BUFFERED),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    fun updateEmail(email: String) {
        _formState.value = _formState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun updatePassword(password: String) {
        _formState.value = _formState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun updateDisplayName(displayName: String) {
        _formState.value = _formState.value.copy(
            displayName = displayName,
            displayNameError = null
        )
    }

    fun signUp() {
        val current = _formState.value

        val emailError = AuthValidators.validateEmail(current.email)
        val passwordError = AuthValidators.validatePassword(current.password)
        val displayNameError = AuthValidators.validateDisplayName(current.displayName)

        if (emailError != null || passwordError != null || displayNameError != null) {
            _formState.value = current.copy(
                emailError = emailError,
                passwordError = passwordError,
                displayNameError = displayNameError
            )
            return
        }

        _formState.value = current.copy(isLoading = true)

        scope.launch {
            authRepository.signUp(
                email = current.email.trim(),
                password = current.password,
                displayName = current.displayName.trim()
            )
                .onSuccess {
                    Timber.d("User signed up successfully")
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_SIGN_UP)
                    syncWorkScheduler.schedulePeriodicSync()
                    syncWorkScheduler.triggerImmediateSync()
                    snackbarController.showMessage(R.string.auth_register_success)
                    authSuccessChannel.send(true)
                }
                .onFailure { error ->
                    Timber.w("Sign up failed: $error")
                    _formState.value = _formState.value.copy(isLoading = false)
                    handleAuthError(error)
                }
        }
    }

    fun resetState() {
        _formState.value = RegisterFormState()
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
