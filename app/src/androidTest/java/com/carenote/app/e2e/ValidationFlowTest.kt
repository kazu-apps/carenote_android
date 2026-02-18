package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ValidationFlowTest : E2eTestBase() {

    private fun navigateToNotes() {
        navigateToTab(R.string.nav_notes)
        waitForFab(TestTags.NOTES_FAB)
    }

    private fun navigateToCalendar() {
        navigateToTab(R.string.nav_calendar)
        waitForFab(TestTags.CALENDAR_FAB)
    }

    private fun navigateToHealthRecords() {
        navigateToTab(R.string.nav_health_records)
        waitForFab(TestTags.HEALTH_RECORDS_FAB)
    }

    private fun waitForAddMedicationForm() {
        val nameLabel = getString(R.string.medication_name)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(nameLabel) and hasSetTextAction())
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForError(errorText: String) {
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(errorText))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(errorText).performScrollTo()
        composeRule.onNodeWithText(errorText).assertIsDisplayed()
    }

    @Test
    fun test_note_emptyTitle_showsError() {
        navigateToNotes()

        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)

        // Fill content but leave title empty
        fillTextFieldByLabel(R.string.notes_content_label, "Content without title")
        scrollToAndClickText(R.string.common_save)

        val errorText = getString(R.string.notes_title_required)
        waitForError(errorText)
    }

    @Test
    fun test_note_emptyContent_showsError() {
        navigateToNotes()

        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)

        // Fill title but leave content empty
        fillTextFieldByLabel(R.string.notes_title_label, "Title without content")
        scrollToAndClickText(R.string.common_save)

        val errorText = getString(R.string.notes_content_required)
        waitForError(errorText)
    }

    @Test
    fun test_calendarEvent_emptyTitle_showsError() {
        navigateToCalendar()

        clickFab(TestTags.CALENDAR_FAB)
        waitForText(R.string.calendar_add_event)

        // Try to save without filling title
        scrollToAndClickText(R.string.common_save)

        val errorText = getString(R.string.calendar_event_title_required)
        waitForError(errorText)
    }

    @Test
    fun test_medication_nameOnly_savesWithoutTiming() {
        // Verify medication can be saved with name only (timing is optional)
        waitForFab(TestTags.MEDICATION_FAB)
        clickFab(TestTags.MEDICATION_FAB)
        waitForAddMedicationForm()

        fillTextFieldByLabel(R.string.medication_name, "NameOnly E2E")
        scrollToAndClickText(R.string.common_save)

        // Medication saves successfully — timing is not required
        waitForText("NameOnly E2E")
        composeRule.onNodeWithText("NameOnly E2E").assertIsDisplayed()
    }

    @Test
    fun test_healthRecord_extremeTemperature_showsError() {
        navigateToHealthRecords()

        clickFab(TestTags.HEALTH_RECORDS_FAB)
        waitForText(R.string.health_records_add)

        // Enter temperature above max (42.0)
        fillTextFieldByLabel(R.string.health_records_temperature, "50.0")
        scrollToAndClickText(R.string.common_save)

        val errorText = getString(
            R.string.health_records_temperature_range_error,
            AppConfig.HealthRecord.TEMPERATURE_MIN,
            AppConfig.HealthRecord.TEMPERATURE_MAX
        )
        waitForError(errorText)
    }
}
