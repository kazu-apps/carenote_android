package com.carenote.app.ui.screens.auth

import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.data.worker.SyncWorkSchedulerInterface
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

class LoginFormHandler(
    private val authRepository: AuthRepository,
    private val syncWorkScheduler: SyncWorkSchedulerInterface,
    val snackbarController: SnackbarController = SnackbarController(),
    private val authSuccessChannel: Channel<Boolean> = Channel(Channel.BUFFERED),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

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

    fun signIn() {
        val current = _formState.value

        val emailError = AuthValidators.validateEmail(current.email)
        val passwordError = AuthValidators.validatePasswordForLogin(current.password)

        if (emailError != null || passwordError != null) {
            _formState.value = current.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        _formState.value = current.copy(isLoading = true)

        scope.launch {
            authRepository.signIn(current.email.trim(), current.password)
                .onSuccess { user ->
                    Timber.d("User signed in successfully")
                    if (!user.isEmailVerified) {
                        snackbarController.showMessage(R.string.auth_email_not_verified)
                    }
                    syncWorkScheduler.schedulePeriodicSync()
                    authSuccessChannel.send(true)
                }
                .onFailure { error ->
                    Timber.w("Sign in failed: $error")
                    _formState.value = _formState.value.copy(isLoading = false)
                    handleAuthError(error)
                }
        }
    }

    fun resetState() {
        _formState.value = LoginFormState()
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
