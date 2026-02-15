package com.carenote.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import com.carenote.app.ui.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MemberInvitationFlowTest : E2eTestBase() {

    /**
     * Navigate to the MemberManagement screen from Settings.
     * Settings is accessed via the settings icon (contentDescription),
     * then "Member Management" is scrolled to and clicked within the LazyColumn.
     */
    private fun navigateToMemberManagement() {
        // Open Settings via the icon in the top bar
        val settingsDesc = getString(R.string.settings_title)
        composeRule.onNodeWithContentDescription(settingsDesc).performClick()
        composeRule.waitForIdle()

        // Wait for Settings screen to load
        waitForText(R.string.settings_title)

        // Scroll to and click Member Management within the LazyColumn
        scrollToAndClickText(R.string.member_management_section_summary)
        waitForText(R.string.member_management_title, 10_000L)
    }

    @Test
    fun test_navigateToMemberManagement_fromSettings() {
        // Wait for initial screen
        waitForText(R.string.medication_title)

        // Navigate to Settings via icon
        val settingsDesc = getString(R.string.settings_title)
        composeRule.onNodeWithContentDescription(settingsDesc).performClick()
        composeRule.waitForIdle()

        // Wait for Settings to load
        waitForText(R.string.settings_title)

        // Scroll to and click Member Management
        scrollToAndClickText(R.string.member_management_section_summary)

        // Verify MemberManagement screen is displayed
        waitForText(R.string.member_management_title, 10_000L)
        assertTextDisplayed(R.string.member_management_title)
    }

    @Test
    fun test_memberManagement_emptyState() {
        waitForText(R.string.medication_title)
        navigateToMemberManagement()

        // Verify empty state message is displayed
        assertTextDisplayed(R.string.member_management_empty)
    }

    @Test
    fun test_memberManagement_fabNavigatesToSendInvitation() {
        waitForText(R.string.medication_title)
        navigateToMemberManagement()

        // Wait for and click the FAB
        waitForFab(TestTags.MEMBER_MANAGEMENT_FAB)
        clickFab(TestTags.MEMBER_MANAGEMENT_FAB)

        // Verify Send Invitation screen is displayed
        waitForText(R.string.send_invitation_title, 10_000L)
        assertTextDisplayed(R.string.send_invitation_title)
    }

    @Test
    fun test_sendInvitation_emailValidation() {
        waitForText(R.string.medication_title)
        navigateToMemberManagement()

        // Navigate to Send Invitation
        waitForFab(TestTags.MEMBER_MANAGEMENT_FAB)
        clickFab(TestTags.MEMBER_MANAGEMENT_FAB)
        waitForText(R.string.send_invitation_title, 10_000L)

        // Try to send without entering email
        scrollToAndClickText(R.string.send_invitation_send)

        // Verify validation error
        waitForText(R.string.send_invitation_email_required, 5_000L)
        assertTextDisplayed(R.string.send_invitation_email_required)
    }

    @Test
    fun test_sendInvitation_emailInputDisplayed() {
        waitForText(R.string.medication_title)
        navigateToMemberManagement()

        // Navigate to Send Invitation
        waitForFab(TestTags.MEMBER_MANAGEMENT_FAB)
        clickFab(TestTags.MEMBER_MANAGEMENT_FAB)
        waitForText(R.string.send_invitation_title, 10_000L)

        // Verify email input field is displayed
        assertTextDisplayed(R.string.send_invitation_email_label)
    }

    @Test
    fun test_memberManagement_showsMemberList() {
        // Seed data
        runBlocking {
            val careRecipientId = database.seedCareRecipient()
            database.seedMember(careRecipientId = careRecipientId)
        }

        waitForText(R.string.medication_title)
        navigateToMemberManagement()

        // Verify member list section is displayed
        waitForText(R.string.member_management_section_title, 10_000L)
        assertTextDisplayed(R.string.member_management_section_title)
    }

    @Test
    fun test_memberManagement_showsPendingInvitations() {
        // Seed data
        runBlocking {
            val careRecipientId = database.seedCareRecipient()
            database.seedInvitation(careRecipientId = careRecipientId)
        }

        waitForText(R.string.medication_title)
        navigateToMemberManagement()

        // Verify pending invitations section is displayed
        waitForText(R.string.invitation_pending_section, 10_000L)
        assertTextDisplayed(R.string.invitation_pending_section)
    }

    @Test
    fun test_memberManagement_cancelInvitation() {
        // Seed data with invitation
        runBlocking {
            val careRecipientId = database.seedCareRecipient()
            database.seedInvitation(careRecipientId = careRecipientId)
        }

        waitForText(R.string.medication_title)
        navigateToMemberManagement()

        // Wait for the pending invitation section
        waitForText(R.string.invitation_pending_section, 10_000L)

        // Click on the invitation status to trigger cancel
        clickText(R.string.invitation_status_pending)
        composeRule.waitForIdle()

        // Verify cancel confirmation dialog is shown
        waitForText(R.string.invitation_cancel_confirm, 5_000L)
        assertTextDisplayed(R.string.invitation_cancel_confirm)
    }
}
