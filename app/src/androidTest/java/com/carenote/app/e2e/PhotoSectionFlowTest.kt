package com.carenote.app.e2e

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
class PhotoSectionFlowTest : E2eTestBase() {

    @Test
    fun test_photoSectionVisibleInHealthRecordForm() {
        navigateToTab(R.string.nav_health_records)
        waitForFab(TestTags.HEALTH_RECORDS_FAB)

        clickFab(TestTags.HEALTH_RECORDS_FAB)
        waitForText(R.string.health_records_add)

        val photoTitle = getString(R.string.photo_section_title)
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(photoTitle))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(photoTitle).performScrollTo()
        assertTextDisplayed(photoTitle)
    }

    @Test
    fun test_photoSectionVisibleInNoteForm() {
        navigateToTab(R.string.nav_notes)
        waitForFab(TestTags.NOTES_FAB)

        clickFab(TestTags.NOTES_FAB)
        waitForText(R.string.notes_add)

        val photoTitle = getString(R.string.photo_section_title)
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodes(hasText(photoTitle))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(photoTitle).performScrollTo()
        assertTextDisplayed(photoTitle)
    }
}
