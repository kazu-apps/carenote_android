package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
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
class DeleteFlowTest : E2eTestBase() {

    private fun navigateToNotes() {
        navigateToTab(R.string.nav_notes)
        waitForFab(TestTags.NOTES_FAB)
    }

    private fun navigateToCalendar() {
        navigateToTab(R.string.nav_calendar)
        waitForFab(TestTags.CALENDAR_FAB)
    }

    private fun addNote(title: String, content: String = "Delete test content") {
        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)
        fillTextFieldByLabel(R.string.notes_title_label, title)
        fillTextFieldByLabel(R.string.notes_content_label, content)
        clickText(R.string.common_save)
        waitForText(title)
    }

    private fun swipeToDeleteAndConfirm(itemText: String) {
        composeRule.onNodeWithText(itemText).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        waitForText(R.string.ui_confirm_delete_title, 5_000L)
        clickText(R.string.ui_confirm_yes)
    }

    private fun waitForItemRemoved(itemText: String) {
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(itemText))
                .fetchSemanticsNodes().isEmpty()
        }
        assertTextNotExists(itemText)
    }

    @Test
    fun test_deleteNote_removesFromList() {
        navigateToNotes()

        val noteTitle = "DeleteNote E2E"
        addNote(noteTitle)
        composeRule.onNodeWithText(noteTitle).assertIsDisplayed()

        // Swipe to delete and confirm
        swipeToDeleteAndConfirm(noteTitle)

        // Verify note is removed
        waitForItemRemoved(noteTitle)
    }

    @Test
    fun test_deleteCalendarEvent_removesFromList() {
        navigateToCalendar()

        val eventTitle = "DeleteEvent E2E"

        // Add event
        clickFab(TestTags.CALENDAR_FAB)
        waitForText(R.string.calendar_add_event)
        fillTextFieldByLabel(R.string.calendar_event_title, eventTitle)
        clickText(R.string.common_save)
        waitForText(eventTitle)
        composeRule.onNodeWithText(eventTitle).assertIsDisplayed()

        // Swipe to delete and confirm
        swipeToDeleteAndConfirm(eventTitle)

        // Verify event is removed
        waitForItemRemoved(eventTitle)
    }

    @Test
    fun test_deleteConfirmDialog_cancelPreservesItem() {
        navigateToNotes()

        val noteTitle = "CancelDelete E2E"
        addNote(noteTitle)
        composeRule.onNodeWithText(noteTitle).assertIsDisplayed()

        // Swipe to trigger delete dialog
        composeRule.onNodeWithText(noteTitle).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        // Wait for confirm dialog and cancel
        waitForText(R.string.ui_confirm_delete_title, 5_000L)
        clickText(R.string.ui_confirm_no)
        composeRule.waitForIdle()

        // Verify note is still present
        waitForText(noteTitle, 5_000L)
        composeRule.onNodeWithText(noteTitle).assertIsDisplayed()
    }
}
