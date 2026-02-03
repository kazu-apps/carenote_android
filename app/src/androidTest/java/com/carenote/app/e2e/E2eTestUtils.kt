package com.carenote.app.e2e

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput

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
