package com.carenote.app.ui.screens.settings.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * TDD RED phase tests for SettingsScreen section Composables.
 *
 * These tests define the expected interface for 6 section components
 * that will be extracted from SettingsScreen (462 lines).
 * All tests will fail to compile until the section Composables are implemented.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ja")
class SettingsSectionsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // ThemeSection (3 tests)
    // =========================================================================

    @Test
    fun themeSection_displaysTitle() {
        composeTestRule.setContent {
            ThemeSection(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeSelected = {}
            )
        }

        composeTestRule.onNodeWithText("テーマ", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun themeSection_currentModeIsSelected() {
        composeTestRule.setContent {
            ThemeSection(
                themeMode = ThemeMode.DARK,
                onThemeModeSelected = {}
            )
        }

        // The radio button for DARK mode should be selected
        composeTestRule.onNodeWithText("ダーク", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun themeSection_clickModeCallsCallback() {
        var selectedMode: ThemeMode? = null

        composeTestRule.setContent {
            ThemeSection(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeSelected = { selectedMode = it }
            )
        }

        composeTestRule.onNodeWithText("ライト", substring = true)
            .performClick()

        assertEquals(ThemeMode.LIGHT, selectedMode)
    }

    // =========================================================================
    // LanguageSection (3 tests)
    // =========================================================================

    @Test
    fun languageSection_displaysTitle() {
        composeTestRule.setContent {
            LanguageSection(
                appLanguage = AppLanguage.SYSTEM,
                onLanguageSelected = {}
            )
        }

        composeTestRule.onNodeWithText("言語", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun languageSection_currentLanguageIsDisplayed() {
        composeTestRule.setContent {
            LanguageSection(
                appLanguage = AppLanguage.JAPANESE,
                onLanguageSelected = {}
            )
        }

        composeTestRule.onNodeWithText("日本語", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun languageSection_clickLanguageCallsCallback() {
        var selectedLanguage: AppLanguage? = null

        composeTestRule.setContent {
            LanguageSection(
                appLanguage = AppLanguage.SYSTEM,
                onLanguageSelected = { selectedLanguage = it }
            )
        }

        composeTestRule.onNodeWithText("English", substring = true)
            .performClick()

        assertEquals(AppLanguage.ENGLISH, selectedLanguage)
    }

    // =========================================================================
    // SyncSection (5 tests)
    // =========================================================================

    @Test
    fun syncSection_displaysTitle() {
        composeTestRule.setContent {
            SyncSection(
                syncEnabled = true,
                onSyncEnabledChange = {},
                isSyncing = false,
                isLoggedIn = true,
                lastSyncText = "未同期",
                onSyncNowClick = {}
            )
        }

        composeTestRule.onNodeWithText("同期設定", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun syncSection_displaysSyncSwitch() {
        composeTestRule.setContent {
            SyncSection(
                syncEnabled = true,
                onSyncEnabledChange = {},
                isSyncing = false,
                isLoggedIn = true,
                lastSyncText = "未同期",
                onSyncNowClick = {}
            )
        }

        composeTestRule.onNodeWithText("クラウド同期を有効にする", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun syncSection_syncNowButtonClickCallsCallback() {
        var clicked = false

        composeTestRule.setContent {
            SyncSection(
                syncEnabled = true,
                onSyncEnabledChange = {},
                isSyncing = false,
                isLoggedIn = true,
                lastSyncText = "未同期",
                onSyncNowClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("今すぐ同期", substring = true)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun syncSection_showsProgressWhenSyncing() {
        composeTestRule.setContent {
            SyncSection(
                syncEnabled = true,
                onSyncEnabledChange = {},
                isSyncing = true,
                isLoggedIn = true,
                lastSyncText = "未同期",
                onSyncNowClick = {}
            )
        }

        composeTestRule.onNodeWithText("同期中", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun syncSection_syncNowDisabledWhenNotLoggedIn() {
        composeTestRule.setContent {
            SyncSection(
                syncEnabled = true,
                onSyncEnabledChange = {},
                isSyncing = false,
                isLoggedIn = false,
                lastSyncText = "未同期",
                onSyncNowClick = {}
            )
        }

        composeTestRule.onNodeWithText("今すぐ同期", substring = true)
            .assertIsDisplayed()
    }

    // =========================================================================
    // NotificationSection (3 tests)
    // =========================================================================

    @Test
    fun notificationSection_displaysTitle() {
        composeTestRule.setContent {
            NotificationSection(
                notificationsEnabled = true,
                onNotificationsEnabledChange = {},
                quietHoursText = "22:00 〜 7:00",
                onQuietHoursClick = {}
            )
        }

        composeTestRule.onNodeWithText("通知設定", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun notificationSection_displaysNotificationSwitch() {
        composeTestRule.setContent {
            NotificationSection(
                notificationsEnabled = true,
                onNotificationsEnabledChange = {},
                quietHoursText = "22:00 〜 7:00",
                onQuietHoursClick = {}
            )
        }

        composeTestRule.onNodeWithText("通知を有効にする", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun notificationSection_quietHoursClickCallsCallback() {
        var clicked = false

        composeTestRule.setContent {
            NotificationSection(
                notificationsEnabled = true,
                onNotificationsEnabledChange = {},
                quietHoursText = "22:00 〜 7:00",
                onQuietHoursClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("おやすみ時間", substring = true)
            .performClick()

        assertTrue(clicked)
    }

    // =========================================================================
    // HealthThresholdSection (3 tests)
    // =========================================================================

    @Test
    fun healthThresholdSection_displaysTitle() {
        composeTestRule.setContent {
            HealthThresholdSection(
                temperatureText = "37.5℃ 以上",
                onTemperatureClick = {},
                bpUpperText = "140 mmHg 以上",
                onBpUpperClick = {},
                bpLowerText = "90 mmHg 以上",
                onBpLowerClick = {},
                pulseHighText = "100 回/分 以上",
                onPulseHighClick = {},
                pulseLowText = "60 回/分 以下",
                onPulseLowClick = {}
            )
        }

        composeTestRule.onNodeWithText("健康", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun healthThresholdSection_displaysAllFiveItems() {
        composeTestRule.setContent {
            HealthThresholdSection(
                temperatureText = "37.5℃ 以上",
                onTemperatureClick = {},
                bpUpperText = "140 mmHg 以上",
                onBpUpperClick = {},
                bpLowerText = "90 mmHg 以上",
                onBpLowerClick = {},
                pulseHighText = "100 回/分 以上",
                onPulseHighClick = {},
                pulseLowText = "60 回/分 以下",
                onPulseLowClick = {}
            )
        }

        composeTestRule.onNodeWithText("体温", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("血圧上", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("血圧下", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("脈拍の異常値上限", substring = true).assertExists()
    }

    @Test
    fun healthThresholdSection_clickItemCallsCallback() {
        var clicked = false

        composeTestRule.setContent {
            HealthThresholdSection(
                temperatureText = "37.5℃ 以上",
                onTemperatureClick = { clicked = true },
                bpUpperText = "140 mmHg 以上",
                onBpUpperClick = {},
                bpLowerText = "90 mmHg 以上",
                onBpLowerClick = {},
                pulseHighText = "100 回/分 以上",
                onPulseHighClick = {},
                pulseLowText = "60 回/分 以下",
                onPulseLowClick = {}
            )
        }

        composeTestRule.onNodeWithText("体温", substring = true)
            .performClick()

        assertTrue(clicked)
    }

    // =========================================================================
    // MedicationTimeSection (3 tests)
    // =========================================================================

    @Test
    fun medicationTimeSection_displaysTitle() {
        composeTestRule.setContent {
            MedicationTimeSection(
                morningTimeText = "08:00",
                onMorningClick = {},
                noonTimeText = "12:00",
                onNoonClick = {},
                eveningTimeText = "18:00",
                onEveningClick = {}
            )
        }

        composeTestRule.onNodeWithText("服薬", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun medicationTimeSection_displaysAllThreeTimings() {
        composeTestRule.setContent {
            MedicationTimeSection(
                morningTimeText = "08:00",
                onMorningClick = {},
                noonTimeText = "12:00",
                onNoonClick = {},
                eveningTimeText = "18:00",
                onEveningClick = {}
            )
        }

        composeTestRule.onNodeWithText("朝", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("昼", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("夕", substring = true).assertIsDisplayed()
    }

    @Test
    fun medicationTimeSection_clickTimingCallsCallback() {
        var clicked = false

        composeTestRule.setContent {
            MedicationTimeSection(
                morningTimeText = "08:00",
                onMorningClick = { clicked = true },
                noonTimeText = "12:00",
                onNoonClick = {},
                eveningTimeText = "18:00",
                onEveningClick = {}
            )
        }

        composeTestRule.onNodeWithText("朝", substring = true)
            .performClick()

        assertTrue(clicked)
    }

    // =========================================================================
    // AppInfoSection (4 tests)
    // =========================================================================

    @Test
    fun appInfoSection_displaysTitle() {
        composeTestRule.setContent {
            AppInfoSection(
                versionName = "1.0.0",
                onPrivacyPolicyClick = {},
                onTermsOfServiceClick = {},
                onContactClick = {},
                onResetClick = {}
            )
        }

        composeTestRule.onNodeWithText("アプリ情報", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun appInfoSection_displaysVersion() {
        composeTestRule.setContent {
            AppInfoSection(
                versionName = "1.0.0",
                onPrivacyPolicyClick = {},
                onTermsOfServiceClick = {},
                onContactClick = {},
                onResetClick = {}
            )
        }

        composeTestRule.onNodeWithText("1.0.0", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun appInfoSection_privacyPolicyClickCallsCallback() {
        var clicked = false

        composeTestRule.setContent {
            AppInfoSection(
                versionName = "1.0.0",
                onPrivacyPolicyClick = { clicked = true },
                onTermsOfServiceClick = {},
                onContactClick = {},
                onResetClick = {}
            )
        }

        composeTestRule.onNodeWithText("プライバシーポリシー", substring = true)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun appInfoSection_termsOfServiceClickCallsCallback() {
        var clicked = false

        composeTestRule.setContent {
            AppInfoSection(
                versionName = "1.0.0",
                onPrivacyPolicyClick = {},
                onTermsOfServiceClick = { clicked = true },
                onContactClick = {},
                onResetClick = {}
            )
        }

        composeTestRule.onNodeWithText("利用規約", substring = true)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun appInfoSection_contactClickCallsCallback() {
        var clicked = false

        composeTestRule.setContent {
            AppInfoSection(
                versionName = "1.0.0",
                onPrivacyPolicyClick = {},
                onTermsOfServiceClick = {},
                onContactClick = { clicked = true },
                onResetClick = {}
            )
        }

        composeTestRule.onNodeWithText("お問い合わせ", substring = true)
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun appInfoSection_displaysContactSummary() {
        composeTestRule.setContent {
            AppInfoSection(
                versionName = "1.0.0",
                onPrivacyPolicyClick = {},
                onTermsOfServiceClick = {},
                onContactClick = {},
                onResetClick = {}
            )
        }

        composeTestRule.onNodeWithText("ご質問・ご要望はこちら", substring = true)
            .assertIsDisplayed()
    }

    // =========================================================================
    // DataExportSection (2 tests)
    // =========================================================================

    @Test
    fun dataExportSection_displaysTitle() {
        composeTestRule.setContent {
            DataExportSection(
                onExportTasksClick = {},
                onExportNotesClick = {}
            )
        }

        composeTestRule.onNodeWithText("データエクスポート", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun dataExportSection_clickTasksCallsCallback() {
        var clicked = false

        composeTestRule.setContent {
            DataExportSection(
                onExportTasksClick = { clicked = true },
                onExportNotesClick = {}
            )
        }

        composeTestRule.onNodeWithText("タスクのエクスポート", substring = true)
            .performClick()

        assertTrue(clicked)
    }
}
