package com.carenote.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * タスクモデル
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: LocalDate? = null,
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
