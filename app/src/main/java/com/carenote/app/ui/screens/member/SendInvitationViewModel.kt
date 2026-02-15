package com.carenote.app.ui.screens.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.InvitationRepository
import com.carenote.app.domain.validator.InputValidator
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject

data class SendInvitationFormState(
    val email: String = "",
    val message: String = "",
    val emailError: UiText? = null,
    val isSending: Boolean = false
)

data class InvitationSavedResult(
    val token: String,
    val inviteLink: String
)

@HiltViewModel
class SendInvitationViewModel @Inject constructor(
    private val invitationRepository: InvitationRepository,
    private val authRepository: AuthRepository,
    private val activeCareRecipientProvider: ActiveCareRecipientProvider,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: com.carenote.app.domain.util.Clock
) : ViewModel() {

    private val _formState = MutableStateFlow(SendInvitationFormState())
    val formState: StateFlow<SendInvitationFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<InvitationSavedResult>(Channel.BUFFERED)
    val savedEvent: Flow<InvitationSavedResult> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private val initialFormState = SendInvitationFormState()

    val isDirty: Boolean
        get() {
            val current = _formState.value.copy(emailError = null, isSending = false)
            val baseline = initialFormState.copy(emailError = null, isSending = false)
            return current != baseline
        }

    fun updateEmail(email: String) {
        _formState.value = _formState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun updateMessage(message: String) {
        _formState.value = _formState.value.copy(message = message)
    }

    fun send() {
        val current = _formState.value

        val emailError = validateEmail(current.email)
        if (emailError != null) {
            _formState.value = current.copy(emailError = emailError)
            return
        }

        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null && current.email.trim().equals(currentUser.email, ignoreCase = true)) {
            _formState.value = current.copy(
                emailError = UiText.Resource(R.string.send_invitation_self_error)
            )
            return
        }

        viewModelScope.launch {
            // Check for existing PENDING invitation
            val existingPending = invitationRepository.getInvitationsByEmail(
                current.email.trim(), InvitationStatus.PENDING
            ).firstOrNull() ?: emptyList()
            if (existingPending.isNotEmpty()) {
                _formState.value = current.copy(
                    emailError = UiText.Resource(R.string.send_invitation_duplicate_error)
                )
                return@launch
            }

            _formState.value = current.copy(isSending = true)

            val token = generateToken()
            val now = clock.now()
            val expiresAt = now.plusDays(AppConfig.Member.INVITATION_VALID_DAYS)
            val careRecipientId = activeCareRecipientProvider.getActiveCareRecipientId()
            val inviterUid = currentUser?.uid ?: ""

            val invitation = Invitation(
                careRecipientId = careRecipientId,
                inviterUid = inviterUid,
                inviteeEmail = current.email.trim(),
                status = InvitationStatus.PENDING,
                token = token,
                expiresAt = expiresAt,
                createdAt = now
            )

            invitationRepository.insertInvitation(invitation)
                .onSuccess {
                    Timber.d("Invitation sent successfully")
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_INVITATION_SENT)
                    val inviteLink = "https://${AppConfig.Member.DEEP_LINK_HOST}${AppConfig.Member.DEEP_LINK_PATH_PREFIX}/$token"
                    _savedEvent.send(InvitationSavedResult(token = token, inviteLink = inviteLink))
                }
                .onFailure { error ->
                    Timber.w("Failed to send invitation: $error")
                    _formState.value = _formState.value.copy(isSending = false)
                    snackbarController.showMessage(R.string.send_invitation_failed)
                }
        }
    }

    private fun validateEmail(email: String): UiText? {
        if (email.isBlank()) {
            return UiText.Resource(R.string.send_invitation_email_required)
        }
        val domainError = InputValidator.validateEmail(email)
        if (domainError != null && domainError != "Email is required") {
            return UiText.Resource(R.string.send_invitation_email_invalid)
        }
        if (email.length > AppConfig.Member.EMAIL_MAX_LENGTH) {
            return UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Member.EMAIL_MAX_LENGTH)
            )
        }
        return null
    }

    private fun generateToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(AppConfig.Member.INVITATION_TOKEN_LENGTH / 2)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
