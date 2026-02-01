package com.carenote.app.domain.model

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
data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String = "",
    val timings: List<MedicationTiming> = emptyList(),
    val times: Map<MedicationTiming, LocalTime> = emptyMap(),
    val reminderEnabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
