package com.carenote.app.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark tests for CareNote startup performance.
 *
 * Measures cold and warm startup with and without Baseline Profile compilation.
 *
 * Run on a physical device:
 *   ./gradlew.bat :benchmark:connectedBenchmarkAndroidTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    /**
     * Cold startup without any AOT compilation.
     * This is the worst-case baseline.
     */
    @Test
    fun startupColdNoCompilation() {
        benchmark(
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD
        )
    }

    /**
     * Cold startup with Baseline Profile (partial AOT compilation).
     * This represents the typical user experience after install/update.
     */
    @Test
    fun startupColdBaselineProfile() {
        benchmark(
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.COLD
        )
    }

    /**
     * Warm startup without any AOT compilation.
     */
    @Test
    fun startupWarmNoCompilation() {
        benchmark(
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM
        )
    }

    /**
     * Warm startup with Baseline Profile (partial AOT compilation).
     */
    @Test
    fun startupWarmBaselineProfile() {
        benchmark(
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.WARM
        )
    }

    private fun benchmark(compilationMode: CompilationMode, startupMode: StartupMode) {
        rule.measureRepeated(
            packageName = "com.carenote.app",
            metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = startupMode,
            iterations = 5
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
