package com.carenote.app.data.repository

import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.domain.validator.SettingsValidator
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
        SettingsValidator.validateQuietHour(start)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "quietHoursStart"))
        }
        SettingsValidator.validateQuietHour(end)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "quietHoursEnd"))
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
        SettingsValidator.validateTemperatureThreshold(value)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "temperatureHigh"))
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
        SettingsValidator.validateBloodPressureThreshold(upper, "Blood pressure upper")?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "bloodPressureHighUpper"))
        }
        SettingsValidator.validateBloodPressureThreshold(lower, "Blood pressure lower")?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "bloodPressureHighLower"))
        }
        SettingsValidator.validateBloodPressureRelation(upper, lower)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "bloodPressureHighLower"))
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
        SettingsValidator.validatePulseThreshold(high, "Pulse high")?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "pulseHigh"))
        }
        SettingsValidator.validatePulseThreshold(low, "Pulse low")?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "pulseLow"))
        }
        SettingsValidator.validatePulseRelation(high, low)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "pulseLow"))
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
        SettingsValidator.validateMedicationHour(hour)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "hour"))
        }
        SettingsValidator.validateMedicationMinute(minute)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "minute"))
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

    override suspend fun updateSessionTimeout(
        minutes: Int
    ): Result<Unit, DomainError> {
        SettingsValidator.validateSessionTimeout(minutes)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "sessionTimeoutMinutes"))
        }
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save session timeout", it) }
        ) {
            dataSource.updateSessionTimeout(minutes)
            Timber.d("Session timeout updated: $minutes minutes")
        }
    }

    override fun getSessionTimeoutMs(): Long {
        return dataSource.getSessionTimeoutMs()
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

}
