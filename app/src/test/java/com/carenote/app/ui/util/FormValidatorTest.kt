package com.carenote.app.ui.util

import com.carenote.app.R
import com.carenote.app.ui.common.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FormValidatorTest {

    // --- validateRequired ---

    @Test
    fun `validateRequired returns error for empty string`() {
        val result = FormValidator.validateRequired("", R.string.medication_name_required)
        assertEquals(UiText.Resource(R.string.medication_name_required), result)
    }

    @Test
    fun `validateRequired returns error for blank string`() {
        val result = FormValidator.validateRequired("   ", R.string.medication_name_required)
        assertNotNull(result)
    }

    @Test
    fun `validateRequired returns error for whitespace only`() {
        val result = FormValidator.validateRequired("\t\n ", R.string.medication_name_required)
        assertNotNull(result)
    }

    @Test
    fun `validateRequired returns null for valid value`() {
        val result = FormValidator.validateRequired("Aspirin", R.string.medication_name_required)
        assertNull(result)
    }

    @Test
    fun `validateRequired returns null for value with leading and trailing spaces`() {
        val result = FormValidator.validateRequired("  Aspirin  ", R.string.medication_name_required)
        assertNull(result)
    }

    // --- validateMaxLength ---

    @Test
    fun `validateMaxLength returns null for empty string`() {
        val result = FormValidator.validateMaxLength("", 10)
        assertNull(result)
    }

    @Test
    fun `validateMaxLength returns null when under limit`() {
        val result = FormValidator.validateMaxLength("abc", 10)
        assertNull(result)
    }

    @Test
    fun `validateMaxLength returns null at exact boundary`() {
        val result = FormValidator.validateMaxLength("a".repeat(100), 100)
        assertNull(result)
    }

    @Test
    fun `validateMaxLength returns error when over limit`() {
        val result = FormValidator.validateMaxLength("a".repeat(101), 100)
        assertEquals(
            UiText.ResourceWithArgs(R.string.ui_validation_too_long, listOf(100)),
            result
        )
    }

    @Test
    fun `validateMaxLength counts multibyte characters correctly`() {
        val japanese = "あ".repeat(11)
        val result = FormValidator.validateMaxLength(japanese, 10)
        assertNotNull(result)
    }

    @Test
    fun `validateMaxLength counts emoji as characters`() {
        val emoji = "\uD83D\uDE00".repeat(6)
        val result = FormValidator.validateMaxLength(emoji, 5)
        assertNotNull(result)
    }

    // --- combineValidations ---

    @Test
    fun `combineValidations returns null when all null`() {
        val result = FormValidator.combineValidations(null, null, null)
        assertNull(result)
    }

    @Test
    fun `combineValidations returns first non-null error`() {
        val error = UiText.Resource(R.string.medication_name_required)
        val result = FormValidator.combineValidations(null, error, null)
        assertEquals(error, result)
    }

    @Test
    fun `combineValidations returns first error when multiple present`() {
        val error1 = UiText.Resource(R.string.medication_name_required)
        val error2 = UiText.ResourceWithArgs(R.string.ui_validation_too_long, listOf(100))
        val result = FormValidator.combineValidations(error1, error2)
        assertEquals(error1, result)
    }

    @Test
    fun `combineValidations returns null for empty vararg`() {
        val result = FormValidator.combineValidations()
        assertNull(result)
    }

    // --- validatePhoneFormat ---

    @Test
    fun `validatePhoneFormat returns null for valid digits and hyphens`() {
        val result = FormValidator.validatePhoneFormat("090-1234-5678")
        assertNull(result)
    }

    @Test
    fun `validatePhoneFormat returns null for empty string`() {
        val result = FormValidator.validatePhoneFormat("")
        assertNull(result)
    }

    @Test
    fun `validatePhoneFormat returns error for letters`() {
        val result = FormValidator.validatePhoneFormat("090-abc-5678")
        assertNotNull(result)
    }

    @Test
    fun `validatePhoneFormat returns error for special characters`() {
        val result = FormValidator.validatePhoneFormat("090@1234#5678")
        assertNotNull(result)
    }

    @Test
    fun `validatePhoneFormat returns null for international format`() {
        val result = FormValidator.validatePhoneFormat("+81-90-1234-5678")
        assertNull(result)
    }
}
