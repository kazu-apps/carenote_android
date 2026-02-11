package com.carenote.app.ui.screens.healthrecords

import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.common.UiText

object HealthMetricsParser {

    data class ParsedFields(
        val temperature: Double?,
        val bpHigh: Int?,
        val bpLow: Int?,
        val pulse: Int?,
        val weight: Double?,
        val trimmedNote: String,
        val hasAnyField: Boolean
    )

    data class ValidationErrors(
        val temperatureError: UiText?,
        val bloodPressureError: UiText?,
        val pulseError: UiText?,
        val weightError: UiText?,
        val conditionNoteError: UiText?
    )

    fun parseFormFields(state: AddEditHealthRecordFormState): ParsedFields {
        val temperature = state.temperature.trim().toDoubleOrNull()
        val bpHigh = state.bloodPressureHigh.trim().toIntOrNull()
        val bpLow = state.bloodPressureLow.trim().toIntOrNull()
        val pulse = state.pulse.trim().toIntOrNull()
        val weight = state.weight.trim().toDoubleOrNull()
        val trimmedNote = state.conditionNote.trim()

        val hasAnyField = temperature != null || bpHigh != null || bpLow != null ||
            pulse != null || weight != null || state.meal != null ||
            state.excretion != null || trimmedNote.isNotBlank()

        return ParsedFields(temperature, bpHigh, bpLow, pulse, weight, trimmedNote, hasAnyField)
    }

    fun validateFields(
        state: AddEditHealthRecordFormState,
        parsed: ParsedFields
    ): ValidationErrors? {
        val tempErr = validateRange(
            state.temperature, parsed.temperature,
            AppConfig.HealthRecord.TEMPERATURE_MIN, AppConfig.HealthRecord.TEMPERATURE_MAX,
            UiText.ResourceWithArgs(
                R.string.health_records_temperature_range_error,
                listOf(AppConfig.HealthRecord.TEMPERATURE_MIN, AppConfig.HealthRecord.TEMPERATURE_MAX)
            )
        )
        val bpHighErr = validateRange(
            state.bloodPressureHigh, parsed.bpHigh?.toDouble(),
            AppConfig.HealthRecord.BLOOD_PRESSURE_MIN.toDouble(),
            AppConfig.HealthRecord.BLOOD_PRESSURE_MAX.toDouble(),
            UiText.ResourceWithArgs(
                R.string.health_records_blood_pressure_range_error,
                listOf(AppConfig.HealthRecord.BLOOD_PRESSURE_MIN, AppConfig.HealthRecord.BLOOD_PRESSURE_MAX)
            )
        )
        val bpLowErr = if (bpHighErr == null) {
            validateRange(
                state.bloodPressureLow, parsed.bpLow?.toDouble(),
                AppConfig.HealthRecord.BLOOD_PRESSURE_MIN.toDouble(),
                AppConfig.HealthRecord.BLOOD_PRESSURE_MAX.toDouble(),
                UiText.ResourceWithArgs(
                    R.string.health_records_blood_pressure_range_error,
                    listOf(AppConfig.HealthRecord.BLOOD_PRESSURE_MIN, AppConfig.HealthRecord.BLOOD_PRESSURE_MAX)
                )
            )
        } else {
            null
        }
        val bpErr = bpHighErr ?: bpLowErr
        val pulseErr = validateRange(
            state.pulse, parsed.pulse?.toDouble(),
            AppConfig.HealthRecord.PULSE_MIN.toDouble(),
            AppConfig.HealthRecord.PULSE_MAX.toDouble(),
            UiText.ResourceWithArgs(
                R.string.health_records_pulse_range_error,
                listOf(AppConfig.HealthRecord.PULSE_MIN, AppConfig.HealthRecord.PULSE_MAX)
            )
        )
        val weightErr = validateRange(
            state.weight, parsed.weight,
            AppConfig.HealthRecord.WEIGHT_MIN, AppConfig.HealthRecord.WEIGHT_MAX,
            UiText.ResourceWithArgs(
                R.string.health_records_weight_range_error,
                listOf(AppConfig.HealthRecord.WEIGHT_MIN, AppConfig.HealthRecord.WEIGHT_MAX)
            )
        )
        val conditionNoteErr = if (
            parsed.trimmedNote.length > AppConfig.HealthRecord.CONDITION_NOTE_MAX_LENGTH
        ) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.HealthRecord.CONDITION_NOTE_MAX_LENGTH)
            )
        } else {
            null
        }

        return if (
            tempErr != null || bpErr != null || pulseErr != null ||
            weightErr != null || conditionNoteErr != null
        ) {
            ValidationErrors(tempErr, bpErr, pulseErr, weightErr, conditionNoteErr)
        } else {
            null
        }
    }

    internal fun validateRange(
        rawValue: String,
        parsedValue: Double?,
        min: Double,
        max: Double,
        error: UiText
    ): UiText? {
        if (rawValue.isBlank()) return null
        if (parsedValue == null || parsedValue < min || parsedValue > max) return error
        return null
    }
}
