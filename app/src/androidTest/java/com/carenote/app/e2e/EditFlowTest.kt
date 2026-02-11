package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import com.carenote.app.ui.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EditFlowTest : E2eTestBase() {

    private fun waitForAddMedicationForm() {
        val nameLabel = getString(R.string.medication_name)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(nameLabel) and hasSetTextAction())
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateToCalendar() {
        navigateToTab(R.string.nav_calendar)
        waitForFab(TestTags.CALENDAR_FAB)
    }

    private fun navigateToTasks() {
        navigateToTab(R.string.nav_tasks)
        waitForFab(TestTags.TASKS_FAB)
    }

    private fun navigateToHealthRecords() {
        navigateToTab(R.string.nav_health_records)
        waitForFab(TestTags.HEALTH_RECORDS_FAB)
    }

    @Test
    fun test_editMedication_updatesName() {
        val originalName = "EditMed E2E"
        val updatedName = "EditMed Updated E2E"

        // Add medication
        waitForFab(TestTags.MEDICATION_FAB)
        clickFab(TestTags.MEDICATION_FAB)
        waitForAddMedicationForm()
        fillTextFieldByLabel(R.string.medication_name, originalName)
        clickText(R.string.medication_morning)
        clickText(R.string.common_save)
        waitForText(originalName)

        // Navigate to detail screen
        clickText(originalName)
        waitForText(originalName, 10_000L)

        // Click edit icon
        val editDesc = getString(R.string.common_edit)
        composeRule.onNodeWithContentDescription(editDesc).performClick()
        composeRule.waitForIdle()

        // Wait for edit screen
        waitForText(R.string.medication_edit)

        // Update name
        fillTextFieldByLabel(R.string.medication_name, updatedName)
        clickText(R.string.common_save)

        // Verify updated name on detail screen
        waitForText(updatedName, 10_000L)
        assertTextDisplayed(updatedName)
    }

    @Test
    fun test_editCalendarEvent_updatesTitle() {
        navigateToCalendar()

        val originalTitle = "EditEvent E2E"
        val updatedTitle = "EditEvent Updated E2E"

        // Add event
        clickFab(TestTags.CALENDAR_FAB)
        waitForText(R.string.calendar_add_event)
        fillTextFieldByLabel(R.string.calendar_event_title, originalTitle)
        clickText(R.string.common_save)
        waitForText(originalTitle)

        // Click event to edit (direct navigation to edit screen)
        clickText(originalTitle)
        waitForText(R.string.calendar_edit_event)

        // Update title
        fillTextFieldByLabel(R.string.calendar_event_title, updatedTitle)
        clickText(R.string.common_save)

        // Verify updated title in list
        waitForText(updatedTitle)
        composeRule.onNodeWithText(updatedTitle).assertIsDisplayed()
    }

    @Test
    fun test_editTask_updatesTitle() {
        navigateToTasks()

        val originalTitle = "EditTask E2E"
        val updatedTitle = "EditTask Updated E2E"

        // Add task
        clickFab(TestTags.TASKS_FAB)
        waitForText(R.string.tasks_add)
        fillTextFieldByLabel(R.string.tasks_task_title, originalTitle)
        clickText(R.string.common_save)
        waitForText(originalTitle)

        // Click task to edit (direct navigation to edit screen)
        clickText(originalTitle)
        waitForText(R.string.tasks_edit)

        // Update title
        fillTextFieldByLabel(R.string.tasks_task_title, updatedTitle)
        clickText(R.string.common_save)

        // Verify updated title in list
        waitForText(updatedTitle)
        composeRule.onNodeWithText(updatedTitle).assertIsDisplayed()
    }

    @Test
    fun test_editHealthRecord_updatesTemperature() {
        navigateToHealthRecords()

        // Add health record with initial temperature
        clickFab(TestTags.HEALTH_RECORDS_FAB)
        waitForText(R.string.health_records_add)
        fillTextFieldByLabel(R.string.health_records_temperature, "36.5")
        clickText(R.string.common_save)
        waitForText("36.5")

        // Click record to edit (direct navigation to edit screen)
        composeRule.onNodeWithText("36.5", substring = true).performClick()
        composeRule.waitForIdle()
        waitForText(R.string.health_records_edit)

        // Update temperature
        fillTextFieldByLabel(R.string.health_records_temperature, "37.2")
        clickText(R.string.common_save)

        // Verify updated temperature in list
        waitForText("37.2")
        composeRule.onNodeWithText("37.2", substring = true).assertIsDisplayed()
    }
}
