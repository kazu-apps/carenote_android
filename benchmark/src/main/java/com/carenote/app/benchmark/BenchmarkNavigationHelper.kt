package com.carenote.app.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

/**
 * Shared UIAutomator navigation helpers for macrobenchmark tests.
 *
 * Extension functions on [MacrobenchmarkScope] for common patterns:
 * tab navigation, FAB clicks, back navigation, and scroll gestures.
 */

const val PACKAGE_NAME = "com.carenote.app"
private const val DEFAULT_TIMEOUT = 5_000L
private const val SCROLL_STEPS = 10

/**
 * Tab labels matching strings.xml: 服薬 / 予定 / タスク / 記録 / メモ / 設定
 */
val TAB_LABELS = listOf("服薬", "予定", "タスク", "記録", "メモ")

/**
 * Navigate to a bottom navigation tab by its label.
 *
 * Tries [By.desc] first (content description), then falls back to [By.text].
 */
fun MacrobenchmarkScope.navigateToTab(label: String) {
    device.findObject(By.desc(label))?.click()
        ?: device.findObject(By.text(label))?.click()
    device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), DEFAULT_TIMEOUT)
}

/**
 * Click a FAB identified by its test tag.
 */
fun MacrobenchmarkScope.clickFab(testTag: String) {
    device.findObject(By.res(PACKAGE_NAME, testTag))?.click()
    device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), DEFAULT_TIMEOUT)
}

/**
 * Press the system back button and wait for the app to settle.
 */
fun MacrobenchmarkScope.navigateBack() {
    device.pressBack()
    device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), DEFAULT_TIMEOUT)
}

/**
 * Perform a scroll-down gesture (swipe up from bottom to top of screen).
 */
fun MacrobenchmarkScope.scrollDown() {
    val displayHeight = device.displayHeight
    val displayWidth = device.displayWidth
    device.swipe(
        displayWidth / 2,
        displayHeight * 3 / 4,
        displayWidth / 2,
        displayHeight / 4,
        SCROLL_STEPS
    )
    device.waitForIdle()
}

/**
 * Perform a scroll-up gesture (swipe down from top to bottom of screen).
 */
fun MacrobenchmarkScope.scrollUp() {
    val displayHeight = device.displayHeight
    val displayWidth = device.displayWidth
    device.swipe(
        displayWidth / 2,
        displayHeight / 4,
        displayWidth / 2,
        displayHeight * 3 / 4,
        SCROLL_STEPS
    )
    device.waitForIdle()
}
