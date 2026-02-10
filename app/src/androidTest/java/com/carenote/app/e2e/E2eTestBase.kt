package com.carenote.app.e2e

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.carenote.app.data.local.CareNoteDatabase
import com.carenote.app.domain.model.User
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeSyncRepository
import com.carenote.app.fakes.FakeSyncWorkScheduler
import com.carenote.app.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Base class for E2E tests providing common rules and helpers.
 *
 * Subclasses must be annotated with:
 * - @HiltAndroidTest
 * - @RunWith(AndroidJUnit4::class)
 */
abstract class E2eTestBase {

    @Inject
    lateinit var database: CareNoteDatabase

    @Inject
    lateinit var fakeAuthRepository: FakeAuthRepository

    @Inject
    lateinit var fakeSyncRepository: FakeSyncRepository

    @Inject
    lateinit var fakeSyncWorkScheduler: FakeSyncWorkScheduler

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val grantPermissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    protected val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    open fun setUp() {
        hiltRule.inject()
        composeRule.waitForIdle()
    }

    @After
    open fun tearDown() {
        database.clearAllTables()
        fakeAuthRepository.clear()
        fakeSyncRepository.clear()
        fakeSyncWorkScheduler.clear()
        cleanupCacheDir("photos")
        cleanupCacheDir("exports")
    }

    private fun cleanupCacheDir(dirName: String) {
        val dir = File(context.cacheDir, dirName)
        if (dir.exists()) dir.deleteRecursively()
    }

    // --- Auth helpers ---

    /**
     * Create a test user for authentication tests.
     */
    protected fun createTestUser(
        uid: String = "test-uid",
        email: String = "test@example.com",
        name: String = "Test User",
        isPremium: Boolean = false
    ) = User(
        uid = uid,
        email = email,
        name = name,
        createdAt = LocalDateTime.now(),
        isPremium = isPremium
    )

    /**
     * Set the current user to simulate a logged-in state.
     */
    protected fun setLoggedInUser(user: User = createTestUser()) {
        fakeAuthRepository.setCurrentUser(user)
    }

    /**
     * Clear the current user to simulate a logged-out state.
     */
    protected fun setLoggedOut() {
        fakeAuthRepository.setCurrentUser(null)
    }

    // --- Navigation helpers ---

    protected fun navigateToTab(@StringRes tabLabelResId: Int) {
        val label = getString(tabLabelResId)
        composeRule.onNodeWithText(label).performClick()
        composeRule.waitForIdle()
    }

    protected fun clickFab(testTag: String) {
        composeRule.onNodeWithTag(testTag).performClick()
        composeRule.waitForIdle()
    }

    // --- Text field helpers ---

    /**
     * Fill a text field matched by its label text.
     * Uses merged semantics tree so that OutlinedTextField label text
     * and SetTextAction are on the same node.
     * Waits for the field to be available before interacting.
     */
    protected fun fillTextField(label: String, text: String, timeoutMs: Long = 5_000L) {
        val matcher = hasText(label) and hasSetTextAction()
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(matcher).performTextClearance()
        composeRule.onNode(matcher).performTextInput(text)
    }

    protected fun fillTextFieldByLabel(@StringRes labelResId: Int, text: String) {
        fillTextField(getString(labelResId), text)
    }

    // --- Assertion helpers ---

    /**
     * Assert that at least one node with the given text is displayed.
     * Handles cases where the same text appears in multiple places
     * (e.g., TopAppBar title and BottomNav label).
     */
    protected fun assertTextDisplayed(text: String) {
        composeRule.onAllNodesWithText(text).onFirst().assertIsDisplayed()
    }

    protected fun assertTextDisplayed(@StringRes resId: Int) {
        assertTextDisplayed(getString(resId))
    }

    protected fun assertTextNotExists(text: String) {
        composeRule.onNodeWithText(text).assertDoesNotExist()
    }

    protected fun waitForText(text: String, timeoutMs: Long = 5_000L) {
        composeRule.waitForText(text, timeoutMs)
    }

    protected fun waitForText(@StringRes resId: Int, timeoutMs: Long = 5_000L) {
        waitForText(getString(resId), timeoutMs)
    }

    // --- String resource ---

    protected fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    protected fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    // --- Click helpers ---

    protected fun clickText(text: String) {
        composeRule.onNodeWithText(text).performClick()
        composeRule.waitForIdle()
    }

    protected fun clickText(@StringRes resId: Int) {
        clickText(getString(resId))
    }

    /**
     * Scroll to a node with the given text and click it.
     * Useful for buttons at the bottom of scrollable forms.
     */
    protected fun scrollToAndClickText(text: String) {
        composeRule.onNodeWithText(text).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    protected fun scrollToAndClickText(@StringRes resId: Int) {
        scrollToAndClickText(getString(resId))
    }

    // --- Wait helpers ---

    protected fun waitForFab(testTag: String, timeoutMs: Long = 10_000L) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodes(hasTestTag(testTag))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
