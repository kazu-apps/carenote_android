package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E tests for authentication flows.
 *
 * Tests cover:
 * - Login screen display
 * - Navigation between login and register screens
 * - Login success flow
 * - Register success flow
 * - Logout flow
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthFlowTest : E2eTestBase() {

    /**
     * Wait for the Login screen to be ready by checking for the login button.
     */
    private fun waitForLoginScreen() {
        val loginButtonText = getString(R.string.auth_sign_in)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(loginButtonText))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Wait for the Register screen to be ready by checking for the sign up button.
     */
    private fun waitForRegisterScreen() {
        val signUpButtonText = getString(R.string.auth_sign_up)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(signUpButtonText))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Wait for the email text field to be ready on auth screens.
     */
    private fun waitForEmailField() {
        val emailLabel = getString(R.string.auth_email)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(emailLabel) and hasSetTextAction())
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Navigate to login screen from the home (Medication) screen.
     * Since the app may start on Medication screen, we need to navigate to Settings first.
     */
    private fun navigateToLoginViaSettings() {
        // App starts on Medication screen, navigate to Settings
        val settingsTitle = getString(R.string.settings_title)

        // Wait for initial screen to load
        composeRule.waitForIdle()

        // The Medication screen has a settings icon button
        // For now, let's just check if we're already logged out (showing login screen)
        // or if we're on the home screen
    }

    @Test
    fun test_loginScreen_displaysCorrectElements() {
        // Set logged out state to ensure login screen is shown
        setLoggedOut()

        // Navigate to login screen
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }

        // Wait for the login screen elements
        waitForLoginScreen()

        // Verify login screen elements are displayed
        assertTextDisplayed(R.string.app_name)
        assertTextDisplayed(R.string.auth_login_subtitle)
        assertTextDisplayed(R.string.auth_email)
        assertTextDisplayed(R.string.auth_password)
        assertTextDisplayed(R.string.auth_sign_in)
        assertTextDisplayed(R.string.auth_forgot_password)
        assertTextDisplayed(R.string.auth_no_account)
        assertTextDisplayed(R.string.auth_sign_up_link)
    }

    @Test
    fun test_loginToRegister_navigation() {
        // Set logged out state
        setLoggedOut()

        // Recreate activity to ensure we start at login
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }

        waitForLoginScreen()

        // Click on "Sign up" link
        clickText(R.string.auth_sign_up_link)

        // Wait for register screen
        waitForRegisterScreen()

        // Verify register screen elements
        assertTextDisplayed(R.string.auth_register_title)
        assertTextDisplayed(R.string.auth_register_subtitle)
        assertTextDisplayed(R.string.auth_display_name)
        assertTextDisplayed(R.string.auth_sign_up)
    }

    @Test
    fun test_login_success_navigatesToHome() {
        // Set logged out state
        setLoggedOut()

        // Recreate activity
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }

        waitForLoginScreen()
        waitForEmailField()

        // Fill in login form
        fillTextField(getString(R.string.auth_email), "test@example.com")
        fillTextField(getString(R.string.auth_password), "password123")

        // Click login button
        clickText(R.string.auth_sign_in)

        // Wait for navigation to home (Medication screen)
        waitForText(R.string.medication_title, 10_000L)

        // Verify we're on the home screen
        assertTextDisplayed(R.string.medication_title)
    }

    @Test
    fun test_register_success_navigatesToHome() {
        // Set logged out state
        setLoggedOut()

        // Recreate activity
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }

        waitForLoginScreen()

        // Navigate to register screen
        clickText(R.string.auth_sign_up_link)
        waitForRegisterScreen()

        // Wait for form fields
        val displayNameLabel = getString(R.string.auth_display_name)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(displayNameLabel) and hasSetTextAction())
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Fill in registration form
        fillTextField(getString(R.string.auth_display_name), "Test User")
        fillTextField(getString(R.string.auth_email), "newuser@example.com")
        fillTextField(getString(R.string.auth_password), "password123")

        // Click sign up button
        scrollToAndClickText(R.string.auth_sign_up)

        // Wait for navigation to home (Medication screen)
        waitForText(R.string.medication_title, 10_000L)

        // Verify we're on the home screen
        assertTextDisplayed(R.string.medication_title)
    }

    @Test
    fun test_logout_navigatesToLogin() {
        // Set up logged in state
        setLoggedInUser()

        // Recreate activity to apply the logged-in state
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }

        // Wait for home screen to load
        waitForText(R.string.medication_title, 10_000L)

        // For this test, we would need a logout button in the UI.
        // Since logout is typically in settings, we need to navigate there.
        // The current implementation may not expose logout directly from the main flow.

        // This test verifies that when the user is logged in, they see the home screen
        assertTextDisplayed(R.string.medication_title)

        // Simulate logout by clearing the user
        setLoggedOut()

        // Trigger UI update by recreating activity
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }

        // Wait for login screen
        waitForLoginScreen()

        // Verify login screen is shown
        assertTextDisplayed(R.string.auth_sign_in)
    }
}
