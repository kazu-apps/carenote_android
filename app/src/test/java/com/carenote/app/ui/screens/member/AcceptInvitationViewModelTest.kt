package com.carenote.app.ui.screens.member

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakeInvitationRepository
import com.carenote.app.fakes.FakeMemberRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aInvitation
import com.carenote.app.testing.aUser
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AcceptInvitationViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private lateinit var invitationRepository: FakeInvitationRepository
    private lateinit var memberRepository: FakeMemberRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private val fakeClock = FakeClock()

    @Before
    fun setUp() {
        invitationRepository = FakeInvitationRepository()
        memberRepository = FakeMemberRepository()
        authRepository = FakeAuthRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    private fun createViewModel(
        token: String = "test-token"
    ): AcceptInvitationViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("invitationToken" to token))
        return AcceptInvitationViewModel(
            savedStateHandle,
            invitationRepository,
            memberRepository,
            authRepository,
            analyticsRepository,
            fakeClock
        )
    }

    @Test
    fun `loading state on init`() = runTest {
        // Create ViewModel without any invitation in repository
        // The initial state is Loading before coroutine processes
        val savedStateHandle = SavedStateHandle(mapOf("invitationToken" to "test-token"))
        // With UnconfinedTestDispatcher, the coroutine runs immediately,
        // so we test that eventually it reaches Error (since no invitation exists)
        val viewModel = AcceptInvitationViewModel(
            savedStateHandle,
            invitationRepository,
            memberRepository,
            authRepository,
            analyticsRepository,
            fakeClock
        )

        viewModel.uiState.test {
            val state = awaitItem()
            // With UnconfinedTestDispatcher and no matching invitation, it goes to Error
            assertTrue(state is AcceptInvitationUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `content state with valid token`() = runTest {
        val invitation = aInvitation(
            id = 1L,
            token = "valid-token",
            status = InvitationStatus.PENDING,
            expiresAt = fakeClock.now().plusDays(7)
        )
        invitationRepository.setInvitations(listOf(invitation))

        val viewModel = createViewModel(token = "valid-token")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AcceptInvitationUiState.Content)
            val content = state as AcceptInvitationUiState.Content
            assertEquals("valid-token", content.invitation.token)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state with empty token`() = runTest {
        val viewModel = createViewModel(token = "")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AcceptInvitationUiState.Error)
            val error = state as AcceptInvitationUiState.Error
            assertTrue(error.message is UiText.Resource)
            assertEquals(R.string.accept_invitation_invalid_token, (error.message as UiText.Resource).resId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state with non-existent token`() = runTest {
        val viewModel = createViewModel(token = "non-existent-token")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AcceptInvitationUiState.Error)
            val error = state as AcceptInvitationUiState.Error
            assertTrue(error.message is UiText.Resource)
            assertEquals(R.string.accept_invitation_invalid_token, (error.message as UiText.Resource).resId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state with expired invitation`() = runTest {
        val invitation = aInvitation(
            id = 1L,
            token = "expired-token",
            status = InvitationStatus.PENDING,
            expiresAt = fakeClock.now().minusDays(1)
        )
        invitationRepository.setInvitations(listOf(invitation))

        val viewModel = createViewModel(token = "expired-token")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AcceptInvitationUiState.Error)
            val error = state as AcceptInvitationUiState.Error
            assertTrue(error.message is UiText.Resource)
            assertEquals(R.string.accept_invitation_expired, (error.message as UiText.Resource).resId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state with already accepted invitation`() = runTest {
        val invitation = aInvitation(
            id = 1L,
            token = "accepted-token",
            status = InvitationStatus.ACCEPTED,
            expiresAt = fakeClock.now().plusDays(7)
        )
        invitationRepository.setInvitations(listOf(invitation))

        val viewModel = createViewModel(token = "accepted-token")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AcceptInvitationUiState.Error)
            val error = state as AcceptInvitationUiState.Error
            assertTrue(error.message is UiText.Resource)
            assertEquals(R.string.accept_invitation_already_accepted, (error.message as UiText.Resource).resId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `accept creates member and updates invitation`() = runTest {
        val user = aUser(uid = "accepterUid")
        authRepository.setCurrentUser(user)
        val invitation = aInvitation(
            id = 1L,
            token = "accept-token",
            status = InvitationStatus.PENDING,
            careRecipientId = 1L,
            expiresAt = fakeClock.now().plusDays(7)
        )
        invitationRepository.setInvitations(listOf(invitation))

        val viewModel = createViewModel(token = "accept-token")

        viewModel.accept()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AcceptInvitationUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }

        // Verify member was created
        memberRepository.getAllMembers().test {
            val members = awaitItem()
            assertEquals(1, members.size)
            assertEquals("accepterUid", members[0].uid)
            assertEquals(1L, members[0].careRecipientId)
            cancelAndIgnoreRemainingEvents()
        }

        // Verify invitation status was updated
        invitationRepository.getAllInvitations().test {
            val invitations = awaitItem()
            assertEquals(1, invitations.size)
            assertEquals(InvitationStatus.ACCEPTED, invitations[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `accept shows success state`() = runTest {
        val user = aUser(uid = "accepterUid")
        authRepository.setCurrentUser(user)
        val invitation = aInvitation(
            id = 1L,
            token = "success-token",
            status = InvitationStatus.PENDING,
            expiresAt = fakeClock.now().plusDays(7)
        )
        invitationRepository.setInvitations(listOf(invitation))

        val viewModel = createViewModel(token = "success-token")

        viewModel.accept()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AcceptInvitationUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `accept shows error on repository failure`() = runTest {
        val user = aUser(uid = "accepterUid")
        authRepository.setCurrentUser(user)
        val invitation = aInvitation(
            id = 1L,
            token = "fail-token",
            status = InvitationStatus.PENDING,
            expiresAt = fakeClock.now().plusDays(7)
        )
        invitationRepository.setInvitations(listOf(invitation))
        memberRepository.shouldFail = true

        val viewModel = createViewModel(token = "fail-token")

        viewModel.snackbarController.events.test {
            viewModel.accept()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.accept_invitation_failed, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `decline updates invitation status to REJECTED`() = runTest {
        val invitation = aInvitation(
            id = 1L,
            token = "decline-token",
            status = InvitationStatus.PENDING,
            expiresAt = fakeClock.now().plusDays(7)
        )
        invitationRepository.setInvitations(listOf(invitation))

        val viewModel = createViewModel(token = "decline-token")

        viewModel.decline()

        invitationRepository.getAllInvitations().test {
            val invitations = awaitItem()
            assertEquals(1, invitations.size)
            assertEquals(InvitationStatus.REJECTED, invitations[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `accept requires logged-in user`() = runTest {
        // No user logged in
        val invitation = aInvitation(
            id = 1L,
            token = "no-user-token",
            status = InvitationStatus.PENDING,
            expiresAt = fakeClock.now().plusDays(7)
        )
        invitationRepository.setInvitations(listOf(invitation))

        val viewModel = createViewModel(token = "no-user-token")

        viewModel.snackbarController.events.test {
            viewModel.accept()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.accept_invitation_not_logged_in, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
