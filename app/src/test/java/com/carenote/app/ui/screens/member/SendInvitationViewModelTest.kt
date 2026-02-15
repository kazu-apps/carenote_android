package com.carenote.app.ui.screens.member

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.fakes.FakeActiveCareRecipientProvider
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.fakes.FakeInvitationRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.TestDataFixtures
import com.carenote.app.testing.aInvitation
import com.carenote.app.testing.aUser
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SendInvitationViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private lateinit var invitationRepository: FakeInvitationRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var activeCareRecipientProvider: FakeActiveCareRecipientProvider
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private val fakeClock = FakeClock()

    @Before
    fun setUp() {
        invitationRepository = FakeInvitationRepository()
        authRepository = FakeAuthRepository()
        activeCareRecipientProvider = FakeActiveCareRecipientProvider()
        analyticsRepository = FakeAnalyticsRepository()
    }

    private fun createViewModel(): SendInvitationViewModel {
        return SendInvitationViewModel(
            invitationRepository,
            authRepository,
            activeCareRecipientProvider,
            analyticsRepository,
            fakeClock
        )
    }

    @Test
    fun `initial form state is empty`() {
        val viewModel = createViewModel()
        val state = viewModel.formState.value

        assertEquals("", state.email)
        assertEquals("", state.message)
        assertNull(state.emailError)
        assertFalse(state.isSending)
    }

    @Test
    fun `updateEmail updates form state`() {
        val viewModel = createViewModel()
        viewModel.updateEmail("test@example.com")
        assertEquals("test@example.com", viewModel.formState.value.email)
    }

    @Test
    fun `updateMessage updates form state`() {
        val viewModel = createViewModel()
        viewModel.updateMessage("一緒にケア記録を管理しましょう")
        assertEquals("一緒にケア記録を管理しましょう", viewModel.formState.value.message)
    }

    @Test
    fun `send with empty email shows error`() {
        val viewModel = createViewModel()
        viewModel.send()

        val error = viewModel.formState.value.emailError
        assertNotNull(error)
        assertTrue(error is UiText.Resource)
        assertEquals(R.string.send_invitation_email_required, (error as UiText.Resource).resId)
    }

    @Test
    fun `send with invalid email shows error`() {
        val viewModel = createViewModel()
        viewModel.updateEmail("invalid-email")
        viewModel.send()

        val error = viewModel.formState.value.emailError
        assertNotNull(error)
        assertTrue(error is UiText.Resource)
        assertEquals(R.string.send_invitation_email_invalid, (error as UiText.Resource).resId)
    }

    @Test
    fun `send with valid email creates invitation`() = runTest {
        val user = aUser(uid = "senderUid", email = "sender@example.com")
        authRepository.setCurrentUser(user)
        val viewModel = createViewModel()
        viewModel.updateEmail("invitee@example.com")

        viewModel.savedEvent.test {
            viewModel.send()
            val result = awaitItem()
            assertNotNull(result.token)
            assertTrue(result.inviteLink.contains(result.token))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send with duplicate pending invitation shows error`() = runTest {
        val user = aUser(uid = "senderUid", email = "sender@example.com")
        authRepository.setCurrentUser(user)
        // Pre-seed a PENDING invitation for the same email
        val existingInvitation = aInvitation(
            id = 1L,
            inviteeEmail = "invitee@example.com",
            status = InvitationStatus.PENDING,
            expiresAt = fakeClock.now().plusDays(7)
        )
        invitationRepository.setInvitations(listOf(existingInvitation))

        val viewModel = createViewModel()
        viewModel.updateEmail("invitee@example.com")
        viewModel.send()

        val error = viewModel.formState.value.emailError
        assertNotNull(error)
        assertTrue(error is UiText.Resource)
        assertEquals(R.string.send_invitation_duplicate_error, (error as UiText.Resource).resId)
    }

    @Test
    fun `send sets isSending during operation`() = runTest {
        val user = aUser(uid = "senderUid", email = "sender@example.com")
        authRepository.setCurrentUser(user)
        invitationRepository.shouldFail = true
        val viewModel = createViewModel()
        viewModel.updateEmail("invitee@example.com")

        viewModel.send()

        // After failure, isSending should be reset
        assertFalse(viewModel.formState.value.isSending)
    }

    @Test
    fun `send generates token of correct length`() = runTest {
        val user = aUser(uid = "senderUid", email = "sender@example.com")
        authRepository.setCurrentUser(user)
        val viewModel = createViewModel()
        viewModel.updateEmail("invitee@example.com")

        viewModel.savedEvent.test {
            viewModel.send()
            val result = awaitItem()
            assertEquals(AppConfig.Member.INVITATION_TOKEN_LENGTH, result.token.length)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send calculates correct expiration date`() = runTest {
        val user = aUser(uid = "senderUid", email = "sender@example.com")
        authRepository.setCurrentUser(user)
        val viewModel = createViewModel()
        viewModel.updateEmail("invitee@example.com")

        viewModel.savedEvent.test {
            viewModel.send()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Verify the invitation was created with proper expiration
        invitationRepository.getAllInvitations().test {
            val invitations = awaitItem()
            assertEquals(1, invitations.size)
            val expectedExpiry = fakeClock.now().plusDays(AppConfig.Member.INVITATION_VALID_DAYS)
            assertEquals(expectedExpiry, invitations[0].expiresAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send shows success via savedEvent`() = runTest {
        val user = aUser(uid = "senderUid", email = "sender@example.com")
        authRepository.setCurrentUser(user)
        val viewModel = createViewModel()
        viewModel.updateEmail("invitee@example.com")

        viewModel.savedEvent.test {
            viewModel.send()
            val result = awaitItem()
            assertNotNull(result)
            assertTrue(result.inviteLink.startsWith("https://"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send shows error on repository failure`() = runTest {
        val user = aUser(uid = "senderUid", email = "sender@example.com")
        authRepository.setCurrentUser(user)
        invitationRepository.shouldFail = true
        val viewModel = createViewModel()
        viewModel.updateEmail("invitee@example.com")

        viewModel.snackbarController.events.test {
            viewModel.send()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.send_invitation_failed, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isDirty is false initially`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty is true after email change`() {
        val viewModel = createViewModel()
        viewModel.updateEmail("changed@example.com")
        assertTrue(viewModel.isDirty)
    }
}
