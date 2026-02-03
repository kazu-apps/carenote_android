package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
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
class TasksFlowTest : E2eTestBase() {

    private fun navigateToTasks() {
        navigateToTab(R.string.nav_tasks)
        waitForFab(TestTags.TASKS_FAB)
    }

    @Test
    fun test_addTask_showsInList() {
        navigateToTasks()

        // Tap FAB to add task
        clickFab(TestTags.TASKS_FAB)
        waitForText(R.string.tasks_add)

        // Fill in title
        fillTextFieldByLabel(R.string.tasks_task_title, "TestTask E2E")

        // Fill in description
        fillTextFieldByLabel(R.string.tasks_task_description, "E2E test task description")

        // Select high priority
        clickText(R.string.tasks_task_priority_high)

        // Save
        clickText(R.string.common_save)

        // Verify task appears in list
        waitForText("TestTask E2E")
        composeRule.onNodeWithText("TestTask E2E").assertIsDisplayed()
    }

    @Test
    fun test_toggleTaskCompletion() {
        navigateToTasks()

        // First add a task
        clickFab(TestTags.TASKS_FAB)
        waitForText(R.string.tasks_add)
        fillTextFieldByLabel(R.string.tasks_task_title, "ToggleTask E2E")
        clickText(R.string.common_save)

        // Wait for task in list
        waitForText("ToggleTask E2E")

        // Find the checkbox inside the task card (ancestor CareNoteCard has merged text)
        composeRule.onNode(
            isToggleable() and hasAnyAncestor(hasText("ToggleTask E2E"))
        ).performClick()
        composeRule.waitForIdle()

        // Switch to completed filter to verify
        clickText(R.string.tasks_filter_completed)
        composeRule.waitForIdle()

        // The task should appear in the completed tab
        waitForText("ToggleTask E2E", 3_000L)
    }

    @Test
    fun test_taskFiltering_showsCorrectTasks() {
        navigateToTasks()

        // Add a task
        clickFab(TestTags.TASKS_FAB)
        waitForText(R.string.tasks_add)
        fillTextFieldByLabel(R.string.tasks_task_title, "FilterTask E2E")
        clickText(R.string.common_save)
        waitForText("FilterTask E2E")

        // Verify "All" filter shows the task
        clickText(R.string.tasks_filter_all)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("FilterTask E2E").assertIsDisplayed()

        // Verify "Incomplete" filter shows the task (new tasks are incomplete)
        clickText(R.string.tasks_filter_incomplete)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("FilterTask E2E").assertIsDisplayed()

        // Verify "Completed" filter does not show the task
        clickText(R.string.tasks_filter_completed)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("FilterTask E2E").assertDoesNotExist()
    }
}
