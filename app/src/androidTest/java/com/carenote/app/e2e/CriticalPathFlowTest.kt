package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import com.carenote.app.ui.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CriticalPathFlowTest : E2eTestBase() {

    private fun waitForAddMedicationForm() {
        val nameLabel = getString(R.string.medication_name)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(nameLabel) and hasSetTextAction())
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateToNotes() {
        navigateToTab(R.string.nav_notes)
        waitForFab(TestTags.NOTES_FAB)
    }

    private fun addMedication(name: String, dosage: String = "1 tablet") {
        waitForFab(TestTags.MEDICATION_FAB)
        clickFab(TestTags.MEDICATION_FAB)
        waitForAddMedicationForm()
        fillTextFieldByLabel(R.string.medication_name, name)
        fillTextFieldByLabel(R.string.medication_dosage, dosage)
        clickText(R.string.medication_morning)
        clickText(R.string.common_save)
        waitForText(name)
    }

    @Test
    fun test_medication_fullCrudFlow() {
        val medName = "CrudMed E2E"
        val updatedName = "CrudMed Updated E2E"

        // Add medication
        addMedication(medName)
        composeRule.onNodeWithText(medName).assertIsDisplayed()

        // Navigate to detail
        clickText(medName)
        waitForText(medName, 10_000L)
        assertTextDisplayed(medName)

        // Click edit icon on detail screen
        val editDesc = getString(R.string.common_edit)
        composeRule.onNodeWithContentDescription(editDesc).performClick()
        composeRule.waitForIdle()

        // Wait for edit screen and update name
        waitForText(R.string.medication_edit)
        fillTextFieldByLabel(R.string.medication_name, updatedName)
        clickText(R.string.common_save)

        // Back on detail screen — verify updated name
        waitForText(updatedName, 10_000L)
        assertTextDisplayed(updatedName)

        // Delete from detail screen
        val deleteDesc = getString(R.string.common_delete)
        composeRule.onNodeWithContentDescription(deleteDesc).performClick()
        composeRule.waitForIdle()

        // Confirm deletion
        waitForText(R.string.ui_confirm_delete_title)
        clickText(R.string.ui_confirm_yes)

        // Back on list — verify medication is removed
        waitForFab(TestTags.MEDICATION_FAB, 10_000L)
        assertTextNotExists(updatedName)
    }

    @Test
    fun test_note_fullCrudFlow() {
        navigateToNotes()

        val noteTitle = "CrudNote E2E"
        val updatedTitle = "CrudNote Updated E2E"

        // Add note
        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)
        fillTextFieldByLabel(R.string.notes_title_label, noteTitle)
        fillTextFieldByLabel(R.string.notes_content_label, "Content for CRUD test")
        clickText(R.string.common_save)
        waitForText(noteTitle)
        composeRule.onNodeWithText(noteTitle).assertIsDisplayed()

        // Click note to edit (direct navigation to edit screen)
        clickText(noteTitle)
        waitForText(R.string.notes_edit)

        // Update title
        fillTextFieldByLabel(R.string.notes_title_label, updatedTitle)
        clickText(R.string.common_save)

        // Verify updated title in list
        waitForText(updatedTitle)
        composeRule.onNodeWithText(updatedTitle).assertIsDisplayed()

        // Delete via swipe-to-dismiss
        composeRule.onNodeWithText(updatedTitle).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        // Confirm deletion
        waitForText(R.string.ui_confirm_delete_title, 5_000L)
        clickText(R.string.ui_confirm_yes)

        // Verify note is removed
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(updatedTitle))
                .fetchSemanticsNodes().isEmpty()
        }
        assertTextNotExists(updatedTitle)
    }

    @Test
    fun test_loginToCrud_integratedFlow() {
        // Start logged out
        setLoggedOut()
        composeRule.activityRule.scenario.onActivity { it.recreate() }

        // Wait for login screen
        val loginText = getString(R.string.auth_sign_in)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(loginText))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Login
        val emailLabel = getString(R.string.auth_email)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(emailLabel) and hasSetTextAction())
                .fetchSemanticsNodes().isNotEmpty()
        }
        fillTextField(getString(R.string.auth_email), "test@example.com")
        fillTextField(getString(R.string.auth_password), "password123")
        clickText(R.string.auth_sign_in)

        // Wait for home screen
        waitForText(R.string.medication_title, 10_000L)

        // Add medication
        val medName = "LoginCrud E2E"
        addMedication(medName)
        composeRule.onNodeWithText(medName).assertIsDisplayed()

        // Navigate to Notes tab and back
        navigateToNotes()
        navigateToTab(R.string.nav_medication)

        // Verify medication persists after navigation
        waitForText(medName, 5_000L)
        composeRule.onNodeWithText(medName).assertIsDisplayed()
    }
}
