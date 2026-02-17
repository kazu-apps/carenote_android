package com.carenote.app.ui.screens.home

import androidx.paging.PagingData
import app.cash.turbine.test
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.TaskRepository
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeCalendarEventRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakeHealthRecordRepository
import com.carenote.app.fakes.FakeMedicationLogRepository
import com.carenote.app.fakes.FakeMedicationRepository
import com.carenote.app.fakes.FakeNoteRepository
import com.carenote.app.fakes.FakeTaskRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aCalendarEvent
import com.carenote.app.testing.aMedication
import com.carenote.app.testing.aMedicationLog
import com.carenote.app.testing.aTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

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
        medicationRepository = FakeMedicationRepository()
        medicationLogRepository = FakeMedicationLogRepository()
        taskRepository = FakeTaskRepository()
        healthRecordRepository = FakeHealthRecordRepository()
        noteRepository = FakeNoteRepository()
        calendarEventRepository = FakeCalendarEventRepository()
        analyticsRepository = FakeAnalyticsRepository()
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
    fun `initial state is loading`() = runTest(mainCoroutineRule.testDispatcher) {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads all sections with data`() = runTest(mainCoroutineRule.testDispatcher) {
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
    fun `empty state when no data`() = runTest(mainCoroutineRule.testDispatcher) {
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
    fun `tasks are sorted by due date`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = fakeClock.today()
        taskRepository.setTasks(
            listOf(
                aTask(id = 1, title = "Later", dueDate = today.plusDays(5)),
                aTask(id = 2, title = "Soon", dueDate = today.plusDays(1)),
                aTask(id = 3, title = "No date", dueDate = null)
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
    fun `health records show latest by recordedAt`() = runTest(mainCoroutineRule.testDispatcher) {
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
    fun `notes are sorted by updatedAt descending`() = runTest(mainCoroutineRule.testDispatcher) {
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
    fun `section items are limited to MAX_SECTION_ITEMS`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = fakeClock.today()
        val medications = (1..10).map { i ->
            aMedication(id = i.toLong(), name = "Med $i")
        }
        medicationRepository.setMedications(medications)

        val tasks = (1..10).map { i ->
            aTask(id = i.toLong(), title = "Task $i", dueDate = today.plusDays(i.toLong()))
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
            aCalendarEvent(
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
    fun `refresh triggers data reload`() = runTest(mainCoroutineRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial.todayMedications.isEmpty())

            medicationRepository.setMedications(
                listOf(aMedication(id = 1, name = "New Med"))
            )

            viewModel.refresh()
            advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertFalse(viewModel.isRefreshing.value)
            assertEquals(1, refreshed.todayMedications.size)
        }
    }

    @Test
    fun `MedicationWithLog correctly matches logs to medications`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = fakeClock.today()
        val todayTime = today.atTime(8, 0)

        medicationRepository.setMedications(
            listOf(
                aMedication(
                    id = 1,
                    name = "Med A",
                    timings = listOf(MedicationTiming.MORNING, MedicationTiming.EVENING)
                ),
                aMedication(
                    id = 2,
                    name = "Med B",
                    timings = listOf(MedicationTiming.MORNING)
                )
            )
        )

        medicationLogRepository.setLogs(
            listOf(
                aMedicationLog(
                    id = 1,
                    medicationId = 1,
                    status = MedicationLogStatus.TAKEN,
                    scheduledAt = todayTime,
                    timing = MedicationTiming.MORNING
                ),
                aMedicationLog(
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
    fun `logSeeAllClicked records analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.logSeeAllClicked("medication")

        assertEquals(1, analyticsRepository.loggedEvents.size)
        val (name, params) = analyticsRepository.loggedEvents[0]
        assertEquals(AppConfig.Analytics.EVENT_HOME_SEE_ALL_CLICKED, name)
        assertEquals("medication", params[AppConfig.Analytics.PARAM_SECTION])
    }

    @Test
    fun `calendar events sorted by startTime`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = fakeClock.today()
        calendarEventRepository.setEvents(
            listOf(
                aCalendarEvent(
                    id = 1,
                    title = "Afternoon",
                    date = today,
                    startTime = LocalTime.of(14, 0)
                ),
                aCalendarEvent(
                    id = 2,
                    title = "Morning",
                    date = today,
                    startTime = LocalTime.of(9, 0)
                ),
                aCalendarEvent(
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
            listOf(aMedication(id = 1, name = "Test Med", timings = listOf(MedicationTiming.MORNING)))
        )
        medicationLogRepository.setLogs(
            listOf(
                aMedicationLog(
                    id = 1,
                    medicationId = 1,
                    status = MedicationLogStatus.TAKEN,
                    scheduledAt = today.atTime(8, 0)
                )
            )
        )
        taskRepository.setTasks(
            listOf(aTask(id = 1, title = "Test Task", dueDate = today.plusDays(1)))
        )
        healthRecordRepository.setRecords(
            listOf(HealthRecord(id = 1, temperature = 36.5, recordedAt = today.atTime(8, 0)))
        )
        noteRepository.setNotes(
            listOf(Note(id = 1, title = "Test Note", content = "content", updatedAt = today.atStartOfDay()))
        )
        calendarEventRepository.setEvents(
            listOf(aCalendarEvent(id = 1, title = "Test Event", date = today, startTime = LocalTime.of(9, 0)))
        )
    }

    // --- Error scenario tests ---

    @Test
    fun `medication repository exception is caught and shows error state`() = runTest(mainCoroutineRule.testDispatcher) {
        val failingMedicationRepo = object : MedicationRepository {
            override fun getAllMedications(): Flow<List<Medication>> = flow {
                throw RuntimeException("Database corrupted")
            }
            override fun getMedicationById(id: Long) = medicationRepository.getMedicationById(id)
            override fun searchMedications(query: String) = medicationRepository.searchMedications(query)
            override suspend fun insertMedication(medication: Medication) = medicationRepository.insertMedication(medication)
            override suspend fun updateMedication(medication: Medication) = medicationRepository.updateMedication(medication)
            override suspend fun deleteMedication(id: Long) = medicationRepository.deleteMedication(id)
            override suspend fun decrementStock(medicationId: Long, amount: Int) = medicationRepository.decrementStock(medicationId, amount)
        }

        val viewModel = HomeViewModel(
            medicationRepository = failingMedicationRepo,
            medicationLogRepository = medicationLogRepository,
            taskRepository = taskRepository,
            healthRecordRepository = healthRecordRepository,
            noteRepository = noteRepository,
            calendarEventRepository = calendarEventRepository,
            analyticsRepository = analyticsRepository,
            clock = fakeClock
        )

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals("Database corrupted", state.error)
        }
    }

    @Test
    fun `task repository exception is caught gracefully`() = runTest(mainCoroutineRule.testDispatcher) {
        val failingTaskRepo = object : TaskRepository {
            override fun getIncompleteTasks(): Flow<List<Task>> = flow {
                throw RuntimeException("Task DB error")
            }
            override fun getAllTasks() = taskRepository.getAllTasks()
            override fun getTaskById(id: Long) = taskRepository.getTaskById(id)
            override fun getTasksByDueDate(date: LocalDate) = taskRepository.getTasksByDueDate(date)
            override fun getPagedAllTasks(query: String): Flow<PagingData<Task>> = taskRepository.getPagedAllTasks(query)
            override fun getPagedIncompleteTasks(query: String): Flow<PagingData<Task>> = taskRepository.getPagedIncompleteTasks(query)
            override fun getPagedCompletedTasks(query: String): Flow<PagingData<Task>> = taskRepository.getPagedCompletedTasks(query)
            override fun getIncompleteTaskCount() = taskRepository.getIncompleteTaskCount()
            override suspend fun insertTask(task: Task) = taskRepository.insertTask(task)
            override suspend fun updateTask(task: Task) = taskRepository.updateTask(task)
            override suspend fun deleteTask(id: Long) = taskRepository.deleteTask(id)
        }

        val viewModel = HomeViewModel(
            medicationRepository = medicationRepository,
            medicationLogRepository = medicationLogRepository,
            taskRepository = failingTaskRepo,
            healthRecordRepository = healthRecordRepository,
            noteRepository = noteRepository,
            calendarEventRepository = calendarEventRepository,
            analyticsRepository = analyticsRepository,
            clock = fakeClock
        )

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNotNull(state.error)
        }
    }

    @Test
    fun `calendar repository exception is caught gracefully`() = runTest(mainCoroutineRule.testDispatcher) {
        val failingCalendarRepo = object : CalendarEventRepository {
            override fun getEventsByDate(date: LocalDate): Flow<List<CalendarEvent>> = flow {
                throw RuntimeException("Calendar DB error")
            }
            override fun getAllEvents() = calendarEventRepository.getAllEvents()
            override fun getEventById(id: Long) = calendarEventRepository.getEventById(id)
            override fun getEventsByDateRange(startDate: LocalDate, endDate: LocalDate) =
                calendarEventRepository.getEventsByDateRange(startDate, endDate)
            override suspend fun insertEvent(event: CalendarEvent) = calendarEventRepository.insertEvent(event)
            override suspend fun updateEvent(event: CalendarEvent) = calendarEventRepository.updateEvent(event)
            override suspend fun deleteEvent(id: Long) = calendarEventRepository.deleteEvent(id)
            override fun getTaskEvents() = calendarEventRepository.getTaskEvents()
            override fun getIncompleteTaskEvents() = calendarEventRepository.getIncompleteTaskEvents()
            override fun getTaskEventsByDate(date: LocalDate) = calendarEventRepository.getTaskEventsByDate(date)
            override fun getPagedTaskEvents(query: String) = calendarEventRepository.getPagedTaskEvents(query)
            override fun getIncompleteTaskCount() = calendarEventRepository.getIncompleteTaskCount()
        }

        val viewModel = HomeViewModel(
            medicationRepository = medicationRepository,
            medicationLogRepository = medicationLogRepository,
            taskRepository = taskRepository,
            healthRecordRepository = healthRecordRepository,
            noteRepository = noteRepository,
            calendarEventRepository = failingCalendarRepo,
            analyticsRepository = analyticsRepository,
            clock = fakeClock
        )

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNotNull(state.error)
        }
    }

    @Test
    fun `error state persists after catch terminates flow`() = runTest(mainCoroutineRule.testDispatcher) {
        val errorMedicationRepo = object : MedicationRepository {
            override fun getAllMedications(): Flow<List<Medication>> = flow {
                throw RuntimeException("Temporary error")
            }
            override fun getMedicationById(id: Long) = medicationRepository.getMedicationById(id)
            override fun searchMedications(query: String) = medicationRepository.searchMedications(query)
            override suspend fun insertMedication(medication: Medication) = medicationRepository.insertMedication(medication)
            override suspend fun updateMedication(medication: Medication) = medicationRepository.updateMedication(medication)
            override suspend fun deleteMedication(id: Long) = medicationRepository.deleteMedication(id)
            override suspend fun decrementStock(medicationId: Long, amount: Int) = medicationRepository.decrementStock(medicationId, amount)
        }

        val viewModel = HomeViewModel(
            medicationRepository = errorMedicationRepo,
            medicationLogRepository = medicationLogRepository,
            taskRepository = taskRepository,
            healthRecordRepository = healthRecordRepository,
            noteRepository = noteRepository,
            calendarEventRepository = calendarEventRepository,
            analyticsRepository = analyticsRepository,
            clock = fakeClock
        )

        viewModel.uiState.test {
            // Initial value
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            advanceUntilIdle()
            val errorState = awaitItem()
            assertNotNull(errorState.error)
            assertEquals("Temporary error", errorState.error)
            assertFalse(errorState.isLoading)
        }
    }
}
