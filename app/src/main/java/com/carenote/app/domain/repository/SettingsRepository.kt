package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * 設定の読み書きを行うリポジトリインターフェース
 */
interface SettingsRepository {

    fun getSettings(): Flow<UserSettings>

    suspend fun updateNotifications(enabled: Boolean): Result<Unit, DomainError>

    suspend fun updateQuietHours(start: Int, end: Int): Result<Unit, DomainError>

    suspend fun updateTemperatureThreshold(value: Double): Result<Unit, DomainError>

    suspend fun updateBloodPressureThresholds(
        upper: Int,
        lower: Int
    ): Result<Unit, DomainError>

    suspend fun updatePulseThresholds(
        high: Int,
        low: Int
    ): Result<Unit, DomainError>

    suspend fun updateMedicationTime(
        timing: MedicationTiming,
        hour: Int,
        minute: Int
    ): Result<Unit, DomainError>

    suspend fun updateThemeMode(mode: ThemeMode): Result<Unit, DomainError>

    suspend fun updateAppLanguage(language: AppLanguage): Result<Unit, DomainError>

    suspend fun updateSyncEnabled(enabled: Boolean): Result<Unit, DomainError>

    suspend fun updateBiometricEnabled(enabled: Boolean): Result<Unit, DomainError>

    suspend fun updateDynamicColor(enabled: Boolean): Result<Unit, DomainError>

    suspend fun updateSessionTimeout(minutes: Int): Result<Unit, DomainError>

    fun getSessionTimeoutMs(): Long

    fun isOnboardingCompleted(): Boolean

    suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit, DomainError>

    suspend fun resetToDefaults(): Result<Unit, DomainError>
}
