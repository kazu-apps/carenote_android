package com.carenote.app.domain.validator

object InputValidator {

    fun validateRequired(value: String, fieldName: String): String? =
        if (value.isBlank()) "$fieldName is required" else null

    fun validateMaxLength(value: String, maxLength: Int, fieldName: String): String? =
        if (value.length > maxLength) "$fieldName must be $maxLength characters or less" else null

    fun validateMinLength(value: String, minLength: Int, fieldName: String): String? =
        if (value.length < minLength) "$fieldName must be at least $minLength characters" else null

    fun validateRange(value: Double, min: Double, max: Double, fieldName: String): String? =
        if (value < min || value > max) "$fieldName must be between $min and $max" else null

    fun validateRangeInt(value: Int, min: Int, max: Int, fieldName: String): String? =
        if (value !in min..max) "$fieldName must be between $min and $max" else null

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!EMAIL_REGEX.matches(email)) return "Invalid email format"
        return null
    }
}
