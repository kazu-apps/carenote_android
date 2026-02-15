package com.carenote.app.domain.validator

import com.carenote.app.config.AppConfig

object HealthRecordValidator {

    fun validateTemperature(value: Double): String? =
        InputValidator.validateRange(
            value, AppConfig.HealthRecord.TEMPERATURE_MIN,
            AppConfig.HealthRecord.TEMPERATURE_MAX, "Temperature"
        )

    fun validateBloodPressure(value: Int, fieldName: String): String? =
        InputValidator.validateRangeInt(
            value, AppConfig.HealthRecord.BLOOD_PRESSURE_MIN,
            AppConfig.HealthRecord.BLOOD_PRESSURE_MAX, fieldName
        )

    fun validatePulse(value: Int): String? =
        InputValidator.validateRangeInt(
            value, AppConfig.HealthRecord.PULSE_MIN,
            AppConfig.HealthRecord.PULSE_MAX, "Pulse"
        )

    fun validateWeight(value: Double): String? =
        InputValidator.validateRange(
            value, AppConfig.HealthRecord.WEIGHT_MIN,
            AppConfig.HealthRecord.WEIGHT_MAX, "Weight"
        )
}
