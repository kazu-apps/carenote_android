package com.carenote.app.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a Baseline Profile for CareNote.
 *
 * Records the main navigation flow through the 5 bottom nav screens:
 * Medication → Calendar → Tasks → HealthRecords → Notes.
 *
 * Run on a physical device or emulator:
 *   ./gradlew.bat :app:generateReleaseBaselineProfile
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateProfile() {
        rule.collect(
            packageName = "com.carenote.app",
            includeInStartupProfile = true
        ) {
            // Cold start the app
            pressHome()
            startActivityAndWait()

            val timeout = 5_000L

            // Navigate through bottom nav tabs
            // Tab labels match strings.xml: 服薬 / カレンダー / タスク / 健康記録 / メモ
            val tabDescriptions = listOf(
                "カレンダー",
                "タスク",
                "健康記録",
                "メモ",
                "服薬" // Return to first tab
            )

            for (tab in tabDescriptions) {
                device.findObject(By.desc(tab))?.click()
                    ?: device.findObject(By.text(tab))?.click()
                device.wait(Until.hasObject(By.pkg("com.carenote.app")), timeout)
            }
        }
    }
}
