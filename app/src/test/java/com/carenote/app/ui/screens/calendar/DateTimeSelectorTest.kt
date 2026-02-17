package com.carenote.app.ui.screens.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Tests for the clickable pattern used in DateSelector and TimeSelector
 * after the tablet DatePicker/TimePicker accessibility fix.
 *
 * The fix replaces TextButton with Text + Modifier.clickable(role = Role.Button)
 * to ensure proper touch targets on tablet layouts.
 *
 * Since DateSelector and TimeSelector are private composables,
 * these tests reproduce the same UI structure and verify the clickable pattern.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ja")
class DateTimeSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // Test composables that mirror the post-fix structure
    // =========================================================================

    @Composable
    private fun TestDateSelector(
        date: LocalDate,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "日付",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = DateTimeFormatters.formatDate(date),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(12.dp)
            )
        }
    }

    @Composable
    private fun TestTimeSelector(
        label: String,
        time: LocalTime?,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = time?.let { DateTimeFormatters.formatTime(it) } ?: "--:--",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(12.dp)
            )
        }
    }

    // =========================================================================
    // DateSelector tests
    // =========================================================================

    @Test
    fun dateSelector_dateTextClick_triggersOnClick() {
        var clicked = false
        val date = LocalDate.of(2026, 2, 17)
        composeTestRule.setContent {
            CareNoteTheme {
                TestDateSelector(
                    date = date,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText(DateTimeFormatters.formatDate(date))
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun dateSelector_displaysFormattedDate() {
        val date = LocalDate.of(2026, 3, 15)
        composeTestRule.setContent {
            CareNoteTheme {
                TestDateSelector(
                    date = date,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(DateTimeFormatters.formatDate(date))
            .assertIsDisplayed()
    }

    @Test
    fun dateSelector_clickableText_hasButtonRoleSemantics() {
        val date = LocalDate.of(2026, 2, 17)
        composeTestRule.setContent {
            CareNoteTheme {
                TestDateSelector(
                    date = date,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(DateTimeFormatters.formatDate(date))
            .assertHasClickAction()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Button
                )
            )
    }

    // =========================================================================
    // TimeSelector tests
    // =========================================================================

    @Test
    fun timeSelector_timeTextClick_triggersOnClick() {
        var clicked = false
        composeTestRule.setContent {
            CareNoteTheme {
                TestTimeSelector(
                    label = "開始時刻",
                    time = LocalTime.of(10, 0),
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText(DateTimeFormatters.formatTime(LocalTime.of(10, 0)))
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun timeSelector_nullTime_displaysDashes() {
        composeTestRule.setContent {
            CareNoteTheme {
                TestTimeSelector(
                    label = "開始時刻",
                    time = null,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("--:--")
            .assertIsDisplayed()
    }

    @Test
    fun timeSelector_withTime_displaysFormattedTime() {
        val time = LocalTime.of(14, 30)
        composeTestRule.setContent {
            CareNoteTheme {
                TestTimeSelector(
                    label = "終了時刻",
                    time = time,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(DateTimeFormatters.formatTime(time))
            .assertIsDisplayed()
    }

    @Test
    fun timeSelector_clickableText_hasButtonRoleSemantics() {
        composeTestRule.setContent {
            CareNoteTheme {
                TestTimeSelector(
                    label = "開始時刻",
                    time = null,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("--:--")
            .assertHasClickAction()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Button
                )
            )
    }

    @Test
    fun timeSelector_displaysLabel() {
        composeTestRule.setContent {
            CareNoteTheme {
                TestTimeSelector(
                    label = "開始時刻",
                    time = null,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("開始時刻")
            .assertIsDisplayed()
    }
}
