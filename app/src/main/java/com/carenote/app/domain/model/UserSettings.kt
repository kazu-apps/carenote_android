package com.carenote.app.domain.model

import com.carenote.app.config.AppConfig
import java.time.LocalDateTime

/**
 * ユーザー設定を表すドメインモデル
 * DataStore Preferences で永続化される
 */
data class UserSettings(
    // テーマ設定
    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    // 言語設定
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,

    // 通知設定
    val notificationsEnabled: Boolean = true,
    val quietHoursStart: Int = AppConfig.Notification.DEFAULT_QUIET_HOURS_START,
    val quietHoursEnd: Int = AppConfig.Notification.DEFAULT_QUIET_HOURS_END,

    // 健康記録の異常値基準
    val temperatureHigh: Double = AppConfig.HealthThresholds.TEMPERATURE_HIGH,
    val bloodPressureHighUpper: Int = AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_UPPER,
    val bloodPressureHighLower: Int = AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_LOWER,
    val pulseHigh: Int = AppConfig.HealthThresholds.PULSE_HIGH,
    val pulseLow: Int = AppConfig.HealthThresholds.PULSE_LOW,

    // 服薬デフォルト時刻
    val morningHour: Int = AppConfig.Medication.DEFAULT_MORNING_HOUR,
    val morningMinute: Int = AppConfig.Medication.DEFAULT_MORNING_MINUTE,
    val noonHour: Int = AppConfig.Medication.DEFAULT_NOON_HOUR,
    val noonMinute: Int = AppConfig.Medication.DEFAULT_NOON_MINUTE,
    val eveningHour: Int = AppConfig.Medication.DEFAULT_EVENING_HOUR,
    val eveningMinute: Int = AppConfig.Medication.DEFAULT_EVENING_MINUTE,

    // 同期設定
    val syncEnabled: Boolean = true,
    val lastSyncTime: LocalDateTime? = null,

    // セキュリティ設定
    val biometricEnabled: Boolean = false,
    val sessionTimeoutMinutes: Int = AppConfig.Session.DEFAULT_TIMEOUT_MINUTES,

    // テーマ拡張設定
    val useDynamicColor: Boolean = false
)
