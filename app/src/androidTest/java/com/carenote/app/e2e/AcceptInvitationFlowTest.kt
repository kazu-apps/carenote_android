package com.carenote.app.e2e

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carenote.app.R
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AcceptInvitationFlowTest : E2eTestBase() {

    /**
     * Launch a deep link to the accept invitation screen with the given token.
     */
    private fun launchDeepLink(token: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("carenote://accept_invitation/$token"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.startActivity(intent)
        }
        composeRule.waitForIdle()
    }

    @Test
    fun test_acceptInvitation_validToken_showsContent() {
        // Seed data with a known token
        runBlocking {
            val careRecipientId = database.seedCareRecipient()
            database.seedInvitation(
                careRecipientId = careRecipientId,
                token = "valid-token-123"
            )
        }

        // Launch deep link
        launchDeepLink("valid-token-123")

        // Verify the invitation content is displayed
        waitForText(R.string.accept_invitation_description, 10_000L)
        assertTextDisplayed(R.string.accept_invitation_accept)
        assertTextDisplayed(R.string.accept_invitation_decline)
    }

    @Test
    fun test_acceptInvitation_accept_showsSuccess() {
        // Set logged in user (required for accepting)
        setLoggedInUser()

        // Seed data
        runBlocking {
            val careRecipientId = database.seedCareRecipient()
            database.seedInvitation(
                careRecipientId = careRecipientId,
                token = "accept-token-456"
            )
        }

        // Launch deep link
        launchDeepLink("accept-token-456")

        // Wait for content to load
        waitForText(R.string.accept_invitation_description, 10_000L)

        // Click accept button
        clickText(R.string.accept_invitation_accept)

        // Verify success message
        waitForText(R.string.accept_invitation_success, 10_000L)
    }

    @Test
    fun test_acceptInvitation_decline_navigatesBack() {
        // Seed data
        runBlocking {
            val careRecipientId = database.seedCareRecipient()
            database.seedInvitation(
                careRecipientId = careRecipientId,
                token = "decline-token-789"
            )
        }

        // Launch deep link
        launchDeepLink("decline-token-789")

        // Wait for content to load
        waitForText(R.string.accept_invitation_description, 10_000L)

        // Click decline button
        clickText(R.string.accept_invitation_decline)
        composeRule.waitForIdle()
    }

    @Test
    fun test_acceptInvitation_invalidToken_showsError() {
        // No seeding - token does not exist in DB

        // Launch deep link with a nonexistent token
        launchDeepLink("nonexistent-token")

        // Verify invalid token error is displayed
        waitForText(R.string.accept_invitation_invalid_token, 10_000L)
        assertTextDisplayed(R.string.accept_invitation_invalid_token)
    }

    @Test
    fun test_acceptInvitation_expiredToken_showsError() {
        // Seed with an expired invitation (yesterday)
        val yesterday = LocalDateTime.now().minusDays(1)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        runBlocking {
            val careRecipientId = database.seedCareRecipient()
            database.seedInvitation(
                careRecipientId = careRecipientId,
                token = "expired-token",
                expiresAt = yesterday
            )
        }

        // Launch deep link
        launchDeepLink("expired-token")

        // Verify expired error is displayed
        waitForText(R.string.accept_invitation_expired, 10_000L)
        assertTextDisplayed(R.string.accept_invitation_expired)
    }
}
