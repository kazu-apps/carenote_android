package com.carenote.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences by lazy {
        getOrRecreatePrefs()
    }

    private object PreferencesKeys {
        const val THEME_MODE = "theme_mode"
        const val APP_LANGUAGE = "app_language"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val QUIET_HOURS_START = "quiet_hours_start"
        const val QUIET_HOURS_END = "quiet_hours_end"
        const val TEMPERATURE_HIGH = "temperature_high"
        const val BP_HIGH_UPPER = "bp_high_upper"
        const val BP_HIGH_LOWER = "bp_high_lower"
        const val PULSE_HIGH = "pulse_high"
        const val PULSE_LOW = "pulse_low"
        const val MORNING_HOUR = "morning_hour"
        const val MORNING_MINUTE = "morning_minute"
        const val NOON_HOUR = "noon_hour"
        const val NOON_MINUTE = "noon_minute"
        const val EVENING_HOUR = "evening_hour"
        const val EVENING_MINUTE = "evening_minute"
        const val SYNC_ENABLED = "sync_enabled"
        const val LAST_SYNC_TIME = "last_sync_time"
    }

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun getSettings(): Flow<UserSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readCurrentSettings())
        }

        // Emit initial value
        send(readCurrentSettings())

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private fun readCurrentSettings(): UserSettings {
        return UserSettings(
            themeMode = prefs.getString(PreferencesKeys.THEME_MODE, null)?.let { value ->
                try {
                    ThemeMode.valueOf(value)
                } catch (_: IllegalArgumentException) {
                    ThemeMode.SYSTEM
                }
            } ?: ThemeMode.SYSTEM,
            appLanguage = prefs.getString(PreferencesKeys.APP_LANGUAGE, null)?.let { value ->
                try {
                    AppLanguage.valueOf(value)
                } catch (_: IllegalArgumentException) {
                    AppLanguage.SYSTEM
                }
            } ?: AppLanguage.SYSTEM,
            notificationsEnabled = prefs.getBoolean(
                PreferencesKeys.NOTIFICATIONS_ENABLED,
                true
            ),
            quietHoursStart = prefs.getInt(
                PreferencesKeys.QUIET_HOURS_START,
                AppConfig.Notification.DEFAULT_QUIET_HOURS_START
            ),
            quietHoursEnd = prefs.getInt(
                PreferencesKeys.QUIET_HOURS_END,
                AppConfig.Notification.DEFAULT_QUIET_HOURS_END
            ),
            temperatureHigh = getDouble(
                PreferencesKeys.TEMPERATURE_HIGH,
                AppConfig.HealthThresholds.TEMPERATURE_HIGH
            ),
            bloodPressureHighUpper = prefs.getInt(
                PreferencesKeys.BP_HIGH_UPPER,
                AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_UPPER
            ),
            bloodPressureHighLower = prefs.getInt(
                PreferencesKeys.BP_HIGH_LOWER,
                AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_LOWER
            ),
            pulseHigh = prefs.getInt(
                PreferencesKeys.PULSE_HIGH,
                AppConfig.HealthThresholds.PULSE_HIGH
            ),
            pulseLow = prefs.getInt(
                PreferencesKeys.PULSE_LOW,
                AppConfig.HealthThresholds.PULSE_LOW
            ),
            morningHour = prefs.getInt(
                PreferencesKeys.MORNING_HOUR,
                AppConfig.Medication.DEFAULT_MORNING_HOUR
            ),
            morningMinute = prefs.getInt(
                PreferencesKeys.MORNING_MINUTE,
                AppConfig.Medication.DEFAULT_MORNING_MINUTE
            ),
            noonHour = prefs.getInt(
                PreferencesKeys.NOON_HOUR,
                AppConfig.Medication.DEFAULT_NOON_HOUR
            ),
            noonMinute = prefs.getInt(
                PreferencesKeys.NOON_MINUTE,
                AppConfig.Medication.DEFAULT_NOON_MINUTE
            ),
            eveningHour = prefs.getInt(
                PreferencesKeys.EVENING_HOUR,
                AppConfig.Medication.DEFAULT_EVENING_HOUR
            ),
            eveningMinute = prefs.getInt(
                PreferencesKeys.EVENING_MINUTE,
                AppConfig.Medication.DEFAULT_EVENING_MINUTE
            ),
            syncEnabled = prefs.getBoolean(
                PreferencesKeys.SYNC_ENABLED,
                true
            ),
            lastSyncTime = getLastSyncTime()
        )
    }

    suspend fun updateNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(PreferencesKeys.NOTIFICATIONS_ENABLED, enabled).apply()
    }

    suspend fun updateQuietHours(start: Int, end: Int) {
        prefs.edit()
            .putInt(PreferencesKeys.QUIET_HOURS_START, start)
            .putInt(PreferencesKeys.QUIET_HOURS_END, end)
            .apply()
    }

    suspend fun updateTemperatureThreshold(value: Double) {
        putDouble(PreferencesKeys.TEMPERATURE_HIGH, value)
    }

    suspend fun updateBloodPressureThresholds(upper: Int, lower: Int) {
        prefs.edit()
            .putInt(PreferencesKeys.BP_HIGH_UPPER, upper)
            .putInt(PreferencesKeys.BP_HIGH_LOWER, lower)
            .apply()
    }

    suspend fun updatePulseThresholds(high: Int, low: Int) {
        prefs.edit()
            .putInt(PreferencesKeys.PULSE_HIGH, high)
            .putInt(PreferencesKeys.PULSE_LOW, low)
            .apply()
    }

    suspend fun updateMedicationTime(timing: MedicationTiming, hour: Int, minute: Int) {
        val (hourKey, minuteKey) = when (timing) {
            MedicationTiming.MORNING -> PreferencesKeys.MORNING_HOUR to PreferencesKeys.MORNING_MINUTE
            MedicationTiming.NOON -> PreferencesKeys.NOON_HOUR to PreferencesKeys.NOON_MINUTE
            MedicationTiming.EVENING -> PreferencesKeys.EVENING_HOUR to PreferencesKeys.EVENING_MINUTE
        }
        prefs.edit()
            .putInt(hourKey, hour)
            .putInt(minuteKey, minute)
            .apply()
    }

    suspend fun updateThemeMode(mode: String) {
        prefs.edit().putString(PreferencesKeys.THEME_MODE, mode).apply()
    }

    suspend fun updateAppLanguage(language: String) {
        prefs.edit().putString(PreferencesKeys.APP_LANGUAGE, language).apply()
    }

    suspend fun clearAll() {
        prefs.edit().clear().apply()
    }

    /**
     * 同期有効/無効を更新
     *
     * @param enabled 同期を有効にするかどうか
     */
    suspend fun updateSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PreferencesKeys.SYNC_ENABLED, enabled).apply()
    }

    /**
     * 最終同期日時を取得
     *
     * @return 最終同期日時。一度も同期していない場合は null
     */
    fun getLastSyncTime(): LocalDateTime? {
        val timeString = prefs.getString(PreferencesKeys.LAST_SYNC_TIME, null)
        return timeString?.let {
            try {
                LocalDateTime.parse(it, dateTimeFormatter)
            } catch (_: Exception) {
                Timber.w("Invalid last sync time format: $it")
                null
            }
        }
    }

    /**
     * 最終同期日時を更新
     *
     * @param time 同期日時
     */
    suspend fun updateLastSyncTime(time: LocalDateTime) {
        prefs.edit()
            .putString(PreferencesKeys.LAST_SYNC_TIME, time.format(dateTimeFormatter))
            .apply()
    }

    private fun getOrRecreatePrefs(): SharedPreferences {
        return try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            Timber.w("EncryptedSharedPreferences corrupted, recreating: $e")
            deletePrefsFile()
            createEncryptedPrefs()
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun deletePrefsFile() {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        File(prefsDir, "$PREFS_FILE_NAME.xml").delete()
    }

    private fun getDouble(key: String, defaultValue: Double): Double {
        return if (prefs.contains(key)) {
            Double.fromBits(prefs.getLong(key, defaultValue.toRawBits()))
        } else {
            defaultValue
        }
    }

    private fun putDouble(key: String, value: Double) {
        prefs.edit().putLong(key, value.toRawBits()).apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "carenote_settings_prefs"
    }
}
