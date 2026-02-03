package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import com.carenote.app.ui.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HealthRecordsFlowTest : E2eTestBase() {

    private fun navigateToHealthRecords() {
        navigateToTab(R.string.nav_health_records)
        waitForFab(TestTags.HEALTH_RECORDS_FAB)
    }

    @Test
    fun test_addHealthRecord_showsInList() {
        navigateToHealthRecords()

        // Tap FAB to add record
        clickFab(TestTags.HEALTH_RECORDS_FAB)
        waitForText(R.string.health_records_add)

        // Fill in temperature
        fillTextFieldByLabel(R.string.health_records_temperature, "36.5")

        // Save
        clickText(R.string.common_save)

        // Verify record appears in list (temperature should be displayed)
        waitForText("36.5")
        composeRule.onNodeWithText("36.5", substring = true).assertIsDisplayed()
    }

    @Test
    fun test_addHealthRecord_allEmptyValidation() {
        navigateToHealthRecords()

        // Tap FAB to add record
        clickFab(TestTags.HEALTH_RECORDS_FAB)
        waitForText(R.string.health_records_add)

        // Scroll to and click Save without entering any data
        scrollToAndClickText(R.string.common_save)

        val errorText = getString(R.string.health_records_all_empty_error)

        // Wait for error node to exist in semantics tree
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(errorText))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Scroll to the error banner (at the top of the form) and verify
        composeRule.onNodeWithText(errorText).performScrollTo()
        composeRule.onNodeWithText(errorText).assertIsDisplayed()
    }
}
