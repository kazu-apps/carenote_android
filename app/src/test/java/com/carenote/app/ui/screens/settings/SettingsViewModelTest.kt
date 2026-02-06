package com.carenote.app.ui.screens.settings

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeSettingsRepository
import com.carenote.app.fakes.FakeSyncWorkScheduler
import com.carenote.app.ui.util.LocaleManager
import com.carenote.app.ui.util.SnackbarEvent
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var syncWorkScheduler: FakeSyncWorkScheduler
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = FakeSettingsRepository()
        authRepository = FakeAuthRepository()
        syncWorkScheduler = FakeSyncWorkScheduler()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(settingsRepository, authRepository, syncWorkScheduler)
    }

    @Test
    fun `settings flow emits default values initially`() = runTest(testDispatcher) {
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
    fun `toggleNotifications updates state to false`() = runTest(testDispatcher) {
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
    fun `toggleNotifications updates state to true`() = runTest(testDispatcher) {
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
    fun `updateQuietHours success updates state`() = runTest(testDispatcher) {
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
    fun `updateTemperatureThreshold success`() = runTest(testDispatcher) {
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
    fun `updateTemperatureThreshold with invalid value shows error`() = runTest(testDispatcher) {
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
    fun `updateBloodPressureThresholds success`() = runTest(testDispatcher) {
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
    fun `updatePulseThresholds success`() = runTest(testDispatcher) {
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
    fun `updateMedicationTime MORNING success`() = runTest(testDispatcher) {
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
    fun `updateMedicationTime NOON success`() = runTest(testDispatcher) {
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
    fun `updateMedicationTime EVENING success`() = runTest(testDispatcher) {
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
    fun `updateThemeMode DARK success`() = runTest(testDispatcher) {
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
    fun `updateThemeMode LIGHT success`() = runTest(testDispatcher) {
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
    fun `updateThemeMode SYSTEM success`() = runTest(testDispatcher) {
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
    fun `updateThemeMode failure shows error snackbar`() = runTest(testDispatcher) {
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
    fun `updateAppLanguage JAPANESE success`() = runTest(testDispatcher) {
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
    fun `updateAppLanguage ENGLISH success`() = runTest(testDispatcher) {
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
    fun `updateAppLanguage failure shows error snackbar`() = runTest(testDispatcher) {
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
    fun `resetToDefaults restores default values`() = runTest(testDispatcher) {
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
    fun `repository failure shows error snackbar`() = runTest(testDispatcher) {
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
    fun `updateAppLanguage calls LocaleManager applyLanguage on success`() = runTest(testDispatcher) {
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
    fun `updateAppLanguage does not call LocaleManager on failure`() = runTest(testDispatcher) {
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
    fun `settings reflect pre-set values`() = runTest(testDispatcher) {
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
}
