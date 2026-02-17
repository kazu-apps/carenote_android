package com.carenote.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * カレンダーイベントモデル
 */
@Immutable
data class CalendarEvent(
    val id: Long = 0,
    val careRecipientId: Long = 0,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isAllDay: Boolean = true,
    val type: CalendarEventType = CalendarEventType.OTHER,
    val completed: Boolean = false,
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val recurrenceInterval: Int = 1,
    val priority: TaskPriority? = null,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime? = null,
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val isTask: Boolean get() = type == CalendarEventType.TASK
}
