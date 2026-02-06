package com.carenote.app.ui.screens.auth

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeSyncWorkScheduler
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
class LoginFormHandlerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var syncWorkScheduler: FakeSyncWorkScheduler
    private lateinit var handler: LoginFormHandler

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        syncWorkScheduler = FakeSyncWorkScheduler()
        handler = LoginFormHandler(authRepository, syncWorkScheduler, scope = testScope)
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
        assertEquals("", state.password)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertFalse(state.isLoading)
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
    fun `updatePassword updates state and clears error`() = runTest(testDispatcher) {
        handler.updatePassword("password123")
        advanceUntilIdle()

        val state = handler.formState.value
        assertEquals("password123", state.password)
        assertNull(state.passwordError)
    }

    @Test
    fun `signIn with empty email sets error`() = runTest(testDispatcher) {
        handler.updatePassword("password123")
        handler.signIn()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.emailError)
        assertTrue(state.emailError is UiText.Resource)
        assertEquals(R.string.auth_email_required, (state.emailError as UiText.Resource).resId)
    }

    @Test
    fun `signIn with invalid email sets error`() = runTest(testDispatcher) {
        handler.updateEmail("invalid-email")
        handler.updatePassword("password123")
        handler.signIn()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.emailError)
        assertTrue(state.emailError is UiText.Resource)
        assertEquals(R.string.auth_email_invalid, (state.emailError as UiText.Resource).resId)
    }

    @Test
    fun `signIn with empty password sets error`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.signIn()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.passwordError)
        assertTrue(state.passwordError is UiText.Resource)
        assertEquals(R.string.auth_password_required, (state.passwordError as UiText.Resource).resId)
    }

    @Test
    fun `signIn success emits authSuccessEvent`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")

        handler.authSuccessEvent.test {
            handler.signIn()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event)
        }
    }

    @Test
    fun `signIn success with unverified email shows snackbar warning`() = runTest(testDispatcher) {
        authRepository.isEmailVerified = false
        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")

        handler.snackbarController.events.test {
            handler.signIn()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.auth_email_not_verified,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `signIn failure sets isLoading false`() = runTest(testDispatcher) {
        authRepository.shouldFail = true
        authRepository.failureError = DomainError.UnauthorizedError("Invalid credentials")

        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")
        handler.signIn()
        advanceUntilIdle()

        val state = handler.formState.value
        assertFalse(state.isLoading)
    }

    @Test
    fun `signIn with network error shows snackbar`() = runTest(testDispatcher) {
        authRepository.shouldFail = true
        authRepository.failureError = DomainError.NetworkError("No internet connection")

        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")

        handler.snackbarController.events.test {
            handler.signIn()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.ui_error_network, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `signIn with unauthorized error shows snackbar`() = runTest(testDispatcher) {
        authRepository.shouldFail = true
        authRepository.failureError = DomainError.UnauthorizedError("Invalid credentials")

        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")

        handler.snackbarController.events.test {
            handler.signIn()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithString)
        }
    }

    @Test
    fun `resetState clears form`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")
        advanceUntilIdle()

        handler.resetState()
        advanceUntilIdle()

        val state = handler.formState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertFalse(state.isLoading)
    }
}
