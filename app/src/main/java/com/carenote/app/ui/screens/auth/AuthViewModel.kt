package com.carenote.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.carenote.app.data.worker.SyncWorkSchedulerInterface
import com.carenote.app.domain.repository.AuthRepository
import androidx.lifecycle.viewModelScope
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val isLoading: Boolean = false
)

data class RegisterFormState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val displayNameError: UiText? = null,
    val isLoading: Boolean = false
)

data class ForgotPasswordFormState(
    val email: String = "",
    val emailError: UiText? = null,
    val isLoading: Boolean = false,
    val emailSent: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    authRepository: AuthRepository,
    syncWorkScheduler: SyncWorkSchedulerInterface
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _authSuccessEvent = Channel<Boolean>(Channel.BUFFERED)
    val authSuccessEvent: Flow<Boolean> = _authSuccessEvent.receiveAsFlow()

    private val loginHandler = LoginFormHandler(
        authRepository, syncWorkScheduler, snackbarController, _authSuccessEvent,
        scope = viewModelScope
    )
    private val registerHandler = RegisterFormHandler(
        authRepository, syncWorkScheduler, snackbarController, _authSuccessEvent,
        scope = viewModelScope
    )
    private val forgotPasswordHandler = ForgotPasswordFormHandler(
        authRepository, snackbarController,
        scope = viewModelScope
    )

    val loginFormState: StateFlow<LoginFormState> = loginHandler.formState
    val registerFormState: StateFlow<RegisterFormState> = registerHandler.formState
    val forgotPasswordFormState: StateFlow<ForgotPasswordFormState> =
        forgotPasswordHandler.formState

    // ====== Login Functions ======

    fun updateLoginEmail(email: String) = loginHandler.updateEmail(email)
    fun updateLoginPassword(password: String) = loginHandler.updatePassword(password)
    fun signIn() = loginHandler.signIn()

    // ====== Register Functions ======

    fun updateRegisterEmail(email: String) = registerHandler.updateEmail(email)
    fun updateRegisterPassword(password: String) = registerHandler.updatePassword(password)
    fun updateDisplayName(displayName: String) = registerHandler.updateDisplayName(displayName)
    fun signUp() = registerHandler.signUp()

    // ====== ForgotPassword Functions ======

    fun updateForgotPasswordEmail(email: String) = forgotPasswordHandler.updateEmail(email)
    fun sendPasswordResetEmail() = forgotPasswordHandler.sendPasswordResetEmail()

    // ====== Reset Functions ======

    fun resetLoginState() = loginHandler.resetState()
    fun resetRegisterState() = registerHandler.resetState()
    fun resetForgotPasswordState() = forgotPasswordHandler.resetState()
}
