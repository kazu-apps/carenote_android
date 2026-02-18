package com.carenote.app.domain.model

import java.time.LocalDateTime

sealed class SearchResult {
    abstract val id: Long
    abstract val title: String
    abstract val subtitle: String
    abstract val timestamp: LocalDateTime

    data class MedicationResult(val medication: Medication) : SearchResult() {
        override val id: Long get() = medication.id
        override val title: String get() = medication.name
        override val subtitle: String get() = medication.dosage
        override val timestamp: LocalDateTime get() = medication.updatedAt
    }

    data class NoteResult(val note: Note) : SearchResult() {
        override val id: Long get() = note.id
        override val title: String get() = note.title
        override val subtitle: String get() = note.content
        override val timestamp: LocalDateTime get() = note.updatedAt
    }

    data class HealthRecordResult(val record: HealthRecord) : SearchResult() {
        override val id: Long get() = record.id
        override val title: String get() = record.conditionNote
        override val subtitle: String get() = buildString {
            record.temperature?.let { append("${it}℃ ") }
            if (record.bloodPressureHigh != null && record.bloodPressureLow != null) {
                append("${record.bloodPressureHigh}/${record.bloodPressureLow}mmHg ")
            }
            record.pulse?.let { append("${it}bpm ") }
            record.weight?.let { append("${it}kg") }
        }.trim()
        override val timestamp: LocalDateTime get() = record.recordedAt
    }

    data class CalendarEventResult(val event: CalendarEvent) : SearchResult() {
        override val id: Long get() = event.id
        override val title: String get() = event.title
        override val subtitle: String get() = event.description
        override val timestamp: LocalDateTime
            get() = event.startTime?.let { event.date.atTime(it) }
                ?: event.date.atStartOfDay()
    }

    data class EmergencyContactResult(val contact: EmergencyContact) : SearchResult() {
        override val id: Long get() = contact.id
        override val title: String get() = contact.name
        override val subtitle: String get() = contact.phoneNumber
        override val timestamp: LocalDateTime get() = contact.updatedAt
    }
}
