package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import com.carenote.app.ui.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExportFlowTest : E2eTestBase() {

    private fun navigateToHealthRecords() {
        navigateToTab(R.string.nav_health_records)
        waitForFab(TestTags.HEALTH_RECORDS_FAB)
    }

    private fun addHealthRecord(temperature: String = "36.5") {
        clickFab(TestTags.HEALTH_RECORDS_FAB)
        waitForText(R.string.health_records_add)
        fillTextFieldByLabel(R.string.health_records_temperature, temperature)
        clickText(R.string.common_save)
        waitForText(temperature)
    }

    @Test
    fun test_exportMenuOpens() {
        navigateToHealthRecords()

        composeRule.onNodeWithTag(TestTags.EXPORT_BUTTON).performClick()
        composeRule.waitForIdle()

        val csvText = getString(R.string.health_records_export_csv)
        val pdfText = getString(R.string.health_records_export_pdf)

        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(csvText))
                .fetchSemanticsNodes().isNotEmpty()
        }

        assertTextDisplayed(csvText)
        assertTextDisplayed(pdfText)
    }

    @Test
    fun test_exportCsvWithRecords() {
        navigateToHealthRecords()
        addHealthRecord("37.0")

        composeRule.onNodeWithTag(TestTags.EXPORT_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(getString(R.string.health_records_export_csv)))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(TestTags.EXPORT_CSV_ITEM).performClick()
        composeRule.waitForIdle()

        // After export, the menu should be closed and health records screen is still visible
        waitForFab(TestTags.HEALTH_RECORDS_FAB, 10_000L)
    }

    @Test
    fun test_exportPdfWithRecords() {
        navigateToHealthRecords()
        addHealthRecord("36.8")

        composeRule.onNodeWithTag(TestTags.EXPORT_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(getString(R.string.health_records_export_pdf)))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(TestTags.EXPORT_PDF_ITEM).performClick()
        composeRule.waitForIdle()

        // After export, the menu should be closed and health records screen is still visible
        waitForFab(TestTags.HEALTH_RECORDS_FAB, 10_000L)
    }

    @Test
    fun test_exportEmptyShowsSnackbar() {
        navigateToHealthRecords()

        composeRule.onNodeWithTag(TestTags.EXPORT_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(getString(R.string.health_records_export_csv)))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(TestTags.EXPORT_CSV_ITEM).performClick()
        composeRule.waitForIdle()

        val emptyText = getString(R.string.health_records_export_empty)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(emptyText))
                .fetchSemanticsNodes().isNotEmpty()
        }
        assertTextDisplayed(emptyText)
    }
}
