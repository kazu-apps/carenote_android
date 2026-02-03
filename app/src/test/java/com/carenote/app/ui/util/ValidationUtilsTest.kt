package com.carenote.app.ui.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `isNotBlank returns true for non-empty string`() {
        assertTrue(ValidationUtils.isNotBlank("hello"))
    }

    @Test
    fun `isNotBlank returns false for empty string`() {
        assertFalse(ValidationUtils.isNotBlank(""))
    }

    @Test
    fun `isNotBlank returns false for whitespace only`() {
        assertFalse(ValidationUtils.isNotBlank("   "))
    }

    @Test
    fun `isNotBlank returns false for tabs and newlines`() {
        assertFalse(ValidationUtils.isNotBlank("\t\n"))
    }

    @Test
    fun `isWithinLength returns true when within limit`() {
        assertTrue(ValidationUtils.isWithinLength("hello", 10))
    }

    @Test
    fun `isWithinLength returns true when exactly at limit`() {
        assertTrue(ValidationUtils.isWithinLength("hello", 5))
    }

    @Test
    fun `isWithinLength returns false when exceeding limit`() {
        assertFalse(ValidationUtils.isWithinLength("hello world", 5))
    }

    @Test
    fun `isWithinLength returns true for empty string`() {
        assertTrue(ValidationUtils.isWithinLength("", 10))
    }

    @Test
    fun `isValidNumber returns true for valid integer`() {
        assertTrue(ValidationUtils.isValidNumber("123"))
    }

    @Test
    fun `isValidNumber returns true for valid decimal`() {
        assertTrue(ValidationUtils.isValidNumber("36.5"))
    }

    @Test
    fun `isValidNumber returns false for non-numeric text`() {
        assertFalse(ValidationUtils.isValidNumber("abc"))
    }

    @Test
    fun `isValidNumber returns false for empty string`() {
        assertFalse(ValidationUtils.isValidNumber(""))
    }

    @Test
    fun `isValidNumber returns false for mixed content`() {
        assertFalse(ValidationUtils.isValidNumber("12abc"))
    }

    @Test
    fun `isInRange returns true when value in range`() {
        assertTrue(ValidationUtils.isInRange(36.5, 35.0, 42.0))
    }

    @Test
    fun `isInRange returns true at lower bound`() {
        assertTrue(ValidationUtils.isInRange(35.0, 35.0, 42.0))
    }

    @Test
    fun `isInRange returns true at upper bound`() {
        assertTrue(ValidationUtils.isInRange(42.0, 35.0, 42.0))
    }

    @Test
    fun `isInRange returns false below lower bound`() {
        assertFalse(ValidationUtils.isInRange(34.9, 35.0, 42.0))
    }

    @Test
    fun `isInRange returns false above upper bound`() {
        assertFalse(ValidationUtils.isInRange(42.1, 35.0, 42.0))
    }

    @Test
    fun `validateRequired returns null for valid input`() {
        assertNull(ValidationUtils.validateRequired("hello", "名前"))
    }

    @Test
    fun `validateRequired returns error for empty input`() {
        val result = ValidationUtils.validateRequired("", "名前")

        assertTrue(result != null && result.contains("名前"))
    }

    @Test
    fun `validateRequired returns error for blank input`() {
        val result = ValidationUtils.validateRequired("   ", "名前")

        assertTrue(result != null && result.contains("名前"))
    }

    @Test
    fun `validateMaxLength returns null for valid input`() {
        assertNull(ValidationUtils.validateMaxLength("hello", 10, "名前"))
    }

    @Test
    fun `validateMaxLength returns error for too long input`() {
        val result = ValidationUtils.validateMaxLength("hello world!", 5, "名前")

        assertTrue(result != null)
    }

    @Test
    fun `validateNumberRange returns null for valid number`() {
        assertNull(ValidationUtils.validateNumberRange("36.5", 35.0, 42.0, "体温"))
    }

    @Test
    fun `validateNumberRange returns error for non-numeric input`() {
        val result = ValidationUtils.validateNumberRange("abc", 35.0, 42.0, "体温")

        assertTrue(result != null)
    }

    @Test
    fun `validateNumberRange returns error for out of range`() {
        val result = ValidationUtils.validateNumberRange("50.0", 35.0, 42.0, "体温")

        assertTrue(result != null)
    }

    @Test
    fun `isValidNumber returns true for negative number`() {
        assertTrue(ValidationUtils.isValidNumber("-5.0"))
    }

    @Test
    fun `isValidNumber returns true for NaN string`() {
        // toDoubleOrNull() parses "NaN" as Double.NaN - documenting this behavior
        assertTrue(ValidationUtils.isValidNumber("NaN"))
    }

    @Test
    fun `isValidNumber returns true for Infinity string`() {
        // toDoubleOrNull() parses "Infinity" as Double.POSITIVE_INFINITY - documenting this behavior
        assertTrue(ValidationUtils.isValidNumber("Infinity"))
    }

    @Test
    fun `isValidNumber returns false for whitespace only`() {
        assertFalse(ValidationUtils.isValidNumber("   "))
    }

    @Test
    fun `validateNumberRange returns null at lower boundary`() {
        assertNull(ValidationUtils.validateNumberRange("35.0", 35.0, 42.0, "体温"))
    }

    @Test
    fun `validateNumberRange returns null at upper boundary`() {
        assertNull(ValidationUtils.validateNumberRange("42.0", 35.0, 42.0, "体温"))
    }

    @Test
    fun `validateNumberRange returns error just below lower boundary`() {
        val result = ValidationUtils.validateNumberRange("34.9", 35.0, 42.0, "体温")
        assertTrue(result != null)
    }

    @Test
    fun `validateNumberRange returns error for empty string`() {
        val result = ValidationUtils.validateNumberRange("", 35.0, 42.0, "体温")
        assertTrue(result != null)
    }
}
