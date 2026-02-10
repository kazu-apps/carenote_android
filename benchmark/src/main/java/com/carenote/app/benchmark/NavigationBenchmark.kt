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
 * Macrobenchmark tests for bottom navigation tab transitions.
 *
 * Navigates through all 5 tabs sequentially and measures [FrameTimingMetric]
 * to detect janky frames during screen transitions.
 *
 * Run on a physical device:
 *   ./gradlew.bat :benchmark:connectedBenchmarkAndroidTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun tabNavigationNoCompilation() {
        benchmarkTabNavigation(CompilationMode.None())
    }

    @Test
    fun tabNavigationBaselineProfile() {
        benchmarkTabNavigation(CompilationMode.Partial())
    }

    private fun benchmarkTabNavigation(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.WARM,
            iterations = 5
        ) {
            pressHome()
            startActivityAndWait()

            for (tab in TAB_LABELS) {
                navigateToTab(tab)
            }
        }
    }
}
