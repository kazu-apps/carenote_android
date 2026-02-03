package com.carenote.app.data.repository

import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

        assertTrue(result.isSuccess)
        coVerify { dataSource.updateNotifications(false) }
    }

    @Test
    fun `updateQuietHours success with valid range`() = runTest {
        coEvery { dataSource.updateQuietHours(23, 6) } returns Unit

        val result = repository.updateQuietHours(23, 6)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateQuietHours failure with negative start`() = runTest {
        val result = repository.updateQuietHours(-1, 7)

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.ValidationError)
        assertEquals("quietHoursStart", (error as DomainError.ValidationError).field)
    }

    @Test
    fun `updateQuietHours failure with end over 23`() = runTest {
        val result = repository.updateQuietHours(22, 24)

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.ValidationError)
        assertEquals("quietHoursEnd", (error as DomainError.ValidationError).field)
    }

    @Test
    fun `updateTemperatureThreshold success`() = runTest {
        coEvery { dataSource.updateTemperatureThreshold(38.0) } returns Unit

        val result = repository.updateTemperatureThreshold(38.0)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateTemperatureThreshold failure with low value`() = runTest {
        val result = repository.updateTemperatureThreshold(33.0)

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.ValidationError)
        assertEquals("temperatureHigh", (error as DomainError.ValidationError).field)
    }

    @Test
    fun `updateTemperatureThreshold failure with high value`() = runTest {
        val result = repository.updateTemperatureThreshold(43.0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateBloodPressureThresholds success`() = runTest {
        coEvery { dataSource.updateBloodPressureThresholds(150, 95) } returns Unit

        val result = repository.updateBloodPressureThresholds(150, 95)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateBloodPressureThresholds failure with upper out of range`() = runTest {
        val result = repository.updateBloodPressureThresholds(300, 90)

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.ValidationError)
    }

    @Test
    fun `updateBloodPressureThresholds failure with lower greater than upper`() = runTest {
        val result = repository.updateBloodPressureThresholds(100, 100)

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.ValidationError)
        assertEquals("bloodPressureHighLower", (error as DomainError.ValidationError).field)
    }

    @Test
    fun `updatePulseThresholds success`() = runTest {
        coEvery { dataSource.updatePulseThresholds(110, 45) } returns Unit

        val result = repository.updatePulseThresholds(110, 45)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updatePulseThresholds failure with high out of range`() = runTest {
        val result = repository.updatePulseThresholds(250, 50)

        assertTrue(result.isFailure)
    }

    @Test
    fun `updatePulseThresholds failure with low greater than high`() = runTest {
        val result = repository.updatePulseThresholds(80, 80)

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.ValidationError)
        assertEquals("pulseLow", (error as DomainError.ValidationError).field)
    }

    @Test
    fun `updateMedicationTime MORNING success`() = runTest {
        coEvery {
            dataSource.updateMedicationTime(
                SettingsDataSource.MORNING_HOUR_KEY,
                SettingsDataSource.MORNING_MINUTE_KEY,
                7,
                30
            )
        } returns Unit

        val result = repository.updateMedicationTime(MedicationTiming.MORNING, 7, 30)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateMedicationTime NOON success`() = runTest {
        coEvery {
            dataSource.updateMedicationTime(
                SettingsDataSource.NOON_HOUR_KEY,
                SettingsDataSource.NOON_MINUTE_KEY,
                11,
                45
            )
        } returns Unit

        val result = repository.updateMedicationTime(MedicationTiming.NOON, 11, 45)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateMedicationTime failure with invalid hour`() = runTest {
        val result = repository.updateMedicationTime(MedicationTiming.EVENING, 25, 0)

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.ValidationError)
        assertEquals("hour", (error as DomainError.ValidationError).field)
    }

    @Test
    fun `updateMedicationTime failure with invalid minute`() = runTest {
        val result = repository.updateMedicationTime(MedicationTiming.MORNING, 8, 60)

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.ValidationError)
        assertEquals("minute", (error as DomainError.ValidationError).field)
    }

    @Test
    fun `updateThemeMode DARK success`() = runTest {
        coEvery { dataSource.updateThemeMode("DARK") } returns Unit

        val result = repository.updateThemeMode(ThemeMode.DARK)

        assertTrue(result.isSuccess)
        coVerify { dataSource.updateThemeMode("DARK") }
    }

    @Test
    fun `updateThemeMode LIGHT success`() = runTest {
        coEvery { dataSource.updateThemeMode("LIGHT") } returns Unit

        val result = repository.updateThemeMode(ThemeMode.LIGHT)

        assertTrue(result.isSuccess)
        coVerify { dataSource.updateThemeMode("LIGHT") }
    }

    @Test
    fun `updateThemeMode SYSTEM success`() = runTest {
        coEvery { dataSource.updateThemeMode("SYSTEM") } returns Unit

        val result = repository.updateThemeMode(ThemeMode.SYSTEM)

        assertTrue(result.isSuccess)
        coVerify { dataSource.updateThemeMode("SYSTEM") }
    }

    @Test
    fun `resetToDefaults success`() = runTest {
        coEvery { dataSource.clearAll() } returns Unit

        val result = repository.resetToDefaults()

        assertTrue(result.isSuccess)
        coVerify { dataSource.clearAll() }
    }
}
