package com.carenote.app.domain.model

import java.time.LocalDateTime

sealed class TimelineItem {
    abstract val timestamp: LocalDateTime

    data class MedicationLogItem(
        val log: MedicationLog,
        val medicationName: String
    ) : TimelineItem() {
        override val timestamp: LocalDateTime get() = log.scheduledAt
    }

    data class CalendarEventItem(
        val event: CalendarEvent
    ) : TimelineItem() {
        override val timestamp: LocalDateTime
            get() = event.startTime?.let { event.date.atTime(it) }
                ?: event.date.atStartOfDay()
    }

    data class HealthRecordItem(
        val record: HealthRecord
    ) : TimelineItem() {
        override val timestamp: LocalDateTime get() = record.recordedAt
    }

    data class NoteItem(
        val note: Note
    ) : TimelineItem() {
        override val timestamp: LocalDateTime get() = note.createdAt
    }
}
