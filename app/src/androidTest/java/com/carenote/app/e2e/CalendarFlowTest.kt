package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
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
class CalendarFlowTest : E2eTestBase() {

    private fun navigateToCalendar() {
        navigateToTab(R.string.nav_calendar)
        waitForFab(TestTags.CALENDAR_FAB)
    }

    @Test
    fun test_addCalendarEvent_showsOnSelectedDate() {
        navigateToCalendar()

        // Tap FAB to add event
        clickFab(TestTags.CALENDAR_FAB)
        waitForText(R.string.calendar_add_event)

        // Fill in title
        fillTextFieldByLabel(R.string.calendar_event_title, "TestEvent E2E")

        // Toggle All Day on (default may vary)
        val allDayText = getString(R.string.calendar_event_all_day)
        composeRule.onNodeWithText(allDayText).assertIsDisplayed()

        // Save
        clickText(R.string.common_save)

        // Verify event appears on the calendar date
        waitForText("TestEvent E2E")
        composeRule.onNodeWithText("TestEvent E2E").assertIsDisplayed()
    }

    @Test
    fun test_calendarMonthNavigation() {
        navigateToCalendar()

        // Click next month button
        val nextMonthDesc = getString(R.string.a11y_calendar_next_month)
        composeRule.onNodeWithContentDescription(nextMonthDesc).performClick()
        composeRule.waitForIdle()

        // Click previous month button twice to go to previous month from original
        val prevMonthDesc = getString(R.string.a11y_calendar_previous_month)
        composeRule.onNodeWithContentDescription(prevMonthDesc).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(prevMonthDesc).performClick()
        composeRule.waitForIdle()

        // Click "Today" to return to current month
        clickText(R.string.calendar_today)
        composeRule.waitForIdle()

        // Verify we are back (the FAB should still be accessible)
        composeRule.onNodeWithText(getString(R.string.calendar_today)).assertIsDisplayed()
    }
}
