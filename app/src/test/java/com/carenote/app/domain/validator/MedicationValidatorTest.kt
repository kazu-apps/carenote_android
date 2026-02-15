package com.carenote.app.domain.validator

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MedicationValidatorTest {

    // Stock: 0..9999
    @Test
    fun `validateStock returns null for zero`() {
        assertNull(MedicationValidator.validateStock(0))
    }

    @Test
    fun `validateStock returns null for max value`() {
        assertNull(MedicationValidator.validateStock(9999))
    }

    @Test
    fun `validateStock returns error for negative value`() {
        assertNotNull(MedicationValidator.validateStock(-1))
    }

    @Test
    fun `validateStock returns error for value exceeding max`() {
        assertNotNull(MedicationValidator.validateStock(10000))
    }

    // LowStockThreshold: 0..9999
    @Test
    fun `validateLowStockThreshold returns null for valid value`() {
        assertNull(MedicationValidator.validateLowStockThreshold(5))
    }

    @Test
    fun `validateLowStockThreshold returns error for negative value`() {
        assertNotNull(MedicationValidator.validateLowStockThreshold(-1))
    }
}
