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
class NavigationFlowTest : E2eTestBase() {

    @Test
    fun test_bottomNav_allTabsAccessible() {
        // Start on Medication (default screen)
        waitForText(R.string.medication_title)
        assertTextDisplayed(R.string.medication_title)

        // Navigate to Calendar
        navigateToTab(R.string.nav_calendar)
        waitForFab(TestTags.CALENDAR_FAB)

        // Navigate to Timeline
        navigateToTab(R.string.timeline_title)
        waitForText(R.string.timeline_title)
        assertTextDisplayed(R.string.timeline_title)

        // Navigate to Health Records
        navigateToTab(R.string.nav_health_records)
        waitForText(R.string.health_records_title)
        assertTextDisplayed(R.string.health_records_title)

        // Navigate to Notes
        navigateToTab(R.string.nav_notes)
        waitForText(R.string.notes_title)
        assertTextDisplayed(R.string.notes_title)

        // Navigate back to Medication
        navigateToTab(R.string.nav_medication)
        waitForText(R.string.medication_title)
        assertTextDisplayed(R.string.medication_title)
    }

    @Test
    fun test_settingsNavigation() {
        // Start on Medication screen
        waitForText(R.string.medication_title)

        // Click settings icon
        val settingsDesc = getString(R.string.settings_title)
        composeRule.onNodeWithContentDescription(settingsDesc).performClick()
        composeRule.waitForIdle()

        // Verify settings screen is displayed
        waitForText(R.string.settings_title)
        assertTextDisplayed(R.string.settings_notifications)

        // Navigate back
        val closeDesc = getString(R.string.common_close)
        composeRule.onNodeWithContentDescription(closeDesc).performClick()
        composeRule.waitForIdle()

        // Verify we are back on Medication screen
        waitForText(R.string.medication_title)
    }

    @Test
    fun test_backNavigation_fromAddScreens() {
        // Go to Medication Add screen
        waitForFab(TestTags.MEDICATION_FAB)
        clickFab(TestTags.MEDICATION_FAB)
        waitForText(R.string.medication_add)

        // Click cancel
        clickText(R.string.common_cancel)
        composeRule.waitForIdle()

        // Verify back on Medication list
        waitForText(R.string.medication_title)

        // Go to Calendar tab, then Add Event screen
        navigateToTab(R.string.nav_calendar)
        waitForFab(TestTags.CALENDAR_FAB)
        clickFab(TestTags.CALENDAR_FAB)
        waitForText(R.string.calendar_add_event)

        // Click cancel
        clickText(R.string.common_cancel)
        composeRule.waitForIdle()

        // Verify back on Calendar
        waitForFab(TestTags.CALENDAR_FAB)

        // Go to Notes tab, then Add Note screen
        navigateToTab(R.string.nav_notes)
        waitForFab(TestTags.NOTES_FAB)
        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)

        // Click cancel
        clickText(R.string.common_cancel)
        composeRule.waitForIdle()

        // Verify back on Notes list
        waitForText(R.string.notes_title)
    }
}
