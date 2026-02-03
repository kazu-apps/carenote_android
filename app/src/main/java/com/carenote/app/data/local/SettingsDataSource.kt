package com.carenote.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val TEMPERATURE_HIGH = doublePreferencesKey("temperature_high")
        val BP_HIGH_UPPER = intPreferencesKey("bp_high_upper")
        val BP_HIGH_LOWER = intPreferencesKey("bp_high_lower")
        val PULSE_HIGH = intPreferencesKey("pulse_high")
        val PULSE_LOW = intPreferencesKey("pulse_low")
        val MORNING_HOUR = intPreferencesKey("morning_hour")
        val MORNING_MINUTE = intPreferencesKey("morning_minute")
        val NOON_HOUR = intPreferencesKey("noon_hour")
        val NOON_MINUTE = intPreferencesKey("noon_minute")
        val EVENING_HOUR = intPreferencesKey("evening_hour")
        val EVENING_MINUTE = intPreferencesKey("evening_minute")
    }

    fun getSettings(): Flow<UserSettings> {
        return dataStore.data.map { prefs ->
            UserSettings(
                themeMode = prefs[PreferencesKeys.THEME_MODE]?.let { value ->
                    try {
                        ThemeMode.valueOf(value)
                    } catch (_: IllegalArgumentException) {
                        ThemeMode.SYSTEM
                    }
                } ?: ThemeMode.SYSTEM,
                notificationsEnabled = prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                quietHoursStart = prefs[PreferencesKeys.QUIET_HOURS_START]
                    ?: AppConfig.Notification.DEFAULT_QUIET_HOURS_START,
                quietHoursEnd = prefs[PreferencesKeys.QUIET_HOURS_END]
                    ?: AppConfig.Notification.DEFAULT_QUIET_HOURS_END,
                temperatureHigh = prefs[PreferencesKeys.TEMPERATURE_HIGH]
                    ?: AppConfig.HealthThresholds.TEMPERATURE_HIGH,
                bloodPressureHighUpper = prefs[PreferencesKeys.BP_HIGH_UPPER]
                    ?: AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_UPPER,
                bloodPressureHighLower = prefs[PreferencesKeys.BP_HIGH_LOWER]
                    ?: AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_LOWER,
                pulseHigh = prefs[PreferencesKeys.PULSE_HIGH]
                    ?: AppConfig.HealthThresholds.PULSE_HIGH,
                pulseLow = prefs[PreferencesKeys.PULSE_LOW]
                    ?: AppConfig.HealthThresholds.PULSE_LOW,
                morningHour = prefs[PreferencesKeys.MORNING_HOUR]
                    ?: AppConfig.Medication.DEFAULT_MORNING_HOUR,
                morningMinute = prefs[PreferencesKeys.MORNING_MINUTE]
                    ?: AppConfig.Medication.DEFAULT_MORNING_MINUTE,
                noonHour = prefs[PreferencesKeys.NOON_HOUR]
                    ?: AppConfig.Medication.DEFAULT_NOON_HOUR,
                noonMinute = prefs[PreferencesKeys.NOON_MINUTE]
                    ?: AppConfig.Medication.DEFAULT_NOON_MINUTE,
                eveningHour = prefs[PreferencesKeys.EVENING_HOUR]
                    ?: AppConfig.Medication.DEFAULT_EVENING_HOUR,
                eveningMinute = prefs[PreferencesKeys.EVENING_MINUTE]
                    ?: AppConfig.Medication.DEFAULT_EVENING_MINUTE
            )
        }
    }

    suspend fun updateNotifications(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateQuietHours(start: Int, end: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.QUIET_HOURS_START] = start
            prefs[PreferencesKeys.QUIET_HOURS_END] = end
        }
    }

    suspend fun updateTemperatureThreshold(value: Double) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.TEMPERATURE_HIGH] = value
        }
    }

    suspend fun updateBloodPressureThresholds(upper: Int, lower: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.BP_HIGH_UPPER] = upper
            prefs[PreferencesKeys.BP_HIGH_LOWER] = lower
        }
    }

    suspend fun updatePulseThresholds(high: Int, low: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.PULSE_HIGH] = high
            prefs[PreferencesKeys.PULSE_LOW] = low
        }
    }

    suspend fun updateMedicationTime(hourKey: String, minuteKey: String, hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[intPreferencesKey(hourKey)] = hour
            prefs[intPreferencesKey(minuteKey)] = minute
        }
    }

    suspend fun updateThemeMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        const val MORNING_HOUR_KEY = "morning_hour"
        const val MORNING_MINUTE_KEY = "morning_minute"
        const val NOON_HOUR_KEY = "noon_hour"
        const val NOON_MINUTE_KEY = "noon_minute"
        const val EVENING_HOUR_KEY = "evening_hour"
        const val EVENING_MINUTE_KEY = "evening_minute"
    }
}
