package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.domain.validator.SettingsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository : SettingsRepository {

    private val settings = MutableStateFlow(UserSettings())
    var shouldFail = false
    private var onboardingCompleted = false

    fun setSettings(userSettings: UserSettings) {
        settings.value = userSettings
    }

    fun clear() {
        settings.value = UserSettings()
        shouldFail = false
        onboardingCompleted = false
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
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake error"))
        SettingsValidator.validateQuietHour(start)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "quietHoursStart"))
        }
        SettingsValidator.validateQuietHour(end)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "quietHoursEnd"))
        }
        settings.value = settings.value.copy(quietHoursStart = start, quietHoursEnd = end)
        return Result.Success(Unit)
    }

    override suspend fun updateTemperatureThreshold(
        value: Double
    ): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake error"))
        SettingsValidator.validateTemperatureThreshold(value)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "temperatureHigh"))
        }
        settings.value = settings.value.copy(temperatureHigh = value)
        return Result.Success(Unit)
    }

    override suspend fun updateBloodPressureThresholds(
        upper: Int,
        lower: Int
    ): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake error"))
        SettingsValidator.validateBloodPressureThreshold(upper, "Blood pressure upper")?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "bloodPressureHighUpper"))
        }
        SettingsValidator.validateBloodPressureThreshold(lower, "Blood pressure lower")?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "bloodPressureHighLower"))
        }
        SettingsValidator.validateBloodPressureRelation(upper, lower)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "bloodPressureHighLower"))
        }
        settings.value = settings.value.copy(bloodPressureHighUpper = upper, bloodPressureHighLower = lower)
        return Result.Success(Unit)
    }

    override suspend fun updatePulseThresholds(
        high: Int,
        low: Int
    ): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake error"))
        SettingsValidator.validatePulseThreshold(high, "Pulse high")?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "pulseHigh"))
        }
        SettingsValidator.validatePulseThreshold(low, "Pulse low")?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "pulseLow"))
        }
        SettingsValidator.validatePulseRelation(high, low)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "pulseLow"))
        }
        settings.value = settings.value.copy(pulseHigh = high, pulseLow = low)
        return Result.Success(Unit)
    }

    override suspend fun updateMedicationTime(
        timing: MedicationTiming,
        hour: Int,
        minute: Int
    ): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake error"))
        SettingsValidator.validateMedicationHour(hour)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "hour"))
        }
        SettingsValidator.validateMedicationMinute(minute)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "minute"))
        }
        settings.value = when (timing) {
            MedicationTiming.MORNING -> settings.value.copy(morningHour = hour, morningMinute = minute)
            MedicationTiming.NOON -> settings.value.copy(noonHour = hour, noonMinute = minute)
            MedicationTiming.EVENING -> settings.value.copy(eveningHour = hour, eveningMinute = minute)
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

    override suspend fun updateBiometricEnabled(
        enabled: Boolean
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        settings.value = settings.value.copy(biometricEnabled = enabled)
        return Result.Success(Unit)
    }

    override suspend fun updateSessionTimeout(
        minutes: Int
    ): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake error"))
        SettingsValidator.validateSessionTimeout(minutes)?.let { msg ->
            return Result.Failure(DomainError.ValidationError(message = msg, field = "sessionTimeoutMinutes"))
        }
        settings.value = settings.value.copy(sessionTimeoutMinutes = minutes)
        return Result.Success(Unit)
    }

    override fun getSessionTimeoutMs(): Long {
        return settings.value.sessionTimeoutMinutes * 60_000L
    }

    override suspend fun updateDynamicColor(
        enabled: Boolean
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        settings.value = settings.value.copy(useDynamicColor = enabled)
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

    override fun isOnboardingCompleted(): Boolean = onboardingCompleted

    override suspend fun setOnboardingCompleted(
        completed: Boolean
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake error"))
        }
        onboardingCompleted = completed
        return Result.Success(Unit)
    }
}
