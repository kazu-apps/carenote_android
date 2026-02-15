package com.carenote.app.domain.validator

import com.carenote.app.config.AppConfig

object SettingsValidator {

    fun validateQuietHour(value: Int): String? =
        InputValidator.validateRangeInt(value, AppConfig.Time.HOUR_MIN, AppConfig.Time.HOUR_MAX, "Quiet hour")

    fun validateTemperatureThreshold(value: Double): String? =
        InputValidator.validateRange(
            value, AppConfig.HealthRecord.TEMPERATURE_MIN,
            AppConfig.HealthRecord.TEMPERATURE_MAX, "Temperature threshold"
        )

    fun validateBloodPressureThreshold(value: Int, fieldName: String): String? =
        InputValidator.validateRangeInt(
            value, AppConfig.HealthRecord.BLOOD_PRESSURE_MIN,
            AppConfig.HealthRecord.BLOOD_PRESSURE_MAX, fieldName
        )

    fun validateBloodPressureRelation(upper: Int, lower: Int): String? =
        if (lower >= upper) "Blood pressure lower must be less than upper" else null

    fun validatePulseThreshold(value: Int, fieldName: String): String? =
        InputValidator.validateRangeInt(
            value, AppConfig.HealthRecord.PULSE_MIN,
            AppConfig.HealthRecord.PULSE_MAX, fieldName
        )

    fun validatePulseRelation(high: Int, low: Int): String? =
        if (low >= high) "Pulse low must be less than high" else null

    fun validateMedicationHour(hour: Int): String? =
        InputValidator.validateRangeInt(hour, AppConfig.Time.HOUR_MIN, AppConfig.Time.HOUR_MAX, "Hour")

    fun validateMedicationMinute(minute: Int): String? =
        InputValidator.validateRangeInt(minute, AppConfig.Time.MINUTE_MIN, AppConfig.Time.MINUTE_MAX, "Minute")

    fun validateSessionTimeout(minutes: Int): String? =
        InputValidator.validateRangeInt(
            minutes, AppConfig.Session.MIN_TIMEOUT_MINUTES,
            AppConfig.Session.MAX_TIMEOUT_MINUTES, "Session timeout"
        )
}
