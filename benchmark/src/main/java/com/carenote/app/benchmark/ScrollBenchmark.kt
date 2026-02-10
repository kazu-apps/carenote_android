package com.carenote.app.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark tests for list scrolling performance.
 *
 * For each list screen (Medication, Tasks, Notes), navigates to the tab
 * and performs scroll-down / scroll-up gestures. Measures [FrameTimingMetric]
 * to detect janky frames during scrolling.
 *
 * Even with an empty list, the EmptyState + PullToRefresh frame rendering
 * provides useful baseline measurements.
 *
 * Run on a physical device:
 *   ./gradlew.bat :benchmark:connectedBenchmarkAndroidTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    // region Medication scroll

    @Test
    fun medicationScrollNoCompilation() {
        benchmarkScroll("服薬", CompilationMode.None())
    }

    @Test
    fun medicationScrollBaselineProfile() {
        benchmarkScroll("服薬", CompilationMode.Partial())
    }

    // endregion

    // region Tasks scroll

    @Test
    fun tasksScrollNoCompilation() {
        benchmarkScroll("タスク", CompilationMode.None())
    }

    @Test
    fun tasksScrollBaselineProfile() {
        benchmarkScroll("タスク", CompilationMode.Partial())
    }

    // endregion

    // region Notes scroll

    @Test
    fun notesScrollNoCompilation() {
        benchmarkScroll("メモ", CompilationMode.None())
    }

    @Test
    fun notesScrollBaselineProfile() {
        benchmarkScroll("メモ", CompilationMode.Partial())
    }

    // endregion

    private fun benchmarkScroll(tabLabel: String, compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.WARM,
            iterations = 5
        ) {
            pressHome()
            startActivityAndWait()

            navigateToTab(tabLabel)
            scrollDown()
            scrollUp()
        }
    }
}
