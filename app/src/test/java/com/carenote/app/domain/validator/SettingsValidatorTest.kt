package com.carenote.app.domain.validator

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsValidatorTest {

    // Quiet Hour: 0..23
    @Test
    fun `validateQuietHour returns null for valid hour`() {
        assertNull(SettingsValidator.validateQuietHour(22))
    }

    @Test
    fun `validateQuietHour returns error for negative`() {
        assertNotNull(SettingsValidator.validateQuietHour(-1))
    }

    @Test
    fun `validateQuietHour returns error for 24`() {
        assertNotNull(SettingsValidator.validateQuietHour(24))
    }

    // Temperature threshold: 34.0..42.0
    @Test
    fun `validateTemperatureThreshold returns null for valid value`() {
        assertNull(SettingsValidator.validateTemperatureThreshold(37.5))
    }

    @Test
    fun `validateTemperatureThreshold returns error for out of range`() {
        assertNotNull(SettingsValidator.validateTemperatureThreshold(50.0))
    }

    // Blood Pressure threshold: 40..250
    @Test
    fun `validateBloodPressureThreshold returns null for valid value`() {
        assertNull(SettingsValidator.validateBloodPressureThreshold(140, "Upper"))
    }

    @Test
    fun `validateBloodPressureThreshold returns error for out of range`() {
        assertNotNull(SettingsValidator.validateBloodPressureThreshold(300, "Upper"))
    }

    // Blood Pressure relation
    @Test
    fun `validateBloodPressureRelation returns null when upper greater than lower`() {
        assertNull(SettingsValidator.validateBloodPressureRelation(140, 90))
    }

    @Test
    fun `validateBloodPressureRelation returns error when lower equals upper`() {
        assertNotNull(SettingsValidator.validateBloodPressureRelation(120, 120))
    }

    @Test
    fun `validateBloodPressureRelation returns error when lower greater than upper`() {
        assertNotNull(SettingsValidator.validateBloodPressureRelation(90, 140))
    }

    // Pulse threshold: 30..200
    @Test
    fun `validatePulseThreshold returns null for valid value`() {
        assertNull(SettingsValidator.validatePulseThreshold(100, "High"))
    }

    // Pulse relation
    @Test
    fun `validatePulseRelation returns null when high greater than low`() {
        assertNull(SettingsValidator.validatePulseRelation(100, 50))
    }

    @Test
    fun `validatePulseRelation returns error when low equals high`() {
        assertNotNull(SettingsValidator.validatePulseRelation(70, 70))
    }

    // Medication hour/minute
    @Test
    fun `validateMedicationHour returns null for valid hour`() {
        assertNull(SettingsValidator.validateMedicationHour(8))
    }

    @Test
    fun `validateMedicationMinute returns null for valid minute`() {
        assertNull(SettingsValidator.validateMedicationMinute(30))
    }

    @Test
    fun `validateMedicationMinute returns error for 60`() {
        assertNotNull(SettingsValidator.validateMedicationMinute(60))
    }

    // Session timeout: 1..60
    @Test
    fun `validateSessionTimeout returns null for valid value`() {
        assertNull(SettingsValidator.validateSessionTimeout(5))
    }

    @Test
    fun `validateSessionTimeout returns error for zero`() {
        assertNotNull(SettingsValidator.validateSessionTimeout(0))
    }

    @Test
    fun `validateSessionTimeout returns error for exceeding max`() {
        assertNotNull(SettingsValidator.validateSessionTimeout(61))
    }
}
