package com.carenote.app.data.repository

import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.testing.assertDatabaseError
import com.carenote.app.testing.assertFailure
import com.carenote.app.testing.assertSuccess
import com.carenote.app.testing.assertValidationError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsRepositoryImplTest {

    private lateinit var dataSource: SettingsDataSource
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk(relaxed = true)
        every { dataSource.getSettings() } returns flowOf(UserSettings())
        repository = SettingsRepositoryImpl(dataSource)
    }

    @Test
    fun `getSettings delegates to dataSource`() = runTest {
        repository.getSettings()
        // Flow is returned from dataSource
    }

    @Test
    fun `updateNotifications success`() = runTest {
        coEvery { dataSource.updateNotifications(false) } returns Unit

        val result = repository.updateNotifications(false)

        result.assertSuccess()
        coVerify { dataSource.updateNotifications(false) }
    }

    @Test
    fun `updateQuietHours success with valid range`() = runTest {
        coEvery { dataSource.updateQuietHours(23, 6) } returns Unit

        val result = repository.updateQuietHours(23, 6)

        result.assertSuccess()
    }

    @Test
    fun `updateQuietHours failure with negative start`() = runTest {
        val result = repository.updateQuietHours(-1, 7)

        val error = result.assertValidationError()
        assertEquals("quietHoursStart", error.field)
    }

    @Test
    fun `updateQuietHours failure with end over 23`() = runTest {
        val result = repository.updateQuietHours(22, 24)

        val error = result.assertValidationError()
        assertEquals("quietHoursEnd", error.field)
    }

    @Test
    fun `updateTemperatureThreshold success`() = runTest {
        coEvery { dataSource.updateTemperatureThreshold(38.0) } returns Unit

        val result = repository.updateTemperatureThreshold(38.0)

        result.assertSuccess()
    }

    @Test
    fun `updateTemperatureThreshold failure with low value`() = runTest {
        val result = repository.updateTemperatureThreshold(33.0)

        val error = result.assertValidationError()
        assertEquals("temperatureHigh", error.field)
    }

    @Test
    fun `updateTemperatureThreshold failure with high value`() = runTest {
        val result = repository.updateTemperatureThreshold(43.0)

        result.assertFailure()
    }

    @Test
    fun `updateBloodPressureThresholds success`() = runTest {
        coEvery { dataSource.updateBloodPressureThresholds(150, 95) } returns Unit

        val result = repository.updateBloodPressureThresholds(150, 95)

        result.assertSuccess()
    }

    @Test
    fun `updateBloodPressureThresholds failure with upper out of range`() = runTest {
        val result = repository.updateBloodPressureThresholds(300, 90)

        result.assertValidationError()
    }

    @Test
    fun `updateBloodPressureThresholds failure with lower greater than upper`() = runTest {
        val result = repository.updateBloodPressureThresholds(100, 100)

        val error = result.assertValidationError()
        assertEquals("bloodPressureHighLower", error.field)
    }

    @Test
    fun `updatePulseThresholds success`() = runTest {
        coEvery { dataSource.updatePulseThresholds(110, 45) } returns Unit

        val result = repository.updatePulseThresholds(110, 45)

        result.assertSuccess()
    }

    @Test
    fun `updatePulseThresholds failure with high out of range`() = runTest {
        val result = repository.updatePulseThresholds(250, 50)

        result.assertFailure()
    }

    @Test
    fun `updatePulseThresholds failure with low greater than high`() = runTest {
        val result = repository.updatePulseThresholds(80, 80)

        val error = result.assertValidationError()
        assertEquals("pulseLow", error.field)
    }

    @Test
    fun `updateMedicationTime MORNING success`() = runTest {
        coEvery {
            dataSource.updateMedicationTime(MedicationTiming.MORNING, 7, 30)
        } returns Unit

        val result = repository.updateMedicationTime(MedicationTiming.MORNING, 7, 30)

        result.assertSuccess()
    }

    @Test
    fun `updateMedicationTime NOON success`() = runTest {
        coEvery {
            dataSource.updateMedicationTime(MedicationTiming.NOON, 11, 45)
        } returns Unit

        val result = repository.updateMedicationTime(MedicationTiming.NOON, 11, 45)

        result.assertSuccess()
    }

    @Test
    fun `updateMedicationTime failure with invalid hour`() = runTest {
        val result = repository.updateMedicationTime(MedicationTiming.EVENING, 25, 0)

        val error = result.assertValidationError()
        assertEquals("hour", error.field)
    }

    @Test
    fun `updateMedicationTime failure with invalid minute`() = runTest {
        val result = repository.updateMedicationTime(MedicationTiming.MORNING, 8, 60)

        val error = result.assertValidationError()
        assertEquals("minute", error.field)
    }

    @Test
    fun `updateThemeMode DARK success`() = runTest {
        coEvery { dataSource.updateThemeMode("DARK") } returns Unit

        val result = repository.updateThemeMode(ThemeMode.DARK)

        result.assertSuccess()
        coVerify { dataSource.updateThemeMode("DARK") }
    }

    @Test
    fun `updateThemeMode LIGHT success`() = runTest {
        coEvery { dataSource.updateThemeMode("LIGHT") } returns Unit

        val result = repository.updateThemeMode(ThemeMode.LIGHT)

        result.assertSuccess()
        coVerify { dataSource.updateThemeMode("LIGHT") }
    }

    @Test
    fun `updateThemeMode SYSTEM success`() = runTest {
        coEvery { dataSource.updateThemeMode("SYSTEM") } returns Unit

        val result = repository.updateThemeMode(ThemeMode.SYSTEM)

        result.assertSuccess()
        coVerify { dataSource.updateThemeMode("SYSTEM") }
    }

    @Test
    fun `updateAppLanguage JAPANESE success`() = runTest {
        coEvery { dataSource.updateAppLanguage("JAPANESE") } returns Unit

        val result = repository.updateAppLanguage(AppLanguage.JAPANESE)

        result.assertSuccess()
        coVerify { dataSource.updateAppLanguage("JAPANESE") }
    }

    @Test
    fun `updateAppLanguage ENGLISH success`() = runTest {
        coEvery { dataSource.updateAppLanguage("ENGLISH") } returns Unit

        val result = repository.updateAppLanguage(AppLanguage.ENGLISH)

        result.assertSuccess()
        coVerify { dataSource.updateAppLanguage("ENGLISH") }
    }

    @Test
    fun `updateAppLanguage SYSTEM success`() = runTest {
        coEvery { dataSource.updateAppLanguage("SYSTEM") } returns Unit

        val result = repository.updateAppLanguage(AppLanguage.SYSTEM)

        result.assertSuccess()
        coVerify { dataSource.updateAppLanguage("SYSTEM") }
    }

    @Test
    fun `updateAppLanguage returns Failure on db error`() = runTest {
        coEvery { dataSource.updateAppLanguage(any()) } throws RuntimeException("DB error")

        val result = repository.updateAppLanguage(AppLanguage.JAPANESE)

        result.assertDatabaseError()
    }

    @Test
    fun `resetToDefaults success`() = runTest {
        coEvery { dataSource.clearAll() } returns Unit

        val result = repository.resetToDefaults()

        result.assertSuccess()
        coVerify { dataSource.clearAll() }
    }

    // DB exception path tests

    @Test
    fun `updateNotifications returns Failure on db error`() = runTest {
        coEvery { dataSource.updateNotifications(any()) } throws RuntimeException("DB error")

        val result = repository.updateNotifications(false)

        result.assertDatabaseError()
    }

    @Test
    fun `updateQuietHours returns Failure on db error`() = runTest {
        coEvery { dataSource.updateQuietHours(any(), any()) } throws RuntimeException("DB error")

        val result = repository.updateQuietHours(22, 6)

        result.assertDatabaseError()
    }

    @Test
    fun `updateTemperatureThreshold returns Failure on db error`() = runTest {
        coEvery { dataSource.updateTemperatureThreshold(any()) } throws RuntimeException("DB error")

        val result = repository.updateTemperatureThreshold(38.0)

        result.assertDatabaseError()
    }

    @Test
    fun `updateBloodPressureThresholds returns Failure on db error`() = runTest {
        coEvery { dataSource.updateBloodPressureThresholds(any(), any()) } throws RuntimeException("DB error")

        val result = repository.updateBloodPressureThresholds(140, 90)

        result.assertDatabaseError()
    }

    @Test
    fun `updatePulseThresholds returns Failure on db error`() = runTest {
        coEvery { dataSource.updatePulseThresholds(any(), any()) } throws RuntimeException("DB error")

        val result = repository.updatePulseThresholds(100, 50)

        result.assertDatabaseError()
    }

    @Test
    fun `updateMedicationTime returns Failure on db error`() = runTest {
        coEvery {
            dataSource.updateMedicationTime(any(), any(), any())
        } throws RuntimeException("DB error")

        val result = repository.updateMedicationTime(MedicationTiming.MORNING, 8, 0)

        result.assertDatabaseError()
    }

    @Test
    fun `updateThemeMode returns Failure on db error`() = runTest {
        coEvery { dataSource.updateThemeMode(any()) } throws RuntimeException("DB error")

        val result = repository.updateThemeMode(ThemeMode.DARK)

        result.assertDatabaseError()
    }

    @Test
    fun `resetToDefaults returns Failure on db error`() = runTest {
        coEvery { dataSource.clearAll() } throws RuntimeException("DB error")

        val result = repository.resetToDefaults()

        result.assertDatabaseError()
    }

    @Test
    fun `updateSessionTimeout with valid value succeeds`() = runTest {
        coEvery { dataSource.updateSessionTimeout(10) } returns Unit

        val result = repository.updateSessionTimeout(10)

        result.assertSuccess()
        coVerify { dataSource.updateSessionTimeout(10) }
    }

    @Test
    fun `updateSessionTimeout below minimum returns ValidationError`() = runTest {
        val result = repository.updateSessionTimeout(0)

        val error = result.assertValidationError()
        assertEquals("sessionTimeoutMinutes", error.field)
    }

    @Test
    fun `updateSessionTimeout above maximum returns ValidationError`() = runTest {
        val result = repository.updateSessionTimeout(61)

        val error = result.assertValidationError()
        assertEquals("sessionTimeoutMinutes", error.field)
    }

    @Test
    fun `getSessionTimeoutMs returns value from dataSource`() = runTest {
        every { dataSource.getSessionTimeoutMs() } returns 600_000L

        val result = repository.getSessionTimeoutMs()

        assertEquals(600_000L, result)
    }

    @Test
    fun `updateSessionTimeout returns Failure on db error`() = runTest {
        coEvery { dataSource.updateSessionTimeout(any()) } throws RuntimeException("DB error")

        val result = repository.updateSessionTimeout(5)

        result.assertDatabaseError()
    }
}
