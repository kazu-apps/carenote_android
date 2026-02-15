package com.carenote.app.ui.screens.settings

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.User
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeBillingRepository
import com.carenote.app.fakes.FakeCareRecipientRepository
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeNoteCsvExporter
import com.carenote.app.fakes.FakeNotePdfExporter
import com.carenote.app.fakes.FakeNoteRepository
import com.carenote.app.fakes.FakePremiumFeatureGuard
import com.carenote.app.fakes.FakeRootDetector
import com.carenote.app.fakes.FakeSettingsRepository
import com.carenote.app.fakes.FakeSyncWorkScheduler
import com.carenote.app.fakes.FakeTaskCsvExporter
import com.carenote.app.fakes.FakeTaskPdfExporter
import com.carenote.app.fakes.FakeTaskRepository
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.testing.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * SettingsViewModel の update メソッド群が示す Snackbar パターンを網羅的に検証するテスト。
 *
 * 既存の SettingsViewModelTest は state 更新を検証する。
 * このテストは成功/失敗時の Snackbar メッセージパターンを検証し、
 * Item 96 の updateSetting() 統合リファクタリングの安全網として機能する。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelUpdateTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var syncWorkScheduler: FakeSyncWorkScheduler
    private lateinit var careRecipientRepository: FakeCareRecipientRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var taskCsvExporter: FakeTaskCsvExporter
    private lateinit var taskPdfExporter: FakeTaskPdfExporter
    private lateinit var noteCsvExporter: FakeNoteCsvExporter
    private lateinit var notePdfExporter: FakeNotePdfExporter
    private lateinit var rootDetector: FakeRootDetector
    private lateinit var billingRepository: FakeBillingRepository
    private lateinit var premiumFeatureGuard: FakePremiumFeatureGuard
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository()
        authRepository = FakeAuthRepository()
        syncWorkScheduler = FakeSyncWorkScheduler()
        careRecipientRepository = FakeCareRecipientRepository()
        analyticsRepository = FakeAnalyticsRepository()
        taskRepository = FakeTaskRepository()
        noteRepository = FakeNoteRepository()
        taskCsvExporter = FakeTaskCsvExporter()
        taskPdfExporter = FakeTaskPdfExporter()
        noteCsvExporter = FakeNoteCsvExporter()
        notePdfExporter = FakeNotePdfExporter()
        rootDetector = FakeRootDetector()
        billingRepository = FakeBillingRepository()
        premiumFeatureGuard = FakePremiumFeatureGuard()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            settingsRepository, authRepository, syncWorkScheduler,
            analyticsRepository, careRecipientRepository,
            taskRepository, noteRepository,
            taskCsvExporter, taskPdfExporter,
            noteCsvExporter, notePdfExporter,
            rootDetector,
            billingRepository, premiumFeatureGuard
        )
    }

    // --- 汎用パターン: 成功時 Snackbar ---

    @Test
    fun `toggleNotifications success shows saved snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_saved,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `toggleNotifications failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        settingsRepository.shouldFail = true

        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_error_save_failed,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `updateQuietHours success shows saved snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateQuietHours(22, 7)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_saved,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `updateQuietHours failure shows validation error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        settingsRepository.shouldFail = true

        viewModel.updateQuietHours(22, 7)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_error_validation,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `updateTemperatureThreshold success shows saved snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateTemperatureThreshold(38.0)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_saved,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `updateThemeMode success shows saved snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_saved,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `updateMedicationTime success shows saved snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMedicationTime(MedicationTiming.MORNING, 7, 0)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_saved,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `updateBloodPressureThresholds success shows saved snackbar`() =
        runTest(mainCoroutineRule.testDispatcher) {
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateBloodPressureThresholds(150, 95)
            advanceUntilIdle()

            viewModel.snackbarController.events.test {
                advanceUntilIdle()
                val event = expectMostRecentItem()
                assertTrue(event is SnackbarEvent.WithResId)
                assertEquals(
                    R.string.settings_saved,
                    (event as SnackbarEvent.WithResId).messageResId
                )
            }
        }

    @Test
    fun `updatePulseThresholds success shows saved snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePulseThresholds(110, 45)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_saved,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    // --- toggleDynamicColor パターン ---

    @Test
    fun `toggleDynamicColor success shows saved snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleDynamicColor(true)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_saved,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `toggleDynamicColor failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        settingsRepository.shouldFail = true

        viewModel.toggleDynamicColor(true)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_error_save_failed,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    // --- toggleSyncEnabled: 副作用パターン ---

    @Test
    fun `toggleSyncEnabled success schedules sync when enabled and logged in`() =
        runTest(mainCoroutineRule.testDispatcher) {
            authRepository.setCurrentUser(
                User(
                    uid = "test-uid",
                    email = "test@example.com",
                    name = "Test User",
                    createdAt = LocalDateTime.now(),
                    isPremium = false,
                    isEmailVerified = true
                )
            )
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.toggleSyncEnabled(true)
            advanceUntilIdle()

            assertEquals(1, syncWorkScheduler.schedulePeriodicSyncCallCount)
            assertEquals(0, syncWorkScheduler.cancelAllSyncWorkCallCount)

            viewModel.snackbarController.events.test {
                advanceUntilIdle()
                val event = expectMostRecentItem()
                assertTrue(event is SnackbarEvent.WithResId)
                assertEquals(
                    R.string.settings_saved,
                    (event as SnackbarEvent.WithResId).messageResId
                )
            }
        }

    @Test
    fun `toggleSyncEnabled success cancels sync when disabled`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleSyncEnabled(false)
        advanceUntilIdle()

        assertEquals(1, syncWorkScheduler.cancelAllSyncWorkCallCount)
        assertEquals(0, syncWorkScheduler.schedulePeriodicSyncCallCount)

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_saved,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    // --- resetToDefaults: カスタムメッセージパターン ---

    @Test
    fun `resetToDefaults success shows reset done snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.resetToDefaults()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.settings_reset_done,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }
}
