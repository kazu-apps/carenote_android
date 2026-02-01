package com.carenote.app.domain.model

import java.time.LocalDateTime

/**
 * 服薬記録のステータス
 */
enum class MedicationLogStatus {
    TAKEN,
    SKIPPED,
    POSTPONED
}

/**
 * 服薬記録モデル
 */
data class MedicationLog(
    val id: Long = 0,
    val medicationId: Long,
    val status: MedicationLogStatus,
    val scheduledAt: LocalDateTime,
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val memo: String = ""
)
