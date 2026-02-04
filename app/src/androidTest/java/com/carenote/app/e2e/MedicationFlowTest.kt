package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
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
class MedicationFlowTest : E2eTestBase() {

    /**
     * Wait for the Add Medication form to be ready by checking
     * for the medication name text field (unique to this screen).
     */
    private fun waitForAddMedicationForm() {
        val nameLabel = getString(R.string.medication_name)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasText(nameLabel) and hasSetTextAction())
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun test_addMedication_showsInList() {
        waitForFab(TestTags.MEDICATION_FAB)
        clickFab(TestTags.MEDICATION_FAB)
        waitForAddMedicationForm()

        fillTextFieldByLabel(R.string.medication_name, "TestMed E2E")
        fillTextFieldByLabel(R.string.medication_dosage, "1 tablet")
        clickText(R.string.medication_morning)
        clickText(R.string.common_save)

        waitForText("TestMed E2E")
        composeRule.onNodeWithText("TestMed E2E").assertIsDisplayed()
    }

    @Test
    fun test_medicationDetail_showsCorrectInfo() {
        waitForFab(TestTags.MEDICATION_FAB)
        clickFab(TestTags.MEDICATION_FAB)
        waitForAddMedicationForm()

        fillTextFieldByLabel(R.string.medication_name, "DetailMed E2E")
        fillTextFieldByLabel(R.string.medication_dosage, "2 tablets")
        clickText(R.string.medication_morning)
        clickText(R.string.common_save)

        waitForText("DetailMed E2E")
        clickText("DetailMed E2E")

        waitForText("DetailMed E2E", 10_000L)
        assertTextDisplayed("DetailMed E2E")
        assertTextDisplayed("2 tablets")
    }

    @Test
    fun test_recordMedicationTaken_showsStatus() {
        waitForFab(TestTags.MEDICATION_FAB)
        clickFab(TestTags.MEDICATION_FAB)
        waitForAddMedicationForm()

        fillTextFieldByLabel(R.string.medication_name, "TakenMed E2E")
        clickText(R.string.medication_morning)
        clickText(R.string.common_save)

        waitForText("TakenMed E2E")

        val takenText = getString(R.string.medication_taken)
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(takenText)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodes(hasText(takenText)).onFirst().assertIsDisplayed()
        composeRule.onAllNodes(hasText(takenText)).onFirst().performClick()
        composeRule.waitForIdle()

        val statusTakenText = getString(R.string.medication_status_taken)
        waitForText(statusTakenText, 5_000L)
    }

    @Test
    fun test_addMedication_nameRequired_validation() {
        waitForFab(TestTags.MEDICATION_FAB)
        clickFab(TestTags.MEDICATION_FAB)
        waitForAddMedicationForm()

        scrollToAndClickText(R.string.common_save)

        val errorText = getString(R.string.medication_name_required)
        waitForText(errorText, 5_000L)
        assertTextDisplayed(errorText)
    }
}
