package com.carenote.app.ui.screens.member

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.model.MemberRole
import com.carenote.app.domain.model.User
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.InvitationRepository
import com.carenote.app.domain.repository.MemberRepository
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

sealed class AcceptInvitationUiState {
    data object Loading : AcceptInvitationUiState()
    data class Content(
        val invitation: Invitation,
        val isAccepting: Boolean = false
    ) : AcceptInvitationUiState()
    data class Error(val message: UiText) : AcceptInvitationUiState()
    data object Success : AcceptInvitationUiState()
}

@HiltViewModel
class AcceptInvitationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val invitationRepository: InvitationRepository,
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: com.carenote.app.domain.util.Clock
) : ViewModel() {

    private val token: String = savedStateHandle.get<String>("invitationToken") ?: ""

    private val _uiState = MutableStateFlow<AcceptInvitationUiState>(AcceptInvitationUiState.Loading)
    val uiState: StateFlow<AcceptInvitationUiState> = _uiState.asStateFlow()

    val snackbarController = SnackbarController()

    init {
        loadInvitation()
    }

    private fun loadInvitation() {
        if (token.isBlank()) {
            _uiState.value = AcceptInvitationUiState.Error(
                UiText.Resource(R.string.accept_invitation_invalid_token)
            )
            return
        }

        viewModelScope.launch {
            val invitation = invitationRepository.getInvitationByToken(token).firstOrNull()
            if (invitation == null) {
                _uiState.value = AcceptInvitationUiState.Error(
                    UiText.Resource(R.string.accept_invitation_invalid_token)
                )
                return@launch
            }

            val now = clock.now()
            when {
                invitation.status == InvitationStatus.ACCEPTED -> {
                    _uiState.value = AcceptInvitationUiState.Error(
                        UiText.Resource(R.string.accept_invitation_already_accepted)
                    )
                }
                invitation.status == InvitationStatus.REJECTED -> {
                    _uiState.value = AcceptInvitationUiState.Error(
                        UiText.Resource(R.string.accept_invitation_invalid_token)
                    )
                }
                now.isAfter(invitation.expiresAt) -> {
                    _uiState.value = AcceptInvitationUiState.Error(
                        UiText.Resource(R.string.accept_invitation_expired)
                    )
                }
                else -> {
                    _uiState.value = AcceptInvitationUiState.Content(
                        invitation = invitation
                    )
                }
            }
        }
    }

    fun accept() {
        val state = _uiState.value
        if (state !is AcceptInvitationUiState.Content) return

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.accept_invitation_not_logged_in)
            }
            return
        }

        if (state.invitation.inviteeEmail.lowercase() != currentUser.email.lowercase()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.accept_invitation_email_mismatch)
            }
            return
        }

        _uiState.value = state.copy(isAccepting = true)

        viewModelScope.launch {
            val now = clock.now()
            if (now.isAfter(state.invitation.expiresAt)) {
                _uiState.value = AcceptInvitationUiState.Error(
                    UiText.Resource(R.string.accept_invitation_expired)
                )
                return@launch
            }

            if (!addMemberIfNeeded(state, currentUser, now)) return@launch
            completeAcceptance(state)
        }
    }

    private suspend fun addMemberIfNeeded(
        state: AcceptInvitationUiState.Content,
        currentUser: User,
        now: LocalDateTime
    ): Boolean {
        val existingMembers = memberRepository.getAllMembers().firstOrNull() ?: emptyList()
        val isDuplicate = existingMembers.any {
            it.uid == currentUser.uid &&
                it.careRecipientId == state.invitation.careRecipientId
        }
        if (isDuplicate) return true

        val member = Member(
            careRecipientId = state.invitation.careRecipientId,
            uid = currentUser.uid,
            role = MemberRole.MEMBER,
            joinedAt = now
        )
        memberRepository.insertMember(member)
            .onFailure { error ->
                Timber.w("Failed to accept invitation: $error")
                _uiState.value = state.copy(isAccepting = false)
                snackbarController.showMessage(R.string.accept_invitation_failed)
                return false
            }
        return true
    }

    private suspend fun completeAcceptance(state: AcceptInvitationUiState.Content) {
        val updatedInvitation = state.invitation.copy(
            status = InvitationStatus.ACCEPTED
        )
        invitationRepository.updateInvitation(updatedInvitation)
            .onSuccess {
                Timber.d("Invitation accepted successfully")
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_INVITATION_ACCEPTED)
                _uiState.value = AcceptInvitationUiState.Success
            }
            .onFailure { error ->
                Timber.w("Failed to update invitation status: $error")
                _uiState.value = AcceptInvitationUiState.Error(
                    UiText.Resource(R.string.accept_invitation_failed)
                )
            }
    }

    fun decline() {
        val state = _uiState.value
        if (state !is AcceptInvitationUiState.Content) return

        viewModelScope.launch {
            val updatedInvitation = state.invitation.copy(
                status = InvitationStatus.REJECTED
            )
            invitationRepository.updateInvitation(updatedInvitation)
                .onSuccess {
                    Timber.d("Invitation declined")
                }
                .onFailure { error ->
                    Timber.w("Failed to decline invitation: $error")
                }
        }
    }
}
