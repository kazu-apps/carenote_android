package com.carenote.app.domain.validator

import com.carenote.app.config.AppConfig

object MedicationValidator {

    fun validateStock(value: Int): String? =
        InputValidator.validateRangeInt(value, 0, AppConfig.Medication.MAX_STOCK, "Stock")

    fun validateLowStockThreshold(value: Int): String? =
        InputValidator.validateRangeInt(value, 0, AppConfig.Medication.MAX_STOCK, "Low stock threshold")
}
