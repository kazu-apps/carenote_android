package com.carenote.app.domain.model

import java.time.LocalDateTime

/**
 * 食事量
 */
enum class MealAmount {
    FULL,
    MOSTLY,
    HALF,
    LITTLE,
    NONE
}

/**
 * 排泄タイプ
 */
enum class ExcretionType {
    NORMAL,
    SOFT,
    HARD,
    DIARRHEA,
    NONE
}

/**
 * 健康記録モデル
 */
data class HealthRecord(
    val id: Long = 0,
    val temperature: Double? = null,
    val bloodPressureHigh: Int? = null,
    val bloodPressureLow: Int? = null,
    val pulse: Int? = null,
    val weight: Double? = null,
    val meal: MealAmount? = null,
    val excretion: ExcretionType? = null,
    val conditionNote: String = "",
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
