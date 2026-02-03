package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import com.carenote.app.ui.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NotesFlowTest : E2eTestBase() {

    private fun navigateToNotes() {
        navigateToTab(R.string.nav_notes)
        waitForFab(TestTags.NOTES_FAB)
    }

    @Test
    fun test_addNote_showsInList() {
        navigateToNotes()

        // Tap FAB to add note
        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)

        // Fill in title
        fillTextFieldByLabel(R.string.notes_title_label, "TestNote E2E")

        // Fill in content
        fillTextFieldByLabel(R.string.notes_content_label, "Test note content for E2E testing")

        // Select a tag (Condition)
        clickText(R.string.notes_tag_condition)

        // Save
        clickText(R.string.common_save)

        // Verify note appears in list
        waitForText("TestNote E2E")
        composeRule.onNodeWithText("TestNote E2E").assertIsDisplayed()
    }

    @Test
    fun test_editNote_updatesContent() {
        navigateToNotes()

        // First add a note
        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)
        fillTextFieldByLabel(R.string.notes_title_label, "EditNote E2E")
        fillTextFieldByLabel(R.string.notes_content_label, "Original content")
        clickText(R.string.common_save)

        // Wait for note in list, then click to edit
        waitForText("EditNote E2E")
        clickText("EditNote E2E")

        // Wait for edit screen
        waitForText(R.string.notes_edit)

        // Update the title
        fillTextFieldByLabel(R.string.notes_title_label, "EditNote Updated E2E")

        // Save
        clickText(R.string.common_save)

        // Verify updated note in list
        waitForText("EditNote Updated E2E")
        composeRule.onNodeWithText("EditNote Updated E2E").assertIsDisplayed()
    }

    @Test
    fun test_noteSearch_filtersResults() {
        navigateToNotes()

        // Add first note
        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)
        fillTextFieldByLabel(R.string.notes_title_label, "Apple Note")
        fillTextFieldByLabel(R.string.notes_content_label, "Content about apple")
        clickText(R.string.common_save)
        waitForText("Apple Note")

        // Add second note
        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)
        fillTextFieldByLabel(R.string.notes_title_label, "Banana Note")
        fillTextFieldByLabel(R.string.notes_content_label, "Content about banana")
        clickText(R.string.common_save)
        waitForText("Banana Note")

        // Type search query into the search field (only text field on Notes list)
        composeRule.onNode(hasSetTextAction()).performTextInput("Apple")
        composeRule.waitForIdle()

        // Wait for filtering to take effect (Banana Note should disappear)
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText("Banana Note"))
                .fetchSemanticsNodes().isEmpty()
        }

        // Verify filtering
        composeRule.onNodeWithText("Apple Note").assertIsDisplayed()
        composeRule.onNodeWithText("Banana Note").assertDoesNotExist()
    }
}
