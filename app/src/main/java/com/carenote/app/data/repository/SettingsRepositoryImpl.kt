package com.carenote.app.data.repository

import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataSource: SettingsDataSource
) : SettingsRepository {

    override fun getSettings(): Flow<UserSettings> {
        return dataSource.getSettings()
    }

    override suspend fun updateNotifications(
        enabled: Boolean
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save notification setting", it) }
        ) {
            dataSource.updateNotifications(enabled)
            Timber.d("Notification setting updated: $enabled")
        }
    }

    override suspend fun updateQuietHours(
        start: Int,
        end: Int
    ): Result<Unit, DomainError> {
        if (start !in HOUR_MIN..HOUR_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Quiet hours start must be between $HOUR_MIN and $HOUR_MAX",
                    field = "quietHoursStart"
                )
            )
        }
        if (end !in HOUR_MIN..HOUR_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Quiet hours end must be between $HOUR_MIN and $HOUR_MAX",
                    field = "quietHoursEnd"
                )
            )
        }
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save quiet hours", it) }
        ) {
            dataSource.updateQuietHours(start, end)
            Timber.d("Quiet hours updated: $start - $end")
        }
    }

    override suspend fun updateTemperatureThreshold(
        value: Double
    ): Result<Unit, DomainError> {
        if (value < AppConfig.HealthRecord.TEMPERATURE_MIN ||
            value > AppConfig.HealthRecord.TEMPERATURE_MAX
        ) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Temperature must be between " +
                        "${AppConfig.HealthRecord.TEMPERATURE_MIN} and " +
                        "${AppConfig.HealthRecord.TEMPERATURE_MAX}",
                    field = "temperatureHigh"
                )
            )
        }
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save temperature threshold", it) }
        ) {
            dataSource.updateTemperatureThreshold(value)
            Timber.d("Temperature threshold updated: $value")
        }
    }

    override suspend fun updateBloodPressureThresholds(
        upper: Int,
        lower: Int
    ): Result<Unit, DomainError> {
        if (upper !in AppConfig.HealthRecord.BLOOD_PRESSURE_MIN..AppConfig.HealthRecord.BLOOD_PRESSURE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Blood pressure upper must be between " +
                        "${AppConfig.HealthRecord.BLOOD_PRESSURE_MIN} and " +
                        "${AppConfig.HealthRecord.BLOOD_PRESSURE_MAX}",
                    field = "bloodPressureHighUpper"
                )
            )
        }
        if (lower !in AppConfig.HealthRecord.BLOOD_PRESSURE_MIN..AppConfig.HealthRecord.BLOOD_PRESSURE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Blood pressure lower must be between " +
                        "${AppConfig.HealthRecord.BLOOD_PRESSURE_MIN} and " +
                        "${AppConfig.HealthRecord.BLOOD_PRESSURE_MAX}",
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
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save blood pressure thresholds", it) }
        ) {
            dataSource.updateBloodPressureThresholds(upper, lower)
            Timber.d("Blood pressure thresholds updated: $upper / $lower")
        }
    }

    override suspend fun updatePulseThresholds(
        high: Int,
        low: Int
    ): Result<Unit, DomainError> {
        if (high !in AppConfig.HealthRecord.PULSE_MIN..AppConfig.HealthRecord.PULSE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Pulse high must be between " +
                        "${AppConfig.HealthRecord.PULSE_MIN} and " +
                        "${AppConfig.HealthRecord.PULSE_MAX}",
                    field = "pulseHigh"
                )
            )
        }
        if (low !in AppConfig.HealthRecord.PULSE_MIN..AppConfig.HealthRecord.PULSE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Pulse low must be between " +
                        "${AppConfig.HealthRecord.PULSE_MIN} and " +
                        "${AppConfig.HealthRecord.PULSE_MAX}",
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
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save pulse thresholds", it) }
        ) {
            dataSource.updatePulseThresholds(high, low)
            Timber.d("Pulse thresholds updated: $high / $low")
        }
    }

    override suspend fun updateMedicationTime(
        timing: MedicationTiming,
        hour: Int,
        minute: Int
    ): Result<Unit, DomainError> {
        if (hour !in HOUR_MIN..HOUR_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Hour must be between $HOUR_MIN and $HOUR_MAX",
                    field = "hour"
                )
            )
        }
        if (minute !in MINUTE_MIN..MINUTE_MAX) {
            return Result.Failure(
                DomainError.ValidationError(
                    message = "Minute must be between $MINUTE_MIN and $MINUTE_MAX",
                    field = "minute"
                )
            )
        }
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save medication time", it) }
        ) {
            dataSource.updateMedicationTime(timing, hour, minute)
            Timber.d("Medication time updated ($timing): $hour:$minute")
        }
    }

    override suspend fun updateThemeMode(
        mode: ThemeMode
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save theme mode", it) }
        ) {
            dataSource.updateThemeMode(mode.name)
            Timber.d("Theme mode updated: $mode")
        }
    }

    override suspend fun updateAppLanguage(
        language: AppLanguage
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save app language", it) }
        ) {
            dataSource.updateAppLanguage(language.name)
            Timber.d("App language updated: $language")
        }
    }

    override suspend fun updateSyncEnabled(
        enabled: Boolean
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save sync enabled setting", it) }
        ) {
            dataSource.updateSyncEnabled(enabled)
            Timber.d("Sync enabled updated: $enabled")
        }
    }

    override suspend fun updateBiometricEnabled(
        enabled: Boolean
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save biometric setting", it) }
        ) {
            dataSource.updateBiometricEnabled(enabled)
            Timber.d("Biometric enabled updated: $enabled")
        }
    }

    override suspend fun updateDynamicColor(
        enabled: Boolean
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save dynamic color setting", it) }
        ) {
            dataSource.updateDynamicColor(enabled)
            Timber.d("Dynamic color updated: $enabled")
        }
    }

    override fun isOnboardingCompleted(): Boolean {
        return dataSource.isOnboardingCompleted()
    }

    override suspend fun setOnboardingCompleted(
        completed: Boolean
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save onboarding setting", it) }
        ) {
            dataSource.setOnboardingCompleted(completed)
            Timber.d("Onboarding completed updated: $completed")
        }
    }

    override suspend fun resetToDefaults(): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to reset settings", it) }
        ) {
            dataSource.clearAll()
            Timber.d("Settings reset to defaults")
        }
    }

    companion object {
        private const val HOUR_MIN = AppConfig.Time.HOUR_MIN
        private const val HOUR_MAX = AppConfig.Time.HOUR_MAX
        private const val MINUTE_MIN = AppConfig.Time.MINUTE_MIN
        private const val MINUTE_MAX = AppConfig.Time.MINUTE_MAX
    }
}
