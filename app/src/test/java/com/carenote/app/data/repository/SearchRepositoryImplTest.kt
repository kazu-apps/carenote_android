package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.SearchResult
import com.carenote.app.domain.model.Task
import com.carenote.app.fakes.FakeCalendarEventRepository
import com.carenote.app.fakes.FakeEmergencyContactRepository
import com.carenote.app.fakes.FakeHealthRecordRepository
import com.carenote.app.fakes.FakeMedicationRepository
import com.carenote.app.fakes.FakeNoteRepository
import com.carenote.app.fakes.FakeTaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryImplTest {

    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var healthRecordRepository: FakeHealthRecordRepository
    private lateinit var calendarEventRepository: FakeCalendarEventRepository
    private lateinit var emergencyContactRepository: FakeEmergencyContactRepository
    private lateinit var searchRepository: SearchRepositoryImpl

    @Before
    fun setUp() {
        medicationRepository = FakeMedicationRepository()
        noteRepository = FakeNoteRepository()
        taskRepository = FakeTaskRepository()
        healthRecordRepository = FakeHealthRecordRepository()
        calendarEventRepository = FakeCalendarEventRepository()
        emergencyContactRepository = FakeEmergencyContactRepository()
        searchRepository = SearchRepositoryImpl(
            medicationRepository,
            noteRepository,
            taskRepository,
            healthRecordRepository,
            calendarEventRepository,
            emergencyContactRepository
        )
    }

    private fun createMedication(
        id: Long = 1L,
        name: String = "テスト薬",
        dosage: String = "1錠",
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
    ) = Medication(
        id = id,
        name = name,
        dosage = dosage,
        createdAt = createdAt,
        updatedAt = createdAt
    )

    private fun createNote(
        id: Long = 1L,
        title: String = "テストメモ",
        content: String = "メモの内容",
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
    ) = Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = createdAt
    )

    private fun createTask(
        id: Long = 1L,
        title: String = "テストタスク",
        description: String = "タスクの説明",
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
    ) = Task(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        updatedAt = createdAt
    )

    private fun createHealthRecord(
        id: Long = 1L,
        conditionNote: String = "体調メモ",
        recordedAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
    ) = HealthRecord(
        id = id,
        conditionNote = conditionNote,
        recordedAt = recordedAt,
        createdAt = recordedAt,
        updatedAt = recordedAt
    )

    private fun createCalendarEvent(
        id: Long = 1L,
        title: String = "テストイベント",
        description: String = "イベントの説明",
        date: LocalDate = LocalDate.of(2026, 1, 1),
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
    ) = CalendarEvent(
        id = id,
        title = title,
        description = description,
        date = date,
        createdAt = createdAt,
        updatedAt = createdAt
    )

    private fun createEmergencyContact(
        id: Long = 1L,
        name: String = "テスト連絡先",
        phoneNumber: String = "090-1234-5678",
        memo: String = "",
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
    ) = EmergencyContact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        memo = memo,
        createdAt = createdAt,
        updatedAt = createdAt
    )

    @Test
    fun `searchAll returns medication results when name matches`() = runTest {
        val medication = createMedication(name = "アスピリン")
        medicationRepository.setMedications(listOf(medication))

        searchRepository.searchAll("アスピリン").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertTrue(results[0] is SearchResult.MedicationResult)
            assertEquals(medication, (results[0] as SearchResult.MedicationResult).medication)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll returns note results when title matches`() = runTest {
        val note = createNote(title = "体調メモ", content = "内容")
        noteRepository.setNotes(listOf(note))

        searchRepository.searchAll("体調").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertTrue(results[0] is SearchResult.NoteResult)
            assertEquals(note, (results[0] as SearchResult.NoteResult).note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll returns task results when title matches`() = runTest {
        val task = createTask(title = "買い物")
        taskRepository.setTasks(listOf(task))

        searchRepository.searchAll("買い物").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertTrue(results[0] is SearchResult.TaskResult)
            assertEquals(task, (results[0] as SearchResult.TaskResult).task)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll returns health record results when conditionNote matches`() = runTest {
        val record = createHealthRecord(conditionNote = "頭痛がひどい")
        healthRecordRepository.setRecords(listOf(record))

        searchRepository.searchAll("頭痛").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertTrue(results[0] is SearchResult.HealthRecordResult)
            assertEquals(record, (results[0] as SearchResult.HealthRecordResult).record)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll returns calendar event results when title matches`() = runTest {
        val event = createCalendarEvent(title = "通院予定")
        calendarEventRepository.setEvents(listOf(event))

        searchRepository.searchAll("通院").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertTrue(results[0] is SearchResult.CalendarEventResult)
            assertEquals(event, (results[0] as SearchResult.CalendarEventResult).event)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll returns emergency contact results when name matches`() = runTest {
        val contact = createEmergencyContact(name = "田中太郎")
        emergencyContactRepository.setContacts(listOf(contact))

        searchRepository.searchAll("田中").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertTrue(results[0] is SearchResult.EmergencyContactResult)
            assertEquals(contact, (results[0] as SearchResult.EmergencyContactResult).contact)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll returns results from multiple entities`() = runTest {
        val medication = createMedication(
            name = "テスト薬",
            createdAt = LocalDateTime.of(2026, 1, 1, 10, 0)
        )
        val note = createNote(
            title = "テストメモ",
            createdAt = LocalDateTime.of(2026, 1, 1, 11, 0)
        )
        medicationRepository.setMedications(listOf(medication))
        noteRepository.setNotes(listOf(note))

        searchRepository.searchAll("テスト").test {
            val results = awaitItem()
            assertEquals(2, results.size)
            val types = results.map { it::class }
            assertTrue(types.contains(SearchResult.MedicationResult::class))
            assertTrue(types.contains(SearchResult.NoteResult::class))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll returns results sorted by timestamp descending`() = runTest {
        val olderMedication = createMedication(
            id = 1L,
            name = "テスト薬A",
            createdAt = LocalDateTime.of(2026, 1, 1, 8, 0)
        )
        val newerNote = createNote(
            id = 1L,
            title = "テストメモ",
            createdAt = LocalDateTime.of(2026, 1, 2, 10, 0)
        )
        val middleTask = createTask(
            id = 1L,
            title = "テストタスク",
            createdAt = LocalDateTime.of(2026, 1, 1, 15, 0)
        )
        medicationRepository.setMedications(listOf(olderMedication))
        noteRepository.setNotes(listOf(newerNote))
        taskRepository.setTasks(listOf(middleTask))

        searchRepository.searchAll("テスト").test {
            val results = awaitItem()
            assertEquals(3, results.size)
            assertTrue(results[0] is SearchResult.NoteResult)
            assertTrue(results[1] is SearchResult.TaskResult)
            assertTrue(results[2] is SearchResult.MedicationResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll is case insensitive for in-memory filtering`() = runTest {
        val task = createTask(title = "IMPORTANT TASK", description = "do something")
        taskRepository.setTasks(listOf(task))

        searchRepository.searchAll("important task").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertTrue(results[0] is SearchResult.TaskResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAll returns empty when nothing matches`() = runTest {
        medicationRepository.setMedications(listOf(createMedication(name = "アスピリン")))
        noteRepository.setNotes(listOf(createNote(title = "メモ")))
        taskRepository.setTasks(listOf(createTask(title = "タスク")))

        searchRepository.searchAll("zzzzz").test {
            val results = awaitItem()
            assertTrue(results.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
