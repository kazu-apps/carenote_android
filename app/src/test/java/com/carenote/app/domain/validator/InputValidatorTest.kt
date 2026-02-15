package com.carenote.app.domain.validator

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class InputValidatorTest {

    // validateRequired
    @Test
    fun `validateRequired returns error for empty string`() {
        assertNotNull(InputValidator.validateRequired("", "Name"))
    }

    @Test
    fun `validateRequired returns error for blank string`() {
        assertNotNull(InputValidator.validateRequired("   ", "Name"))
    }

    @Test
    fun `validateRequired returns null for valid value`() {
        assertNull(InputValidator.validateRequired("John", "Name"))
    }

    // validateMaxLength
    @Test
    fun `validateMaxLength returns null when within limit`() {
        assertNull(InputValidator.validateMaxLength("abc", 5, "Field"))
    }

    @Test
    fun `validateMaxLength returns null at exact limit`() {
        assertNull(InputValidator.validateMaxLength("abcde", 5, "Field"))
    }

    @Test
    fun `validateMaxLength returns error when exceeding limit`() {
        assertNotNull(InputValidator.validateMaxLength("abcdef", 5, "Field"))
    }

    @Test
    fun `validateMaxLength handles multibyte characters`() {
        assertNull(InputValidator.validateMaxLength("あいう", 3, "Field"))
        assertNotNull(InputValidator.validateMaxLength("あいうえ", 3, "Field"))
    }

    // validateMinLength
    @Test
    fun `validateMinLength returns error when too short`() {
        assertNotNull(InputValidator.validateMinLength("ab", 3, "Field"))
    }

    @Test
    fun `validateMinLength returns null at exact minimum`() {
        assertNull(InputValidator.validateMinLength("abc", 3, "Field"))
    }

    @Test
    fun `validateMinLength returns null when longer than minimum`() {
        assertNull(InputValidator.validateMinLength("abcdef", 3, "Field"))
    }

    // validateRange
    @Test
    fun `validateRange returns null for value within range`() {
        assertNull(InputValidator.validateRange(36.5, 34.0, 42.0, "Temp"))
    }

    @Test
    fun `validateRange returns null at lower boundary`() {
        assertNull(InputValidator.validateRange(34.0, 34.0, 42.0, "Temp"))
    }

    @Test
    fun `validateRange returns null at upper boundary`() {
        assertNull(InputValidator.validateRange(42.0, 34.0, 42.0, "Temp"))
    }

    @Test
    fun `validateRange returns error below range`() {
        assertNotNull(InputValidator.validateRange(33.9, 34.0, 42.0, "Temp"))
    }

    @Test
    fun `validateRange returns error above range`() {
        assertNotNull(InputValidator.validateRange(42.1, 34.0, 42.0, "Temp"))
    }

    // validateRangeInt
    @Test
    fun `validateRangeInt returns null for value within range`() {
        assertNull(InputValidator.validateRangeInt(10, 0, 23, "Hour"))
    }

    @Test
    fun `validateRangeInt returns null at lower boundary`() {
        assertNull(InputValidator.validateRangeInt(0, 0, 23, "Hour"))
    }

    @Test
    fun `validateRangeInt returns null at upper boundary`() {
        assertNull(InputValidator.validateRangeInt(23, 0, 23, "Hour"))
    }

    @Test
    fun `validateRangeInt returns error below range`() {
        assertNotNull(InputValidator.validateRangeInt(-1, 0, 23, "Hour"))
    }

    @Test
    fun `validateRangeInt returns error above range`() {
        assertNotNull(InputValidator.validateRangeInt(24, 0, 23, "Hour"))
    }

    // validateEmail
    @Test
    fun `validateEmail returns error for empty string`() {
        assertNotNull(InputValidator.validateEmail(""))
    }

    @Test
    fun `validateEmail returns error for blank string`() {
        assertNotNull(InputValidator.validateEmail("   "))
    }

    @Test
    fun `validateEmail returns error for invalid format - no at sign`() {
        assertNotNull(InputValidator.validateEmail("invalidemail"))
    }

    @Test
    fun `validateEmail returns error for invalid format - no domain`() {
        assertNotNull(InputValidator.validateEmail("test@"))
    }

    @Test
    fun `validateEmail returns error for invalid format - no TLD`() {
        assertNotNull(InputValidator.validateEmail("test@example"))
    }

    @Test
    fun `validateEmail returns null for valid email`() {
        assertNull(InputValidator.validateEmail("user@example.com"))
    }

    @Test
    fun `validateEmail returns null for email with plus sign`() {
        assertNull(InputValidator.validateEmail("user+tag@example.com"))
    }

    @Test
    fun `validateEmail returns null for email with dots`() {
        assertNull(InputValidator.validateEmail("first.last@example.co.jp"))
    }
}
