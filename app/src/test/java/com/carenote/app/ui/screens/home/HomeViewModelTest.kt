package com.carenote.app.ui.screens.home

import app.cash.turbine.test
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.Task
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeCalendarEventRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakeHealthRecordRepository
import com.carenote.app.fakes.FakeMedicationLogRepository
import com.carenote.app.fakes.FakeMedicationRepository
import com.carenote.app.fakes.FakeNoteRepository
import com.carenote.app.fakes.FakeTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeClock = FakeClock()
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var medicationLogRepository: FakeMedicationLogRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var healthRecordRepository: FakeHealthRecordRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var calendarEventRepository: FakeCalendarEventRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicationRepository = FakeMedicationRepository()
        medicationLogRepository = FakeMedicationLogRepository()
        taskRepository = FakeTaskRepository()
        healthRecordRepository = FakeHealthRecordRepository()
        noteRepository = FakeNoteRepository()
        calendarEventRepository = FakeCalendarEventRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            medicationRepository = medicationRepository,
            medicationLogRepository = medicationLogRepository,
            taskRepository = taskRepository,
            healthRecordRepository = healthRecordRepository,
            noteRepository = noteRepository,
            calendarEventRepository = calendarEventRepository,
            analyticsRepository = analyticsRepository,
            clock = fakeClock
        )
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads all sections with data`() = runTest(testDispatcher) {
        val today = fakeClock.today()
        setupTestData(today)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertTrue(state.todayMedications.isNotEmpty())
            assertTrue(state.upcomingTasks.isNotEmpty())
            assertNotNull(state.latestHealthRecord)
            assertTrue(state.recentNotes.isNotEmpty())
            assertTrue(state.todayEvents.isNotEmpty())
        }
    }

    @Test
    fun `empty state when no data`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertTrue(state.todayMedications.isEmpty())
            assertTrue(state.upcomingTasks.isEmpty())
            assertNull(state.latestHealthRecord)
            assertTrue(state.recentNotes.isEmpty())
            assertTrue(state.todayEvents.isEmpty())
        }
    }

    @Test
    fun `tasks are sorted by due date`() = runTest(testDispatcher) {
        val today = fakeClock.today()
        taskRepository.setTasks(
            listOf(
                Task(id = 1, title = "Later", dueDate = today.plusDays(5)),
                Task(id = 2, title = "Soon", dueDate = today.plusDays(1)),
                Task(id = 3, title = "No date", dueDate = null)
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val tasks = expectMostRecentItem().upcomingTasks
            assertEquals(3, tasks.size)
            assertEquals("Soon", tasks[0].title)
            assertEquals("Later", tasks[1].title)
            assertEquals("No date", tasks[2].title)
        }
    }

    @Test
    fun `health records show latest by recordedAt`() = runTest(testDispatcher) {
        healthRecordRepository.setRecords(
            listOf(
                HealthRecord(
                    id = 1,
                    temperature = 36.5,
                    recordedAt = LocalDateTime.of(2026, 1, 10, 8, 0)
                ),
                HealthRecord(
                    id = 2,
                    temperature = 37.0,
                    recordedAt = LocalDateTime.of(2026, 1, 14, 8, 0)
                ),
                HealthRecord(
                    id = 3,
                    temperature = 36.8,
                    recordedAt = LocalDateTime.of(2026, 1, 12, 8, 0)
                )
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val record = expectMostRecentItem().latestHealthRecord
            assertNotNull(record)
            assertEquals(2L, record!!.id)
            assertEquals(37.0, record.temperature!!, 0.01)
        }
    }

    @Test
    fun `notes are sorted by updatedAt descending`() = runTest(testDispatcher) {
        noteRepository.setNotes(
            listOf(
                Note(
                    id = 1,
                    title = "Old",
                    content = "content",
                    updatedAt = LocalDateTime.of(2026, 1, 10, 8, 0)
                ),
                Note(
                    id = 2,
                    title = "Newest",
                    content = "content",
                    updatedAt = LocalDateTime.of(2026, 1, 15, 8, 0)
                ),
                Note(
                    id = 3,
                    title = "Middle",
                    content = "content",
                    updatedAt = LocalDateTime.of(2026, 1, 12, 8, 0)
                )
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val notes = expectMostRecentItem().recentNotes
            assertEquals(3, notes.size)
            assertEquals("Newest", notes[0].title)
            assertEquals("Middle", notes[1].title)
            assertEquals("Old", notes[2].title)
        }
    }

    @Test
    fun `section items are limited to MAX_SECTION_ITEMS`() = runTest(testDispatcher) {
        val today = fakeClock.today()
        val medications = (1..10).map { i ->
            Medication(id = i.toLong(), name = "Med $i")
        }
        medicationRepository.setMedications(medications)

        val tasks = (1..10).map { i ->
            Task(id = i.toLong(), title = "Task $i", dueDate = today.plusDays(i.toLong()))
        }
        taskRepository.setTasks(tasks)

        val notes = (1..10).map { i ->
            Note(
                id = i.toLong(),
                title = "Note $i",
                content = "content",
                updatedAt = LocalDateTime.of(2026, 1, i, 8, 0)
            )
        }
        noteRepository.setNotes(notes)

        val events = (1..10).map { i ->
            CalendarEvent(
                id = i.toLong(),
                title = "Event $i",
                date = today,
                startTime = LocalTime.of(8 + (i % 12), 0)
            )
        }
        calendarEventRepository.setEvents(events)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(AppConfig.Home.MAX_SECTION_ITEMS, state.todayMedications.size)
            assertEquals(AppConfig.Home.MAX_SECTION_ITEMS, state.upcomingTasks.size)
            assertEquals(AppConfig.Home.MAX_SECTION_ITEMS, state.recentNotes.size)
            assertEquals(AppConfig.Home.MAX_SECTION_ITEMS, state.todayEvents.size)
        }
    }

    @Test
    fun `refresh triggers data reload`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial.todayMedications.isEmpty())

            medicationRepository.setMedications(
                listOf(Medication(id = 1, name = "New Med"))
            )

            viewModel.refresh()
            advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertFalse(viewModel.isRefreshing.value)
            assertEquals(1, refreshed.todayMedications.size)
        }
    }

    @Test
    fun `MedicationWithLog correctly matches logs to medications`() = runTest(testDispatcher) {
        val today = fakeClock.today()
        val todayTime = today.atTime(8, 0)

        medicationRepository.setMedications(
            listOf(
                Medication(
                    id = 1,
                    name = "Med A",
                    timings = listOf(MedicationTiming.MORNING, MedicationTiming.EVENING)
                ),
                Medication(
                    id = 2,
                    name = "Med B",
                    timings = listOf(MedicationTiming.MORNING)
                )
            )
        )

        medicationLogRepository.setLogs(
            listOf(
                MedicationLog(
                    id = 1,
                    medicationId = 1,
                    status = MedicationLogStatus.TAKEN,
                    scheduledAt = todayTime,
                    timing = MedicationTiming.MORNING
                ),
                MedicationLog(
                    id = 2,
                    medicationId = 1,
                    status = MedicationLogStatus.TAKEN,
                    scheduledAt = todayTime.plusHours(10),
                    timing = MedicationTiming.EVENING
                )
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val meds = expectMostRecentItem().todayMedications
            assertEquals(2, meds.size)

            val medA = meds.find { it.medication.id == 1L }!!
            assertEquals(2, medA.logs.size)

            val medB = meds.find { it.medication.id == 2L }!!
            assertEquals(0, medB.logs.size)
        }
    }

    @Test
    fun `logSeeAllClicked records analytics event`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.logSeeAllClicked("medication")

        assertEquals(1, analyticsRepository.loggedEvents.size)
        val (name, params) = analyticsRepository.loggedEvents[0]
        assertEquals(AppConfig.Analytics.EVENT_HOME_SEE_ALL_CLICKED, name)
        assertEquals("medication", params[AppConfig.Analytics.PARAM_SECTION])
    }

    @Test
    fun `calendar events sorted by startTime`() = runTest(testDispatcher) {
        val today = fakeClock.today()
        calendarEventRepository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1,
                    title = "Afternoon",
                    date = today,
                    startTime = LocalTime.of(14, 0)
                ),
                CalendarEvent(
                    id = 2,
                    title = "Morning",
                    date = today,
                    startTime = LocalTime.of(9, 0)
                ),
                CalendarEvent(
                    id = 3,
                    title = "All day",
                    date = today,
                    startTime = null
                )
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val events = expectMostRecentItem().todayEvents
            assertEquals(3, events.size)
            assertEquals("All day", events[0].title)
            assertEquals("Morning", events[1].title)
            assertEquals("Afternoon", events[2].title)
        }
    }

    private fun setupTestData(today: LocalDate) {
        medicationRepository.setMedications(
            listOf(Medication(id = 1, name = "Test Med", timings = listOf(MedicationTiming.MORNING)))
        )
        medicationLogRepository.setLogs(
            listOf(
                MedicationLog(
                    id = 1,
                    medicationId = 1,
                    status = MedicationLogStatus.TAKEN,
                    scheduledAt = today.atTime(8, 0)
                )
            )
        )
        taskRepository.setTasks(
            listOf(Task(id = 1, title = "Test Task", dueDate = today.plusDays(1)))
        )
        healthRecordRepository.setRecords(
            listOf(HealthRecord(id = 1, temperature = 36.5, recordedAt = today.atTime(8, 0)))
        )
        noteRepository.setNotes(
            listOf(Note(id = 1, title = "Test Note", content = "content", updatedAt = today.atStartOfDay()))
        )
        calendarEventRepository.setEvents(
            listOf(CalendarEvent(id = 1, title = "Test Event", date = today, startTime = LocalTime.of(9, 0)))
        )
    }
}
