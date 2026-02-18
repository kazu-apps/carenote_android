package com.carenote.app.ui.screens.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.DateTimeFormatters
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

/**
 * Tests for the clickable pattern used in DueDateSelector and ReminderSection
 * after the tablet DatePicker/TimePicker accessibility fix.
 *
 * The fix replaces TextButton with Text + Modifier.clickable(role = Role.Button)
 * to ensure proper touch targets on tablet layouts.
 *
 * Since DueDateSelector and ReminderSection are private composables,
 * these tests reproduce the same UI structure and verify the clickable pattern.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ja")
class DueDateSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // Test composables that mirror the post-fix structure
    // =========================================================================

    @Composable
    private fun TestDueDateSelector(
        dueDate: LocalDate?,
        onClickDate: () -> Unit,
        onClearDate: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "期限",
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dueDate?.let { DateTimeFormatters.formatDate(it) } ?: "期限なし",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onClickDate)
                        .padding(12.dp)
                )
                if (dueDate != null) {
                    TextButton(onClick = onClearDate) {
                        Text(
                            text = "削除する",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun TestReminderSection(
        enabled: Boolean,
        time: LocalTime?,
        onToggle: () -> Unit,
        onClickTime: () -> Unit
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "リマインダー",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle() }
                )
            }
            if (enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "通知時刻",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = time?.let {
                            String.format("%02d:%02d", it.hour, it.minute)
                        } ?: "未設定",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onClickTime)
                            .padding(12.dp)
                    )
                }
            }
        }
    }

    // =========================================================================
    // DueDateSelector tests
    // =========================================================================

    @Test
    fun dueDateSelector_dateTextClick_triggersOnClickDate() {
        var clicked = false
        composeTestRule.setContent {
            CareNoteTheme {
                TestDueDateSelector(
                    dueDate = null,
                    onClickDate = { clicked = true },
                    onClearDate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("期限なし")
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun dueDateSelector_clearButtonClick_triggersOnClearDate() {
        var cleared = false
        composeTestRule.setContent {
            CareNoteTheme {
                TestDueDateSelector(
                    dueDate = LocalDate.of(2026, 2, 17),
                    onClickDate = {},
                    onClearDate = { cleared = true }
                )
            }
        }

        composeTestRule.onNodeWithText("削除する")
            .performClick()

        assertTrue(cleared)
    }

    @Test
    fun dueDateSelector_nullDueDate_displaysNoDueDateText() {
        composeTestRule.setContent {
            CareNoteTheme {
                TestDueDateSelector(
                    dueDate = null,
                    onClickDate = {},
                    onClearDate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("期限なし")
            .assertIsDisplayed()
    }

    @Test
    fun dueDateSelector_withDueDate_displaysFormattedDate() {
        val date = LocalDate.of(2026, 2, 17)
        composeTestRule.setContent {
            CareNoteTheme {
                TestDueDateSelector(
                    dueDate = date,
                    onClickDate = {},
                    onClearDate = {}
                )
            }
        }

        composeTestRule.onNodeWithText(DateTimeFormatters.formatDate(date))
            .assertIsDisplayed()
    }

    @Test
    fun dueDateSelector_clickableText_hasButtonRoleSemantics() {
        composeTestRule.setContent {
            CareNoteTheme {
                TestDueDateSelector(
                    dueDate = null,
                    onClickDate = {},
                    onClearDate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("期限なし")
            .assertHasClickAction()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Button
                )
            )
    }

    // =========================================================================
    // ReminderSection tests
    // =========================================================================

    @Test
    fun reminderSection_timeTextClick_triggersOnClickTime() {
        var clicked = false
        composeTestRule.setContent {
            CareNoteTheme {
                TestReminderSection(
                    enabled = true,
                    time = null,
                    onToggle = {},
                    onClickTime = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("未設定")
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun reminderSection_withTime_displaysFormattedTime() {
        composeTestRule.setContent {
            CareNoteTheme {
                TestReminderSection(
                    enabled = true,
                    time = LocalTime.of(9, 30),
                    onToggle = {},
                    onClickTime = {}
                )
            }
        }

        composeTestRule.onNodeWithText("09:30")
            .assertIsDisplayed()
    }

    @Test
    fun reminderSection_disabled_hidesTimeSelector() {
        composeTestRule.setContent {
            CareNoteTheme {
                TestReminderSection(
                    enabled = false,
                    time = LocalTime.of(9, 0),
                    onToggle = {},
                    onClickTime = {}
                )
            }
        }

        composeTestRule.onNodeWithText("通知時刻")
            .assertDoesNotExist()
    }

    @Test
    fun reminderSection_clickableTimeText_hasButtonRoleSemantics() {
        composeTestRule.setContent {
            CareNoteTheme {
                TestReminderSection(
                    enabled = true,
                    time = null,
                    onToggle = {},
                    onClickTime = {}
                )
            }
        }

        composeTestRule.onNodeWithText("未設定")
            .assertHasClickAction()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Button
                )
            )
    }
}
