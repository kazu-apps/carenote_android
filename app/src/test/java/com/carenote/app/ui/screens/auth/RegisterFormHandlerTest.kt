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
class RegisterFormHandlerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var syncWorkScheduler: FakeSyncWorkScheduler
    private lateinit var handler: RegisterFormHandler

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        syncWorkScheduler = FakeSyncWorkScheduler()
        handler = RegisterFormHandler(authRepository, syncWorkScheduler, scope = testScope)
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
        assertEquals("", state.displayName)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.displayNameError)
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
    fun `updateDisplayName updates state and clears error`() = runTest(testDispatcher) {
        handler.updateDisplayName("John Doe")
        advanceUntilIdle()

        val state = handler.formState.value
        assertEquals("John Doe", state.displayName)
        assertNull(state.displayNameError)
    }

    @Test
    fun `signUp with empty email sets error`() = runTest(testDispatcher) {
        handler.updatePassword("password123")
        handler.updateDisplayName("John Doe")
        handler.signUp()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.emailError)
        assertTrue(state.emailError is UiText.Resource)
        assertEquals(R.string.auth_email_required, (state.emailError as UiText.Resource).resId)
    }

    @Test
    fun `signUp with empty password sets error`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.updateDisplayName("John Doe")
        handler.signUp()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.passwordError)
        assertTrue(state.passwordError is UiText.Resource)
        assertEquals(R.string.auth_password_required, (state.passwordError as UiText.Resource).resId)
    }

    @Test
    fun `signUp with empty displayName sets error`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")
        handler.signUp()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.displayNameError)
        assertTrue(state.displayNameError is UiText.Resource)
        assertEquals(
            R.string.auth_display_name_required,
            (state.displayNameError as UiText.Resource).resId
        )
    }

    @Test
    fun `signUp with short password sets error`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.updatePassword("123")
        handler.updateDisplayName("John Doe")
        handler.signUp()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.passwordError)
        assertTrue(state.passwordError is UiText.ResourceWithArgs)
        assertEquals(
            R.string.auth_password_too_short,
            (state.passwordError as UiText.ResourceWithArgs).resId
        )
    }

    @Test
    fun `signUp success emits authSuccessEvent and shows snackbar`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")
        handler.updateDisplayName("John Doe")

        handler.snackbarController.events.test {
            handler.authSuccessEvent.test {
                handler.signUp()
                advanceUntilIdle()

                val authEvent = awaitItem()
                assertTrue(authEvent)
            }

            val snackbarEvent = awaitItem()
            assertTrue(snackbarEvent is SnackbarEvent.WithResId)
            assertEquals(
                R.string.auth_register_success,
                (snackbarEvent as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `signUp failure sets isLoading false`() = runTest(testDispatcher) {
        authRepository.shouldFail = true
        authRepository.failureError = DomainError.UnauthorizedError("Registration failed")

        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")
        handler.updateDisplayName("John Doe")
        handler.signUp()
        advanceUntilIdle()

        val state = handler.formState.value
        assertFalse(state.isLoading)
    }

    @Test
    fun `signUp with all empty fields sets all errors simultaneously`() = runTest(testDispatcher) {
        handler.signUp()
        advanceUntilIdle()

        val state = handler.formState.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertNotNull(state.displayNameError)
    }

    @Test
    fun `signUp with validation error shows message`() = runTest(testDispatcher) {
        authRepository.shouldFail = true
        authRepository.failureError = DomainError.ValidationError("Email already in use")

        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")
        handler.updateDisplayName("John Doe")

        handler.snackbarController.events.test {
            handler.signUp()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithString)
            assertEquals("Email already in use", (event as SnackbarEvent.WithString).message)
        }
    }

    @Test
    fun `resetState clears form`() = runTest(testDispatcher) {
        handler.updateEmail("test@example.com")
        handler.updatePassword("password123")
        handler.updateDisplayName("John Doe")
        advanceUntilIdle()

        handler.resetState()
        advanceUntilIdle()

        val state = handler.formState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.displayName)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.displayNameError)
        assertFalse(state.isLoading)
    }
}
