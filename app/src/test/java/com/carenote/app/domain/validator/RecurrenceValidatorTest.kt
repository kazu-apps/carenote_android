package com.carenote.app.domain.validator

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RecurrenceValidatorTest {

    @Test
    fun `validateInterval returns error for zero`() {
        assertNotNull(RecurrenceValidator.validateInterval(0, 99))
    }

    @Test
    fun `validateInterval returns null for one`() {
        assertNull(RecurrenceValidator.validateInterval(1, 99))
    }

    @Test
    fun `validateInterval returns null at max interval`() {
        assertNull(RecurrenceValidator.validateInterval(99, 99))
    }

    @Test
    fun `validateInterval returns error exceeding max`() {
        assertNotNull(RecurrenceValidator.validateInterval(100, 99))
    }

    @Test
    fun `validateInterval returns error for negative value`() {
        assertNotNull(RecurrenceValidator.validateInterval(-1, 99))
    }
}
