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
 * Macrobenchmark tests for FAB → AddEdit screen transitions.
 *
 * For each screen (Medication, Tasks, Notes), navigates to the tab,
 * clicks the FAB to open the AddEdit screen, then presses back.
 * Measures [FrameTimingMetric] for the transition animations.
 *
 * Run on a physical device:
 *   ./gradlew.bat :benchmark:connectedBenchmarkAndroidTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FABNavigationBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    // region Medication FAB

    @Test
    fun medicationFabNoCompilation() {
        benchmarkFab("服薬", "medication_fab", CompilationMode.None())
    }

    @Test
    fun medicationFabBaselineProfile() {
        benchmarkFab("服薬", "medication_fab", CompilationMode.Partial())
    }

    // endregion

    // region Tasks FAB

    @Test
    fun tasksFabNoCompilation() {
        benchmarkFab("タスク", "tasks_fab", CompilationMode.None())
    }

    @Test
    fun tasksFabBaselineProfile() {
        benchmarkFab("タスク", "tasks_fab", CompilationMode.Partial())
    }

    // endregion

    // region Notes FAB

    @Test
    fun notesFabNoCompilation() {
        benchmarkFab("メモ", "notes_fab", CompilationMode.None())
    }

    @Test
    fun notesFabBaselineProfile() {
        benchmarkFab("メモ", "notes_fab", CompilationMode.Partial())
    }

    // endregion

    private fun benchmarkFab(
        tabLabel: String,
        fabTestTag: String,
        compilationMode: CompilationMode
    ) {
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
            clickFab(fabTestTag)
            navigateBack()
        }
    }
}
