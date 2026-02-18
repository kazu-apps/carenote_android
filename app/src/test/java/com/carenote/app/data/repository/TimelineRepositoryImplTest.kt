package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.fakes.FakeCalendarEventRepository
import com.carenote.app.fakes.FakeHealthRecordRepository
import com.carenote.app.fakes.FakeMedicationLogRepository
import com.carenote.app.fakes.FakeMedicationRepository
import com.carenote.app.fakes.FakeNoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineRepositoryImplTest {

    private lateinit var medicationLogRepo: FakeMedicationLogRepository
    private lateinit var medicationRepo: FakeMedicationRepository
    private lateinit var calendarEventRepo: FakeCalendarEventRepository
    private lateinit var healthRecordRepo: FakeHealthRecordRepository
    private lateinit var noteRepo: FakeNoteRepository
    private lateinit var repository: TimelineRepositoryImpl

    private val testDate = LocalDate.of(2025, 6, 15)

    @Before
    fun setUp() {
        medicationLogRepo = FakeMedicationLogRepository()
        medicationRepo = FakeMedicationRepository()
        calendarEventRepo = FakeCalendarEventRepository()
        healthRecordRepo = FakeHealthRecordRepository()
        noteRepo = FakeNoteRepository()
        repository = TimelineRepositoryImpl(
            medicationLogRepository = medicationLogRepo,
            medicationRepository = medicationRepo,
            calendarEventRepository = calendarEventRepo,
            healthRecordRepository = healthRecordRepo,
            noteRepository = noteRepo
        )
    }

    @Test
    fun `getTimelineItemsForDate returns all types sorted by timestamp`() = runTest {
        val medication = Medication(id = 1L, name = "Test Med")
        medicationRepo.setMedications(listOf(medication))

        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = testDate.atTime(8, 0)
        )
        medicationLogRepo.setLogs(listOf(log))

        val event = CalendarEvent(
            id = 1L,
            title = "Doctor Visit",
            date = testDate,
            startTime = LocalTime.of(10, 0)
        )
        val taskEvent = CalendarEvent(
            id = 2L,
            title = "Buy groceries",
            date = testDate,
            type = CalendarEventType.TASK,
            priority = TaskPriority.HIGH
        )
        calendarEventRepo.setEvents(listOf(event, taskEvent))

        val record = HealthRecord(
            id = 1L,
            temperature = 36.5,
            recordedAt = testDate.atTime(7, 0),
            createdAt = testDate.atTime(7, 0)
        )
        healthRecordRepo.setRecords(listOf(record))

        val note = Note(
            id = 1L,
            title = "Morning note",
            content = "Feeling good",
            tag = NoteTag.CONDITION,
            createdAt = testDate.atTime(9, 0),
            updatedAt = testDate.atTime(9, 0)
        )
        noteRepo.setNotes(listOf(note))

        repository.getTimelineItemsForDate(testDate).test {
            val items = awaitItem()
            assertEquals(5, items.size)

            // Sorted by timestamp: HealthRecord 7:00, MedLog 8:00, Note 9:00, CalendarEvent 10:00, TaskEvent startOfDay=00:00
            // TaskEvent with no startTime gets date.atStartOfDay() = 00:00, so it's first
            assertTrue(items[0] is TimelineItem.CalendarEventItem)
            assertEquals("Buy groceries", (items[0] as TimelineItem.CalendarEventItem).event.title)
            assertTrue(items[1] is TimelineItem.HealthRecordItem)
            assertTrue(items[2] is TimelineItem.MedicationLogItem)
            assertTrue(items[3] is TimelineItem.NoteItem)
            assertTrue(items[4] is TimelineItem.CalendarEventItem)
            assertEquals("Doctor Visit", (items[4] as TimelineItem.CalendarEventItem).event.title)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getTimelineItemsForDate resolves medication name correctly`() = runTest {
        val medication = Medication(id = 10L, name = "Aspirin")
        medicationRepo.setMedications(listOf(medication))

        val log = MedicationLog(
            id = 1L,
            medicationId = 10L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = testDate.atTime(8, 0)
        )
        medicationLogRepo.setLogs(listOf(log))

        repository.getTimelineItemsForDate(testDate).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            val medItem = items[0] as TimelineItem.MedicationLogItem
            assertEquals("Aspirin", medItem.medicationName)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getTimelineItemsForDate returns empty medication name when medication not found`() = runTest {
        val log = MedicationLog(
            id = 1L,
            medicationId = 999L,
            status = MedicationLogStatus.SKIPPED,
            scheduledAt = testDate.atTime(8, 0)
        )
        medicationLogRepo.setLogs(listOf(log))

        repository.getTimelineItemsForDate(testDate).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            val medItem = items[0] as TimelineItem.MedicationLogItem
            assertEquals("", medItem.medicationName)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getTimelineItemsForDate returns empty list when no data`() = runTest {
        repository.getTimelineItemsForDate(testDate).test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getTimelineItemsForDate filters notes by date correctly`() = runTest {
        val todayNote = Note(
            id = 1L,
            title = "Today note",
            content = "Content",
            createdAt = testDate.atTime(12, 0),
            updatedAt = testDate.atTime(12, 0)
        )
        val yesterdayNote = Note(
            id = 2L,
            title = "Yesterday note",
            content = "Content",
            createdAt = testDate.minusDays(1).atTime(12, 0),
            updatedAt = testDate.minusDays(1).atTime(12, 0)
        )
        noteRepo.setNotes(listOf(todayNote, yesterdayNote))

        repository.getTimelineItemsForDate(testDate).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            val noteItem = items[0] as TimelineItem.NoteItem
            assertEquals("Today note", noteItem.note.title)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getTimelineItemsForDate note boundary - end of day excluded`() = runTest {
        val endOfDayNote = Note(
            id = 1L,
            title = "End of day",
            content = "",
            createdAt = testDate.atTime(23, 59, 59),
            updatedAt = testDate.atTime(23, 59, 59)
        )
        val nextDayNote = Note(
            id = 2L,
            title = "Next day",
            content = "",
            createdAt = testDate.plusDays(1).atStartOfDay(),
            updatedAt = testDate.plusDays(1).atStartOfDay()
        )
        noteRepo.setNotes(listOf(endOfDayNote, nextDayNote))

        repository.getTimelineItemsForDate(testDate).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            val noteItem = items[0] as TimelineItem.NoteItem
            assertEquals("End of day", noteItem.note.title)
            cancelAndConsumeRemainingEvents()
        }
    }
}
