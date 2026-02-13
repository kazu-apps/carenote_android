package com.carenote.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 服用タイミング
 */
enum class MedicationTiming {
    MORNING,
    NOON,
    EVENING
}

/**
 * 薬モデル
 */
@Immutable
data class Medication(
    val id: Long = 0,
    val careRecipientId: Long = 0,
    val name: String,
    val dosage: String = "",
    val timings: List<MedicationTiming> = emptyList(),
    val times: Map<MedicationTiming, LocalTime> = emptyMap(),
    val reminderEnabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val currentStock: Int? = null,
    val lowStockThreshold: Int? = null
)
