package com.carenote.app.ui.screens.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.model.MemberRole
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.InvitationRepository
import com.carenote.app.domain.repository.MemberRepository
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MemberManagementViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val invitationRepository: InvitationRepository,
    private val authRepository: AuthRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    val members: StateFlow<List<Member>> = memberRepository.getAllMembers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    val pendingInvitations: StateFlow<List<Invitation>> = invitationRepository.getAllInvitations()
        .map { invitations -> invitations.filter { it.status == InvitationStatus.PENDING } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    val isOwner: StateFlow<Boolean> = members.map { memberList ->
        val currentUid = authRepository.getCurrentUser()?.uid
        currentUid != null && memberList.any { it.uid == currentUid && it.role == MemberRole.OWNER }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
        initialValue = false
    )

    fun deleteMember(id: Long) {
        viewModelScope.launch {
            val currentUid = authRepository.getCurrentUser()?.uid
            val memberList = members.value
            val member = memberList.find { it.id == id }

            if (member == null) {
                return@launch
            }

            if (member.uid == currentUid) {
                snackbarController.showMessage(R.string.member_delete_self_error)
                return@launch
            }

            val isCurrentUserOwner = currentUid != null &&
                memberList.any { it.uid == currentUid && it.role == MemberRole.OWNER }
            if (!isCurrentUserOwner) {
                snackbarController.showMessage(R.string.member_delete_permission_error)
                return@launch
            }

            memberRepository.deleteMember(id)
                .onSuccess {
                    Timber.d("Member deleted: id=$id")
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_MEMBER_DELETED)
                    snackbarController.showMessage(R.string.member_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete member: $error")
                    snackbarController.showMessage(R.string.member_delete_failed)
                }
        }
    }

    fun cancelInvitation(id: Long) {
        viewModelScope.launch {
            val currentUid = authRepository.getCurrentUser()?.uid
            val memberList = members.value
            val isCurrentUserOwner = currentUid != null &&
                memberList.any { it.uid == currentUid && it.role == MemberRole.OWNER }
            if (!isCurrentUserOwner) {
                snackbarController.showMessage(R.string.invitation_cancel_permission_error)
                return@launch
            }

            invitationRepository.deleteInvitation(id)
                .onSuccess {
                    Timber.d("Invitation cancelled: id=$id")
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_INVITATION_CANCELLED)
                    snackbarController.showMessage(R.string.invitation_cancelled)
                }
                .onFailure { error ->
                    Timber.w("Failed to cancel invitation: $error")
                    snackbarController.showMessage(R.string.invitation_cancel_failed)
                }
        }
    }
}
