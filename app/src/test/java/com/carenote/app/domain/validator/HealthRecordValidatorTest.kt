package com.carenote.app.domain.validator

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HealthRecordValidatorTest {

    // Temperature: 34.0..42.0
    @Test
    fun `validateTemperature returns null for normal temperature`() {
        assertNull(HealthRecordValidator.validateTemperature(36.5))
    }

    @Test
    fun `validateTemperature returns null at lower boundary`() {
        assertNull(HealthRecordValidator.validateTemperature(34.0))
    }

    @Test
    fun `validateTemperature returns null at upper boundary`() {
        assertNull(HealthRecordValidator.validateTemperature(42.0))
    }

    @Test
    fun `validateTemperature returns error below range`() {
        assertNotNull(HealthRecordValidator.validateTemperature(33.9))
    }

    @Test
    fun `validateTemperature returns error above range`() {
        assertNotNull(HealthRecordValidator.validateTemperature(42.1))
    }

    // Blood Pressure: 40..250
    @Test
    fun `validateBloodPressure returns null for normal value`() {
        assertNull(HealthRecordValidator.validateBloodPressure(120, "Systolic"))
    }

    @Test
    fun `validateBloodPressure returns error below range`() {
        assertNotNull(HealthRecordValidator.validateBloodPressure(39, "Systolic"))
    }

    @Test
    fun `validateBloodPressure returns error above range`() {
        assertNotNull(HealthRecordValidator.validateBloodPressure(251, "Diastolic"))
    }

    // Pulse: 30..200
    @Test
    fun `validatePulse returns null for normal value`() {
        assertNull(HealthRecordValidator.validatePulse(72))
    }

    @Test
    fun `validatePulse returns error below range`() {
        assertNotNull(HealthRecordValidator.validatePulse(29))
    }

    // Weight: 20.0..200.0
    @Test
    fun `validateWeight returns null for normal value`() {
        assertNull(HealthRecordValidator.validateWeight(65.0))
    }

    @Test
    fun `validateWeight returns error below range`() {
        assertNotNull(HealthRecordValidator.validateWeight(19.9))
    }
}
