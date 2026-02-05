package com.carenote.app.fakes

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository : SettingsRepository {

    private val settings = MutableStateFlow(UserSettings())
    var shouldFail = false

    fun setSettings(userSettings: UserSettings) {
        settings.value = userSettings
    }

    fun clear() {
        settings.value = UserSettings()
        shouldFail = false
    }

    override fun getSettings(): Flow<UserSettings> = settings

    override suspend fun updateNotifications(
        enabled: Boolean
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        settings.value = settings.value.copy(notificationsEnabled = enabled)
        return Result.Success(Unit)
    }

    override suspend fun updateQuietHours(
        start: Int,
        end: Int
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        if (start !in AppConfig.Time.HOUR_MIN..AppConfig.Time.HOUR_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Quiet hours start must be between ${AppConfig.Time.HOUR_MIN} and ${AppConfig.Time.HOUR_MAX}",
                    field = "quietHoursStart"
                )
            )
        }
        if (end !in AppConfig.Time.HOUR_MIN..AppConfig.Time.HOUR_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Quiet hours end must be between ${AppConfig.Time.HOUR_MIN} and ${AppConfig.Time.HOUR_MAX}",
                    field = "quietHoursEnd"
                )
            )
        }
        settings.value = settings.value.copy(
            quietHoursStart = start,
            quietHoursEnd = end
        )
        return Result.Success(Unit)
    }

    override suspend fun updateTemperatureThreshold(
        value: Double
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        if (value < AppConfig.HealthRecord.TEMPERATURE_MIN ||
            value > AppConfig.HealthRecord.TEMPERATURE_MAX
        ) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Temperature must be between ${AppConfig.HealthRecord.TEMPERATURE_MIN} and ${AppConfig.HealthRecord.TEMPERATURE_MAX}",
                    field = "temperatureHigh"
                )
            )
        }
        settings.value = settings.value.copy(temperatureHigh = value)
        return Result.Success(Unit)
    }

    override suspend fun updateBloodPressureThresholds(
        upper: Int,
        lower: Int
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        if (upper !in AppConfig.HealthRecord.BLOOD_PRESSURE_MIN..AppConfig.HealthRecord.BLOOD_PRESSURE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Blood pressure upper must be between ${AppConfig.HealthRecord.BLOOD_PRESSURE_MIN} and ${AppConfig.HealthRecord.BLOOD_PRESSURE_MAX}",
                    field = "bloodPressureHighUpper"
                )
            )
        }
        if (lower !in AppConfig.HealthRecord.BLOOD_PRESSURE_MIN..AppConfig.HealthRecord.BLOOD_PRESSURE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Blood pressure lower must be between ${AppConfig.HealthRecord.BLOOD_PRESSURE_MIN} and ${AppConfig.HealthRecord.BLOOD_PRESSURE_MAX}",
                    field = "bloodPressureHighLower"
                )
            )
        }
        if (lower >= upper) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Blood pressure lower must be less than upper",
                    field = "bloodPressureHighLower"
                )
            )
        }
        settings.value = settings.value.copy(
            bloodPressureHighUpper = upper,
            bloodPressureHighLower = lower
        )
        return Result.Success(Unit)
    }

    override suspend fun updatePulseThresholds(
        high: Int,
        low: Int
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        if (high !in AppConfig.HealthRecord.PULSE_MIN..AppConfig.HealthRecord.PULSE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Pulse high must be between ${AppConfig.HealthRecord.PULSE_MIN} and ${AppConfig.HealthRecord.PULSE_MAX}",
                    field = "pulseHigh"
                )
            )
        }
        if (low !in AppConfig.HealthRecord.PULSE_MIN..AppConfig.HealthRecord.PULSE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Pulse low must be between ${AppConfig.HealthRecord.PULSE_MIN} and ${AppConfig.HealthRecord.PULSE_MAX}",
                    field = "pulseLow"
                )
            )
        }
        if (low >= high) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Pulse low must be less than high",
                    field = "pulseLow"
                )
            )
        }
        settings.value = settings.value.copy(pulseHigh = high, pulseLow = low)
        return Result.Success(Unit)
    }

    override suspend fun updateMedicationTime(
        timing: MedicationTiming,
        hour: Int,
        minute: Int
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        if (hour !in AppConfig.Time.HOUR_MIN..AppConfig.Time.HOUR_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Hour must be between ${AppConfig.Time.HOUR_MIN} and ${AppConfig.Time.HOUR_MAX}",
                    field = "hour"
                )
            )
        }
        if (minute !in AppConfig.Time.MINUTE_MIN..AppConfig.Time.MINUTE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Minute must be between ${AppConfig.Time.MINUTE_MIN} and ${AppConfig.Time.MINUTE_MAX}",
                    field = "minute"
                )
            )
        }
        settings.value = when (timing) {
            MedicationTiming.MORNING -> settings.value.copy(
                morningHour = hour,
                morningMinute = minute
            )
            MedicationTiming.NOON -> settings.value.copy(
                noonHour = hour,
                noonMinute = minute
            )
            MedicationTiming.EVENING -> settings.value.copy(
                eveningHour = hour,
                eveningMinute = minute
            )
        }
        return Result.Success(Unit)
    }

    override suspend fun updateThemeMode(
        mode: ThemeMode
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        settings.value = settings.value.copy(themeMode = mode)
        return Result.Success(Unit)
    }

    override suspend fun updateAppLanguage(
        language: AppLanguage
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        settings.value = settings.value.copy(appLanguage = language)
        return Result.Success(Unit)
    }

    override suspend fun updateSyncEnabled(
        enabled: Boolean
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        settings.value = settings.value.copy(syncEnabled = enabled)
        return Result.Success(Unit)
    }

    override suspend fun resetToDefaults(): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        settings.value = UserSettings()
        return Result.Success(Unit)
    }
}
