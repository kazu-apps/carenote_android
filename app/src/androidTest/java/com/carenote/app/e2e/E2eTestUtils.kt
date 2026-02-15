package com.carenote.app.e2e

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.carenote.app.data.local.CareNoteDatabase
import com.carenote.app.data.local.entity.CareRecipientEntity
import com.carenote.app.data.local.entity.MemberEntity
import com.carenote.app.data.local.entity.InvitationEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Wait until a node with the given text exists, with a configurable timeout.
 */
fun ComposeTestRule.waitUntilNodeWithTextExists(
    text: String,
    timeoutMs: Long = 5_000L,
    useUnmergedTree: Boolean = false
) {
    waitUntil(timeoutMs) {
        onAllNodes(
            androidx.compose.ui.test.hasText(text),
            useUnmergedTree = useUnmergedTree
        ).fetchSemanticsNodes().isNotEmpty()
    }
}

/**
 * Wait until a node with the given text is displayed.
 */
fun ComposeTestRule.waitForText(
    text: String,
    timeoutMs: Long = 5_000L
) {
    waitUntilNodeWithTextExists(text, timeoutMs)
    onAllNodesWithText(text).onFirst().assertIsDisplayed()
}

/**
 * Clear existing text and type new text into a text field.
 */
fun SemanticsNodeInteraction.clearAndType(text: String): SemanticsNodeInteraction {
    performTextClearance()
    performTextInput(text)
    return this
}

/**
 * Seed a CareRecipient for tests requiring member/invitation data.
 */
suspend fun CareNoteDatabase.seedCareRecipient(
    name: String = "Test Recipient",
    gender: String = "other"
): Long {
    val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    return careRecipientDao().insertOrUpdate(
        CareRecipientEntity(
            name = name,
            birthDate = "1950-01-01",
            gender = gender,
            memo = "",
            createdAt = now,
            updatedAt = now
        )
    )
}

/**
 * Seed a Member entity for E2E tests.
 */
suspend fun CareNoteDatabase.seedMember(
    careRecipientId: Long,
    uid: String = "test-member-uid",
    role: String = "MEMBER"
): Long {
    val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    return memberDao().insertMember(
        MemberEntity(
            careRecipientId = careRecipientId,
            uid = uid,
            role = role,
            joinedAt = now
        )
    )
}

/**
 * Seed an Invitation entity for E2E tests.
 */
suspend fun CareNoteDatabase.seedInvitation(
    careRecipientId: Long,
    inviterUid: String = "test-inviter-uid",
    inviteeEmail: String = "invitee@example.com",
    status: String = "PENDING",
    token: String = "test-token-${System.currentTimeMillis()}",
    expiresAt: String? = null
): Long {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val expiry = expiresAt ?: now.plusDays(30).format(formatter)
    return invitationDao().insertInvitation(
        InvitationEntity(
            careRecipientId = careRecipientId,
            inviterUid = inviterUid,
            inviteeEmail = inviteeEmail,
            status = status,
            token = token,
            expiresAt = expiry,
            createdAt = now.format(formatter)
        )
    )
}
