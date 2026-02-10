package com.carenote.app.data.repository

import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.TaskRepository
import com.carenote.app.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineRepositoryImpl @Inject constructor(
    private val medicationLogRepository: MedicationLogRepository,
    private val medicationRepository: MedicationRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val taskRepository: TaskRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val noteRepository: NoteRepository
) : TimelineRepository {

    override fun getTimelineItemsForDate(date: LocalDate): Flow<List<TimelineItem>> {
        val startOfDay = date.atStartOfDay()
        val startOfNextDay = date.plusDays(1).atStartOfDay()

        val medicationLogsFlow = medicationLogRepository.getLogsForDate(date)
            .catch { e ->
                Timber.w("Failed to load medication logs for timeline: $e")
                emit(emptyList())
            }

        val medicationsFlow = medicationRepository.getAllMedications()
            .catch { e ->
                Timber.w("Failed to load medications for timeline: $e")
                emit(emptyList())
            }

        val calendarEventsFlow = calendarEventRepository.getEventsByDate(date)
            .catch { e ->
                Timber.w("Failed to load calendar events for timeline: $e")
                emit(emptyList())
            }

        val tasksFlow = taskRepository.getTasksByDueDate(date)
            .catch { e ->
                Timber.w("Failed to load tasks for timeline: $e")
                emit(emptyList())
            }

        val healthRecordsFlow = healthRecordRepository.getRecordsByDateRange(startOfDay, startOfNextDay)
            .catch { e ->
                Timber.w("Failed to load health records for timeline: $e")
                emit(emptyList())
            }

        val notesFlow = noteRepository.getNotesByDate(date)
            .catch { e ->
                Timber.w("Failed to load notes for timeline: $e")
                emit(emptyList())
            }

        val firstCombined: Flow<CombinedData> = combine(
            medicationLogsFlow,
            medicationsFlow,
            calendarEventsFlow,
            tasksFlow,
            healthRecordsFlow
        ) { logs, medications, events, tasks, records ->
            CombinedData(logs, medications, events, tasks, records)
        }

        return combine(firstCombined, notesFlow) { data, notes ->
            buildTimelineItems(data, notes)
        }
    }

    private fun buildTimelineItems(data: CombinedData, notes: List<Note>): List<TimelineItem> {
        val medicationMap = data.medications.associateBy { it.id }
        val items = mutableListOf<TimelineItem>()

        data.logs.mapTo(items) { log ->
            TimelineItem.MedicationLogItem(
                log = log,
                medicationName = medicationMap[log.medicationId]?.name ?: ""
            )
        }

        data.events.mapTo(items) { event ->
            TimelineItem.CalendarEventItem(event = event)
        }

        data.tasks.mapTo(items) { task ->
            TimelineItem.TaskItem(task = task)
        }

        data.records.mapTo(items) { record ->
            TimelineItem.HealthRecordItem(record = record)
        }

        notes.mapTo(items) { note ->
            TimelineItem.NoteItem(note = note)
        }

        return items.sortedBy { it.timestamp }
    }

    private data class CombinedData(
        val logs: List<MedicationLog>,
        val medications: List<Medication>,
        val events: List<CalendarEvent>,
        val tasks: List<Task>,
        val records: List<HealthRecord>
    )
}
