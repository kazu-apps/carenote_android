package com.carenote.app.ui.screens.auth

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ForgotPasswordFormHandlerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var handler: ForgotPasswordFormHandler

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        handler = ForgotPasswordFormHandler(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        authRepository.clear()
    }

    @Test
    fun `initial state is empty`() = runTest(testDispatcher) {
        val state = handler.formState.value

        assertEquals("", state.email)
        assertNull(state.emailError)
        assertFalse(state.isLoading)
        assertFalse(state.emailSent)
    }

    @Test
    fun `updateEmail updates state and clears error`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        advanceUntilIdle()

        val state = handler.formState.value
        assertEquals("test@example.com", state.email)
        assertNull(state.emailError)
    }

    @Test
    fun `sendPasswordResetEmail with empty email sets error`() = runTest(testDispatcher) {
        handler.sendPasswordResetEmail()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.emailError)
        assertTrue(state.emailError is UiText.Resource)
        assertEquals(R.string.auth_email_required, (state.emailError as UiText.Resource).resId)
    }

    @Test
    fun `sendPasswordResetEmail with invalid email sets error`() = runTest(testDispatcher) {
        handler.updateEmail("invalid-email")
        handler.sendPasswordResetEmail()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.emailError)
        assertTrue(state.emailError is UiText.Resource)
        assertEquals(R.string.auth_email_invalid, (state.emailError as UiText.Resource).resId)
    }

    @Test
    fun `sendPasswordResetEmail success sets emailSent true`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.sendPasswordResetEmail()
        advanceUntilIdle()

        val state = handler.formState.value
        assertTrue(state.emailSent)
        assertFalse(state.isLoading)
    }

    @Test
    fun `sendPasswordResetEmail success shows snackbar`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")

        handler.snackbarController.events.test {
            handler.sendPasswordResetEmail()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.auth_password_reset_sent,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `sendPasswordResetEmail failure shows snackbar`() = runTest(testDispatcher) {
        authRepository.shouldFail = true
        authRepository.failureError = DomainError.NetworkError("Network error")

        handler.updateEmail("test@example.com")

        handler.snackbarController.events.test {
            handler.sendPasswordResetEmail()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.ui_error_network, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `sendPasswordResetEmail failure sets isLoading false`() = runTest(testDispatcher) {
        authRepository.shouldFail = true
        authRepository.failureError = DomainError.UnauthorizedError("User not found")

        handler.updateEmail("test@example.com")
        handler.sendPasswordResetEmail()
        advanceUntilIdle()

        val state = handler.formState.value
        assertFalse(state.isLoading)
        assertFalse(state.emailSent)
    }

    @Test
    fun `resetState clears form`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        advanceUntilIdle()

        handler.resetState()
        advanceUntilIdle()

        val state = handler.formState.value
        assertEquals("", state.email)
        assertNull(state.emailError)
        assertFalse(state.isLoading)
        assertFalse(state.emailSent)
    }
}
