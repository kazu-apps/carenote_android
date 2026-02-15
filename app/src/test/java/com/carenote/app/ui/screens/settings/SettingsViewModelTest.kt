package com.carenote.app.ui.screens.settings

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
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
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.Task
import com.carenote.app.ui.util.LocaleManager
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.ExportState
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import com.carenote.app.testing.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

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

    @Test
    fun `settings flow emits default values initially`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(AppConfig.Notification.DEFAULT_QUIET_HOURS_START, settings.quietHoursStart)
            assertEquals(AppConfig.Notification.DEFAULT_QUIET_HOURS_END, settings.quietHoursEnd)
            assertTrue(settings.notificationsEnabled)
            assertEquals(AppConfig.HealthThresholds.TEMPERATURE_HIGH, settings.temperatureHigh, 0.01)
        }
    }

    @Test
    fun `toggleNotifications updates state to false`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertFalse(settings.notificationsEnabled)
        }
    }

    @Test
    fun `toggleNotifications updates state to true`() = runTest(mainCoroutineRule.testDispatcher) {
        settingsRepository.setSettings(UserSettings(notificationsEnabled = false))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleNotifications(true)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertTrue(settings.notificationsEnabled)
        }
    }

    @Test
    fun `updateQuietHours success updates state`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateQuietHours(23, 6)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(23, settings.quietHoursStart)
            assertEquals(6, settings.quietHoursEnd)
        }
    }

    @Test
    fun `updateTemperatureThreshold success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateTemperatureThreshold(38.0)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(38.0, settings.temperatureHigh, 0.01)
        }
    }

    @Test
    fun `updateTemperatureThreshold with invalid value shows error`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateTemperatureThreshold(50.0)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_error_validation, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `updateBloodPressureThresholds success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateBloodPressureThresholds(150, 95)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(150, settings.bloodPressureHighUpper)
            assertEquals(95, settings.bloodPressureHighLower)
        }
    }

    @Test
    fun `updatePulseThresholds success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePulseThresholds(110, 45)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(110, settings.pulseHigh)
            assertEquals(45, settings.pulseLow)
        }
    }

    @Test
    fun `updateMedicationTime MORNING success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMedicationTime(MedicationTiming.MORNING, 7, 30)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(7, settings.morningHour)
            assertEquals(30, settings.morningMinute)
        }
    }

    @Test
    fun `updateMedicationTime NOON success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMedicationTime(MedicationTiming.NOON, 11, 45)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(11, settings.noonHour)
            assertEquals(45, settings.noonMinute)
        }
    }

    @Test
    fun `updateMedicationTime EVENING success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMedicationTime(MedicationTiming.EVENING, 19, 0)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(19, settings.eveningHour)
            assertEquals(0, settings.eveningMinute)
        }
    }

    @Test
    fun `updateThemeMode DARK success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(ThemeMode.DARK, settings.themeMode)
        }
    }

    @Test
    fun `updateThemeMode LIGHT success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(ThemeMode.LIGHT, settings.themeMode)
        }
    }

    @Test
    fun `updateThemeMode SYSTEM success`() = runTest(mainCoroutineRule.testDispatcher) {
        settingsRepository.setSettings(UserSettings(themeMode = ThemeMode.DARK))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateThemeMode(ThemeMode.SYSTEM)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        }
    }

    @Test
    fun `updateThemeMode failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        settingsRepository.shouldFail = true

        viewModel.updateThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_error_save_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `updateAppLanguage JAPANESE success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAppLanguage(AppLanguage.JAPANESE)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(AppLanguage.JAPANESE, settings.appLanguage)
        }
    }

    @Test
    fun `updateAppLanguage ENGLISH success`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAppLanguage(AppLanguage.ENGLISH)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(AppLanguage.ENGLISH, settings.appLanguage)
        }
    }

    @Test
    fun `updateAppLanguage failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        settingsRepository.shouldFail = true

        viewModel.updateAppLanguage(AppLanguage.JAPANESE)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_error_save_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `resetToDefaults restores default values`() = runTest(mainCoroutineRule.testDispatcher) {
        settingsRepository.setSettings(
            UserSettings(
                themeMode = ThemeMode.DARK,
                notificationsEnabled = false,
                temperatureHigh = 39.0,
                quietHoursStart = 20,
                morningHour = 6
            )
        )
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.resetToDefaults()
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(ThemeMode.SYSTEM, settings.themeMode)
            assertTrue(settings.notificationsEnabled)
            assertEquals(AppConfig.HealthThresholds.TEMPERATURE_HIGH, settings.temperatureHigh, 0.01)
            assertEquals(AppConfig.Notification.DEFAULT_QUIET_HOURS_START, settings.quietHoursStart)
            assertEquals(AppConfig.Medication.DEFAULT_MORNING_HOUR, settings.morningHour)
        }
    }

    @Test
    fun `repository failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        settingsRepository.shouldFail = true

        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_error_save_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `updateAppLanguage calls LocaleManager applyLanguage on success`() = runTest(mainCoroutineRule.testDispatcher) {
        mockkObject(LocaleManager)
        every { LocaleManager.applyLanguage(any()) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAppLanguage(AppLanguage.ENGLISH)
        advanceUntilIdle()

        verify(exactly = 1) { LocaleManager.applyLanguage(AppLanguage.ENGLISH) }
        unmockkObject(LocaleManager)
    }

    @Test
    fun `updateAppLanguage does not call LocaleManager on failure`() = runTest(mainCoroutineRule.testDispatcher) {
        mockkObject(LocaleManager)
        every { LocaleManager.applyLanguage(any()) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()
        settingsRepository.shouldFail = true

        viewModel.updateAppLanguage(AppLanguage.JAPANESE)
        advanceUntilIdle()

        verify(exactly = 0) { LocaleManager.applyLanguage(any()) }
        unmockkObject(LocaleManager)
    }

    @Test
    fun `toggleDynamicColor true updates state`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleDynamicColor(true)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertTrue(settings.useDynamicColor)
        }
    }

    @Test
    fun `toggleDynamicColor false updates state`() = runTest(mainCoroutineRule.testDispatcher) {
        settingsRepository.setSettings(UserSettings(useDynamicColor = true))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleDynamicColor(false)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertFalse(settings.useDynamicColor)
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
            assertEquals(R.string.settings_error_save_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `toggleBiometricEnabled true updates state`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleBiometricEnabled(true)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertTrue(settings.biometricEnabled)
        }
    }

    @Test
    fun `toggleBiometricEnabled false updates state`() = runTest(mainCoroutineRule.testDispatcher) {
        settingsRepository.setSettings(UserSettings(biometricEnabled = true))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleBiometricEnabled(false)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertFalse(settings.biometricEnabled)
        }
    }

    @Test
    fun `toggleBiometricEnabled failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        settingsRepository.shouldFail = true

        viewModel.toggleBiometricEnabled(true)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_error_save_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `settings reflect pre-set values`() = runTest(mainCoroutineRule.testDispatcher) {
        settingsRepository.setSettings(
            UserSettings(
                notificationsEnabled = false,
                quietHoursStart = 20,
                quietHoursEnd = 8,
                temperatureHigh = 38.5,
                bloodPressureHighUpper = 160,
                bloodPressureHighLower = 100,
                pulseHigh = 120,
                pulseLow = 40,
                morningHour = 7,
                morningMinute = 30,
                noonHour = 11,
                noonMinute = 30,
                eveningHour = 19,
                eveningMinute = 30
            )
        )
        viewModel = createViewModel()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertFalse(settings.notificationsEnabled)
            assertEquals(20, settings.quietHoursStart)
            assertEquals(8, settings.quietHoursEnd)
            assertEquals(38.5, settings.temperatureHigh, 0.01)
            assertEquals(160, settings.bloodPressureHighUpper)
            assertEquals(100, settings.bloodPressureHighLower)
            assertEquals(120, settings.pulseHigh)
            assertEquals(40, settings.pulseLow)
            assertEquals(7, settings.morningHour)
            assertEquals(30, settings.morningMinute)
        }
    }

    // --- Account Management Tests ---

    @Test
    fun `signOut success clears user and shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        authRepository.setCurrentUser(
            com.carenote.app.domain.model.User(
                uid = "test-uid",
                email = "test@example.com",
                name = "Test",
                createdAt = java.time.LocalDateTime.now()
            )
        )
        viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.isLoggedIn.value)

        viewModel.signOut()
        advanceUntilIdle()

        viewModel.isLoggedIn.test {
            advanceUntilIdle()
            assertFalse(expectMostRecentItem())
        }
        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_signed_out, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `signOut failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        authRepository.setCurrentUser(
            com.carenote.app.domain.model.User(
                uid = "test-uid",
                email = "test@example.com",
                name = "Test",
                createdAt = java.time.LocalDateTime.now()
            )
        )
        viewModel = createViewModel()
        advanceUntilIdle()
        authRepository.shouldFail = true

        viewModel.signOut()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_sign_out_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `changePassword success shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.changePassword("oldpass", "newpassword")
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_password_changed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `changePassword failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Reauthenticate succeeds but updatePassword fails
        // We need a more nuanced approach: first call succeeds, second fails
        // Since FakeAuthRepository uses a single shouldFail flag, we test the case where
        // reauthenticate fails (which is the more critical security path)
        authRepository.shouldFail = true

        viewModel.changePassword("oldpass", "newpassword")
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_reauthenticate_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `deleteAccount success clears user and shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        authRepository.setCurrentUser(
            com.carenote.app.domain.model.User(
                uid = "test-uid",
                email = "test@example.com",
                name = "Test",
                createdAt = java.time.LocalDateTime.now()
            )
        )
        viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.isLoggedIn.value)

        viewModel.deleteAccount("password")
        advanceUntilIdle()

        viewModel.isLoggedIn.test {
            advanceUntilIdle()
            assertFalse(expectMostRecentItem())
        }
        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_account_deleted, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `deleteAccount failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        authRepository.shouldFail = true

        viewModel.deleteAccount("password")
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_reauthenticate_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `sendEmailVerification success shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sendEmailVerification()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_email_verification_sent, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `sendEmailVerification failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        authRepository.shouldFail = true

        viewModel.sendEmailVerification()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_email_verification_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `currentUser emits user when logged in`() = runTest(mainCoroutineRule.testDispatcher) {
        val user = com.carenote.app.domain.model.User(
            uid = "test-uid",
            email = "test@example.com",
            name = "Test User",
            createdAt = java.time.LocalDateTime.now(),
            isEmailVerified = true
        )
        authRepository.setCurrentUser(user)
        viewModel = createViewModel()

        viewModel.currentUser.test {
            advanceUntilIdle()
            val currentUser = expectMostRecentItem()
            assertEquals("test-uid", currentUser?.uid)
            assertTrue(currentUser?.isEmailVerified == true)
        }
    }

    @Test
    fun `currentUser emits null when not logged in`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.currentUser.test {
            advanceUntilIdle()
            val currentUser = expectMostRecentItem()
            assertNull(currentUser)
        }
    }

    // --- Task Export Tests ---

    @Test
    fun `exportTasksCsv with empty list shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksCsv()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.task_export_empty, (event as SnackbarEvent.WithResId).messageResId)
        }
        assertEquals(ExportState.Idle, viewModel.exportState.value)
    }

    @Test
    fun `exportTasksCsv success updates exportState`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksCsv()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Success)
        assertEquals(1, taskCsvExporter.exportCallCount)
    }

    @Test
    fun `exportTasksPdf success updates exportState`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksPdf()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Success)
        assertEquals(1, taskPdfExporter.exportCallCount)
    }

    @Test
    fun `exportTasksCsv failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        taskCsvExporter.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksCsv()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.export_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
        assertTrue(viewModel.exportState.value is ExportState.Error)
    }

    @Test
    fun `exportTasksCsv logs analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksCsv()
        advanceUntilIdle()

        assertTrue(
            analyticsRepository.loggedEvents.any { it.first == AppConfig.Analytics.EVENT_TASK_EXPORT_CSV }
        )
    }

    @Test
    fun `exportTasksPdf logs analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksPdf()
        advanceUntilIdle()

        assertTrue(
            analyticsRepository.loggedEvents.any { it.first == AppConfig.Analytics.EVENT_TASK_EXPORT_PDF }
        )
    }

    // --- Note Export Tests ---

    @Test
    fun `exportNotesCsv with empty list shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesCsv()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.note_export_empty, (event as SnackbarEvent.WithResId).messageResId)
        }
        assertEquals(ExportState.Idle, viewModel.exportState.value)
    }

    @Test
    fun `exportNotesCsv success updates exportState`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesCsv()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Success)
        assertEquals(1, noteCsvExporter.exportCallCount)
    }

    @Test
    fun `exportNotesPdf success updates exportState`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesPdf()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Success)
        assertEquals(1, notePdfExporter.exportCallCount)
    }

    @Test
    fun `exportNotesCsv failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        noteCsvExporter.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesCsv()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.export_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
        assertTrue(viewModel.exportState.value is ExportState.Error)
    }

    @Test
    fun `exportNotesCsv logs analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesCsv()
        advanceUntilIdle()

        assertTrue(
            analyticsRepository.loggedEvents.any { it.first == AppConfig.Analytics.EVENT_NOTE_EXPORT_CSV }
        )
    }

    @Test
    fun `exportNotesPdf logs analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesPdf()
        advanceUntilIdle()

        assertTrue(
            analyticsRepository.loggedEvents.any { it.first == AppConfig.Analytics.EVENT_NOTE_EXPORT_PDF }
        )
    }

    // --- Export State Tests ---

    @Test
    fun `resetExportState sets state to Idle`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksCsv()
        advanceUntilIdle()
        assertTrue(viewModel.exportState.value is ExportState.Success)

        viewModel.resetExportState()
        assertEquals(ExportState.Idle, viewModel.exportState.value)
    }

    // --- Root Detection Export Block Tests ---

    @Test
    fun `exportTasksCsv on rooted device shows blocked snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        rootDetector.isRooted = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksCsv()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.security_root_export_blocked, (event as SnackbarEvent.WithResId).messageResId)
        }
        assertEquals(0, taskCsvExporter.exportCallCount)
    }

    @Test
    fun `exportTasksPdf on rooted device shows blocked snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        rootDetector.isRooted = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksPdf()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.security_root_export_blocked, (event as SnackbarEvent.WithResId).messageResId)
        }
        assertEquals(0, taskPdfExporter.exportCallCount)
    }

    @Test
    fun `exportNotesCsv on rooted device shows blocked snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        rootDetector.isRooted = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesCsv()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.security_root_export_blocked, (event as SnackbarEvent.WithResId).messageResId)
        }
        assertEquals(0, noteCsvExporter.exportCallCount)
    }

    @Test
    fun `exportNotesPdf on rooted device shows blocked snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        rootDetector.isRooted = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesPdf()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.security_root_export_blocked, (event as SnackbarEvent.WithResId).messageResId)
        }
        assertEquals(0, notePdfExporter.exportCallCount)
    }

    @Test
    fun `exportTasksCsv on non-rooted device succeeds normally`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        rootDetector.isRooted = false
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksCsv()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Success)
        assertEquals(1, taskCsvExporter.exportCallCount)
    }

    @Test
    fun `exportTasksPdf on non-rooted device succeeds normally`() = runTest(mainCoroutineRule.testDispatcher) {
        taskRepository.insertTask(Task(id = 0, title = "Test Task"))
        rootDetector.isRooted = false
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportTasksPdf()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Success)
        assertEquals(1, taskPdfExporter.exportCallCount)
    }

    @Test
    fun `exportNotesCsv on non-rooted device succeeds normally`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        rootDetector.isRooted = false
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesCsv()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Success)
        assertEquals(1, noteCsvExporter.exportCallCount)
    }

    @Test
    fun `exportNotesPdf on non-rooted device succeeds normally`() = runTest(mainCoroutineRule.testDispatcher) {
        noteRepository.insertNote(Note(id = 0, title = "Test Note", content = "Content"))
        rootDetector.isRooted = false
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportNotesPdf()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Success)
        assertEquals(1, notePdfExporter.exportCallCount)
    }

    // --- Session Timeout Tests ---

    @Test
    fun `updateSessionTimeout updates settings`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSessionTimeout(10)
        advanceUntilIdle()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(10, settings.sessionTimeoutMinutes)
        }
    }

    @Test
    fun `updateSessionTimeout with invalid value shows error`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSessionTimeout(0) // Below minimum
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.settings_error_validation, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `session timeout default is 5 minutes`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.settings.test {
            advanceUntilIdle()
            val settings = expectMostRecentItem()
            assertEquals(AppConfig.Session.DEFAULT_TIMEOUT_MINUTES, settings.sessionTimeoutMinutes)
        }
    }

    // --- Task Reminder Limit Tests ---

    @Test
    fun `taskReminderLimitText shows count for free user`() = runTest(mainCoroutineRule.testDispatcher) {
        premiumFeatureGuard.todayCount = 1
        viewModel = createViewModel()

        viewModel.taskReminderLimitText.test {
            advanceUntilIdle()
            val text = expectMostRecentItem()
            assertEquals("2/3", text)
        }
    }

    @Test
    fun `taskReminderLimitText is null for premium user`() = runTest(mainCoroutineRule.testDispatcher) {
        billingRepository.setPremiumActive()
        viewModel = createViewModel()

        viewModel.taskReminderLimitText.test {
            advanceUntilIdle()
            val text = expectMostRecentItem()
            assertNull(text)
        }
    }

    @Test
    fun `taskReminderLimitText shows zero remaining at limit`() = runTest(mainCoroutineRule.testDispatcher) {
        premiumFeatureGuard.todayCount = 3
        viewModel = createViewModel()

        viewModel.taskReminderLimitText.test {
            advanceUntilIdle()
            val text = expectMostRecentItem()
            assertEquals("0/3", text)
        }
    }

    @Test
    fun `taskReminderLimitText shows full count when no reminders sent`() = runTest(mainCoroutineRule.testDispatcher) {
        premiumFeatureGuard.todayCount = 0
        viewModel = createViewModel()

        viewModel.taskReminderLimitText.test {
            advanceUntilIdle()
            val text = expectMostRecentItem()
            assertEquals("3/3", text)
        }
    }
}
