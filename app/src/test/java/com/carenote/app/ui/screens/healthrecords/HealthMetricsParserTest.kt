package com.carenote.app.ui.screens.healthrecords

import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.ui.common.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class HealthMetricsParserTest {

    private val baseState = AddEditHealthRecordFormState(
        recordedAt = LocalDateTime.of(2025, 6, 1, 10, 0)
    )

    // --- parseFormFields tests ---

    @Test
    fun `parseFormFields with all empty fields returns hasAnyField false`() {
        val result = HealthMetricsParser.parseFormFields(baseState)

        assertNull(result.temperature)
        assertNull(result.bpHigh)
        assertNull(result.bpLow)
        assertNull(result.pulse)
        assertNull(result.weight)
        assertEquals("", result.trimmedNote)
        assertFalse(result.hasAnyField)
    }

    @Test
    fun `parseFormFields parses temperature correctly`() {
        val state = baseState.copy(temperature = "36.5")
        val result = HealthMetricsParser.parseFormFields(state)

        assertEquals(36.5, result.temperature)
        assertTrue(result.hasAnyField)
    }

    @Test
    fun `parseFormFields parses blood pressure correctly`() {
        val state = baseState.copy(bloodPressureHigh = "120", bloodPressureLow = "80")
        val result = HealthMetricsParser.parseFormFields(state)

        assertEquals(120, result.bpHigh)
        assertEquals(80, result.bpLow)
        assertTrue(result.hasAnyField)
    }

    @Test
    fun `parseFormFields parses pulse correctly`() {
        val state = baseState.copy(pulse = "72")
        val result = HealthMetricsParser.parseFormFields(state)

        assertEquals(72, result.pulse)
        assertTrue(result.hasAnyField)
    }

    @Test
    fun `parseFormFields parses weight correctly`() {
        val state = baseState.copy(weight = "65.5")
        val result = HealthMetricsParser.parseFormFields(state)

        assertEquals(65.5, result.weight)
        assertTrue(result.hasAnyField)
    }

    @Test
    fun `parseFormFields trims conditionNote`() {
        val state = baseState.copy(conditionNote = "  体調良好  ")
        val result = HealthMetricsParser.parseFormFields(state)

        assertEquals("体調良好", result.trimmedNote)
        assertTrue(result.hasAnyField)
    }

    @Test
    fun `parseFormFields trims whitespace-only fields`() {
        val state = baseState.copy(temperature = "  ", conditionNote = "   ")
        val result = HealthMetricsParser.parseFormFields(state)

        assertNull(result.temperature)
        assertEquals("", result.trimmedNote)
        assertFalse(result.hasAnyField)
    }

    @Test
    fun `parseFormFields with only meal returns hasAnyField true`() {
        val state = baseState.copy(meal = MealAmount.FULL)
        val result = HealthMetricsParser.parseFormFields(state)

        assertTrue(result.hasAnyField)
    }

    @Test
    fun `parseFormFields with only excretion returns hasAnyField true`() {
        val state = baseState.copy(excretion = ExcretionType.NORMAL)
        val result = HealthMetricsParser.parseFormFields(state)

        assertTrue(result.hasAnyField)
    }

    @Test
    fun `parseFormFields with all fields populated`() {
        val state = baseState.copy(
            temperature = "37.2",
            bloodPressureHigh = "130",
            bloodPressureLow = "85",
            pulse = "80",
            weight = "65.5",
            meal = MealAmount.MOSTLY,
            excretion = ExcretionType.NORMAL,
            conditionNote = "少し熱がある"
        )
        val result = HealthMetricsParser.parseFormFields(state)

        assertEquals(37.2, result.temperature)
        assertEquals(130, result.bpHigh)
        assertEquals(85, result.bpLow)
        assertEquals(80, result.pulse)
        assertEquals(65.5, result.weight)
        assertEquals("少し熱がある", result.trimmedNote)
        assertTrue(result.hasAnyField)
    }

    @Test
    fun `parseFormFields with unparseable string returns null`() {
        val state = baseState.copy(temperature = "abc", pulse = "xyz")
        val result = HealthMetricsParser.parseFormFields(state)

        assertNull(result.temperature)
        assertNull(result.pulse)
        assertFalse(result.hasAnyField)
    }

    // --- validateFields tests ---

    @Test
    fun `validateFields with all valid fields returns null`() {
        val state = baseState.copy(
            temperature = "36.5",
            bloodPressureHigh = "120",
            bloodPressureLow = "80",
            pulse = "72",
            weight = "65.0"
        )
        val parsed = HealthMetricsParser.parseFormFields(state)
        val errors = HealthMetricsParser.validateFields(state, parsed)

        assertNull(errors)
    }

    @Test
    fun `validateFields with temperature above max returns error`() {
        val state = baseState.copy(temperature = "43.0")
        val parsed = HealthMetricsParser.parseFormFields(state)
        val errors = HealthMetricsParser.validateFields(state, parsed)

        assertNotNull(errors)
        assertEquals(
            UiText.ResourceWithArgs(
                R.string.health_records_temperature_range_error,
                listOf(AppConfig.HealthRecord.TEMPERATURE_MIN, AppConfig.HealthRecord.TEMPERATURE_MAX)
            ),
            errors!!.temperatureError
        )
    }

    @Test
    fun `validateFields with temperature below min returns error`() {
        val state = baseState.copy(temperature = "33.0")
        val parsed = HealthMetricsParser.parseFormFields(state)
        val errors = HealthMetricsParser.validateFields(state, parsed)

        assertNotNull(errors)
        assertNotNull(errors!!.temperatureError)
    }

    @Test
    fun `validateFields with blood pressure high out of range returns error`() {
        val state = baseState.copy(bloodPressureHigh = "260")
        val parsed = HealthMetricsParser.parseFormFields(state)
        val errors = HealthMetricsParser.validateFields(state, parsed)

        assertNotNull(errors)
        assertNotNull(errors!!.bloodPressureError)
    }

    @Test
    fun `validateFields with blood pressure low out of range returns error`() {
        val state = baseState.copy(bloodPressureLow = "30")
        val parsed = HealthMetricsParser.parseFormFields(state)
        val errors = HealthMetricsParser.validateFields(state, parsed)

        assertNotNull(errors)
        assertNotNull(errors!!.bloodPressureError)
    }

    @Test
    fun `validateFields with pulse out of range returns error`() {
        val state = baseState.copy(pulse = "210")
        val parsed = HealthMetricsParser.parseFormFields(state)
        val errors = HealthMetricsParser.validateFields(state, parsed)

        assertNotNull(errors)
        assertEquals(
            UiText.ResourceWithArgs(
                R.string.health_records_pulse_range_error,
                listOf(AppConfig.HealthRecord.PULSE_MIN, AppConfig.HealthRecord.PULSE_MAX)
            ),
            errors!!.pulseError
        )
    }

    @Test
    fun `validateFields with weight out of range returns error`() {
        val state = baseState.copy(weight = "210.0")
        val parsed = HealthMetricsParser.parseFormFields(state)
        val errors = HealthMetricsParser.validateFields(state, parsed)

        assertNotNull(errors)
        assertNotNull(errors!!.weightError)
    }

    @Test
    fun `validateFields with conditionNote exceeding max length returns error`() {
        val longNote = "a".repeat(AppConfig.HealthRecord.CONDITION_NOTE_MAX_LENGTH + 1)
        val state = baseState.copy(conditionNote = longNote)
        val parsed = HealthMetricsParser.parseFormFields(state)
        val errors = HealthMetricsParser.validateFields(state, parsed)

        assertNotNull(errors)
        assertEquals(
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.HealthRecord.CONDITION_NOTE_MAX_LENGTH)
            ),
            errors!!.conditionNoteError
        )
    }

    @Test
    fun `validateFields with empty fields returns null`() {
        val parsed = HealthMetricsParser.parseFormFields(baseState)
        val errors = HealthMetricsParser.validateFields(baseState, parsed)

        assertNull(errors)
    }

    // --- validateRange tests ---

    @Test
    fun `validateRange with blank value returns null`() {
        val error = UiText.Resource(R.string.health_records_temperature_range_error)
        val result = HealthMetricsParser.validateRange("", null, 34.0, 42.0, error)

        assertNull(result)
    }

    @Test
    fun `validateRange with whitespace-only value returns null`() {
        val error = UiText.Resource(R.string.health_records_temperature_range_error)
        val result = HealthMetricsParser.validateRange("   ", null, 34.0, 42.0, error)

        assertNull(result)
    }

    @Test
    fun `validateRange with unparseable value returns error`() {
        val error = UiText.Resource(R.string.health_records_temperature_range_error)
        val result = HealthMetricsParser.validateRange("abc", null, 34.0, 42.0, error)

        assertEquals(error, result)
    }

    @Test
    fun `validateRange with value below min returns error`() {
        val error = UiText.Resource(R.string.health_records_temperature_range_error)
        val result = HealthMetricsParser.validateRange("33.0", 33.0, 34.0, 42.0, error)

        assertEquals(error, result)
    }

    @Test
    fun `validateRange with value above max returns error`() {
        val error = UiText.Resource(R.string.health_records_temperature_range_error)
        val result = HealthMetricsParser.validateRange("43.0", 43.0, 34.0, 42.0, error)

        assertEquals(error, result)
    }

    @Test
    fun `validateRange with value in range returns null`() {
        val error = UiText.Resource(R.string.health_records_temperature_range_error)
        val result = HealthMetricsParser.validateRange("36.5", 36.5, 34.0, 42.0, error)

        assertNull(result)
    }

    @Test
    fun `validateRange with value at min boundary returns null`() {
        val error = UiText.Resource(R.string.health_records_temperature_range_error)
        val result = HealthMetricsParser.validateRange("34.0", 34.0, 34.0, 42.0, error)

        assertNull(result)
    }

    @Test
    fun `validateRange with value at max boundary returns null`() {
        val error = UiText.Resource(R.string.health_records_temperature_range_error)
        val result = HealthMetricsParser.validateRange("42.0", 42.0, 34.0, 42.0, error)

        assertNull(result)
    }
}
