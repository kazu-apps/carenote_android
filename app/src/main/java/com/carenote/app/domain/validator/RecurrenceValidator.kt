package com.carenote.app.domain.validator

object RecurrenceValidator {

    fun validateInterval(interval: Int, maxInterval: Int): String? =
        InputValidator.validateRangeInt(interval, 1, maxInterval, "Recurrence interval")
}
