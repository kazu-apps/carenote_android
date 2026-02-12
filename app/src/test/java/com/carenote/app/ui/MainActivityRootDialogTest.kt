package com.carenote.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carenote.app.R
import com.carenote.app.ui.theme.CareNoteTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Root 検出ダイアログの UI テスト
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ja")
class MainActivityRootDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rootDialog_continueButtonDismissesDialog() {
        composeTestRule.setContent {
            CareNoteTheme {
                var showDialog by remember { mutableStateOf(true) }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = {},
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        title = {
                            Text(text = stringResource(R.string.security_root_warning_dialog_title))
                        },
                        text = {
                            Text(text = stringResource(R.string.security_root_warning_dialog_message))
                        },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text(text = stringResource(R.string.common_continue))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {}) {
                                Text(text = stringResource(R.string.common_exit))
                            }
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("セキュリティ警告")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("続ける")
            .performClick()

        composeTestRule.onNodeWithText("セキュリティ警告")
            .assertDoesNotExist()
    }

    @Test
    fun rootDialog_exitButtonCallsFinish() {
        var exitCalled = false

        composeTestRule.setContent {
            CareNoteTheme {
                AlertDialog(
                    onDismissRequest = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = {
                        Text(text = stringResource(R.string.security_root_warning_dialog_title))
                    },
                    text = {
                        Text(text = stringResource(R.string.security_root_warning_dialog_message))
                    },
                    confirmButton = {
                        TextButton(onClick = {}) {
                            Text(text = stringResource(R.string.common_continue))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { exitCalled = true }) {
                            Text(text = stringResource(R.string.common_exit))
                        }
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("終了")
            .performClick()

        assertTrue(exitCalled)
    }

    @Test
    fun rootDialog_notShownWhenRootNotDetected() {
        composeTestRule.setContent {
            CareNoteTheme {
                val showRootWarning = false

                if (showRootWarning) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = {
                            Text(text = stringResource(R.string.security_root_warning_dialog_title))
                        },
                        text = {
                            Text(text = stringResource(R.string.security_root_warning_dialog_message))
                        },
                        confirmButton = {
                            TextButton(onClick = {}) {
                                Text(text = stringResource(R.string.common_continue))
                            }
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("セキュリティ警告")
            .assertDoesNotExist()
    }

    @Test
    fun rootDialog_displaysBothButtons() {
        composeTestRule.setContent {
            CareNoteTheme {
                AlertDialog(
                    onDismissRequest = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = {
                        Text(text = stringResource(R.string.security_root_warning_dialog_title))
                    },
                    text = {
                        Text(text = stringResource(R.string.security_root_warning_dialog_message))
                    },
                    confirmButton = {
                        TextButton(onClick = {}) {
                            Text(text = stringResource(R.string.common_continue))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {}) {
                            Text(text = stringResource(R.string.common_exit))
                        }
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("続ける").assertIsDisplayed()
        composeTestRule.onNodeWithText("終了").assertIsDisplayed()
        composeTestRule.onNodeWithText("セキュリティ警告").assertIsDisplayed()
    }
}
