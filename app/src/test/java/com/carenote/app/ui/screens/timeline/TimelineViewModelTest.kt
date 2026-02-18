package com.carenote.app.ui.screens.timeline

import app.cash.turbine.test
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.model.TimelineFilterType
import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeCalendarEventRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakeTimelineRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private val fakeClock = FakeClock()
    private lateinit var repository: FakeTimelineRepository
    private lateinit var calendarEventRepository: FakeCalendarEventRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: TimelineViewModel

    private val testDate = LocalDate.of(2025, 6, 15)

    @Before
    fun setUp() {
        repository = FakeTimelineRepository()
        calendarEventRepository = FakeCalendarEventRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    private fun createViewModel(): TimelineViewModel {
        return TimelineViewModel(repository, calendarEventRepository, analyticsRepository, fakeClock)
    }

    @Test
    fun `initial state is Loading then transitions to Success`() = runTest {
        viewModel = createViewModel()

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(emptyList<TimelineItem>(), (state as UiState.Success).data)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `selectedDate defaults to today`() = runTest {
        viewModel = createViewModel()
        assertEquals(LocalDate.of(2026, 1, 15), viewModel.selectedDate.value)
    }

    @Test
    fun `selectDate changes the date and re-emits items`() = runTest {
        val event = TimelineItem.CalendarEventItem(
            event = CalendarEvent(
                id = 1L,
                title = "Test Event",
                date = testDate,
                startTime = LocalTime.of(10, 0)
            )
        )
        repository.setItems(listOf(event))

        viewModel = createViewModel()
        viewModel.selectDate(testDate)

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val items = (state as UiState.Success).data
            assertEquals(1, items.size)
            assertTrue(items[0] is TimelineItem.CalendarEventItem)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `goToPreviousDay decrements date by 1`() = runTest {
        viewModel = createViewModel()
        val today = LocalDate.of(2026, 1, 15)
        viewModel.goToPreviousDay()
        assertEquals(today.minusDays(1), viewModel.selectedDate.value)
    }

    @Test
    fun `goToNextDay increments date by 1`() = runTest {
        viewModel = createViewModel()
        val today = LocalDate.of(2026, 1, 15)
        viewModel.goToNextDay()
        assertEquals(today.plusDays(1), viewModel.selectedDate.value)
    }

    @Test
    fun `goToToday resets to today`() = runTest {
        viewModel = createViewModel()
        viewModel.selectDate(testDate)
        assertEquals(testDate, viewModel.selectedDate.value)

        viewModel.goToToday()
        assertEquals(LocalDate.of(2026, 1, 15), viewModel.selectedDate.value)
    }

    @Test
    fun `refresh triggers re-subscription`() = runTest {
        viewModel = createViewModel()

        val note = TimelineItem.NoteItem(
            note = Note(
                id = 1L,
                title = "Added after",
                content = "Content",
                tag = NoteTag.OTHER,
                createdAt = LocalDate.of(2026, 1, 15).atTime(12, 0),
                updatedAt = LocalDate.of(2026, 1, 15).atTime(12, 0)
            )
        )

        viewModel.timelineItems.test {
            // Initially empty
            val first = awaitItem()
            assertTrue(first is UiState.Success)
            assertEquals(0, (first as UiState.Success).data.size)

            // Add item and refresh
            repository.setItems(listOf(note))
            viewModel.refresh()

            val second = awaitItem()
            assertTrue(second is UiState.Success)
            assertEquals(1, (second as UiState.Success).data.size)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `error state is emitted when repository fails`() = runTest {
        repository.shouldFail = true
        viewModel = createViewModel()

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `filterType defaults to ALL`() = runTest {
        viewModel = createViewModel()
        assertEquals(TimelineFilterType.ALL, viewModel.filterType.value)
    }

    @Test
    fun `setFilter changes filter type`() = runTest {
        viewModel = createViewModel()
        viewModel.setFilter(TimelineFilterType.TASK)
        assertEquals(TimelineFilterType.TASK, viewModel.filterType.value)
    }

    @Test
    fun `filter TASK shows only task items`() = runTest {
        val taskEvent = TimelineItem.CalendarEventItem(
            event = CalendarEvent(
                id = 1L,
                title = "Task",
                date = LocalDate.of(2026, 1, 15),
                type = CalendarEventType.TASK,
                startTime = LocalTime.of(9, 0)
            )
        )
        val normalEvent = TimelineItem.CalendarEventItem(
            event = CalendarEvent(
                id = 2L,
                title = "Event",
                date = LocalDate.of(2026, 1, 15),
                type = CalendarEventType.OTHER,
                startTime = LocalTime.of(10, 0)
            )
        )
        val noteItem = TimelineItem.NoteItem(
            note = Note(
                id = 1L, title = "Note", content = "c", tag = NoteTag.OTHER,
                createdAt = LocalDate.of(2026, 1, 15).atTime(11, 0),
                updatedAt = LocalDate.of(2026, 1, 15).atTime(11, 0)
            )
        )
        repository.setItems(listOf(taskEvent, normalEvent, noteItem))

        viewModel = createViewModel()
        viewModel.setFilter(TimelineFilterType.TASK)

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val items = (state as UiState.Success).data
            assertEquals(1, items.size)
            assertTrue(items[0] is TimelineItem.CalendarEventItem)
            assertEquals(CalendarEventType.TASK, (items[0] as TimelineItem.CalendarEventItem).event.type)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `filter EVENT shows only non-task calendar events`() = runTest {
        val taskEvent = TimelineItem.CalendarEventItem(
            event = CalendarEvent(
                id = 1L, title = "Task", date = LocalDate.of(2026, 1, 15),
                type = CalendarEventType.TASK, startTime = LocalTime.of(9, 0)
            )
        )
        val normalEvent = TimelineItem.CalendarEventItem(
            event = CalendarEvent(
                id = 2L, title = "Hospital", date = LocalDate.of(2026, 1, 15),
                type = CalendarEventType.HOSPITAL, startTime = LocalTime.of(10, 0)
            )
        )
        repository.setItems(listOf(taskEvent, normalEvent))

        viewModel = createViewModel()
        viewModel.setFilter(TimelineFilterType.EVENT)

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val items = (state as UiState.Success).data
            assertEquals(1, items.size)
            assertTrue((items[0] as TimelineItem.CalendarEventItem).event.type == CalendarEventType.HOSPITAL)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `filter MEDICATION shows only medication items`() = runTest {
        val medItem = TimelineItem.MedicationLogItem(
            log = MedicationLog(
                id = 1L, medicationId = 1L,
                status = MedicationLogStatus.TAKEN,
                scheduledAt = LocalDate.of(2026, 1, 15).atTime(8, 0)
            ),
            medicationName = "TestMed"
        )
        val noteItem = TimelineItem.NoteItem(
            note = Note(
                id = 1L, title = "N", content = "c", tag = NoteTag.OTHER,
                createdAt = LocalDate.of(2026, 1, 15).atTime(11, 0),
                updatedAt = LocalDate.of(2026, 1, 15).atTime(11, 0)
            )
        )
        repository.setItems(listOf(medItem, noteItem))

        viewModel = createViewModel()
        viewModel.setFilter(TimelineFilterType.MEDICATION)

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val items = (state as UiState.Success).data
            assertEquals(1, items.size)
            assertTrue(items[0] is TimelineItem.MedicationLogItem)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `filter HEALTH_RECORD shows only health record items`() = runTest {
        val hrItem = TimelineItem.HealthRecordItem(
            record = HealthRecord(
                id = 1L,
                recordedAt = LocalDate.of(2026, 1, 15).atTime(7, 0),
                createdAt = LocalDate.of(2026, 1, 15).atTime(7, 0),
                updatedAt = LocalDate.of(2026, 1, 15).atTime(7, 0)
            )
        )
        val noteItem = TimelineItem.NoteItem(
            note = Note(
                id = 1L, title = "N", content = "c", tag = NoteTag.OTHER,
                createdAt = LocalDate.of(2026, 1, 15).atTime(11, 0),
                updatedAt = LocalDate.of(2026, 1, 15).atTime(11, 0)
            )
        )
        repository.setItems(listOf(hrItem, noteItem))

        viewModel = createViewModel()
        viewModel.setFilter(TimelineFilterType.HEALTH_RECORD)

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val items = (state as UiState.Success).data
            assertEquals(1, items.size)
            assertTrue(items[0] is TimelineItem.HealthRecordItem)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `filter NOTE shows only note items`() = runTest {
        val eventItem = TimelineItem.CalendarEventItem(
            event = CalendarEvent(
                id = 1L, title = "E", date = LocalDate.of(2026, 1, 15),
                startTime = LocalTime.of(9, 0)
            )
        )
        val noteItem = TimelineItem.NoteItem(
            note = Note(
                id = 1L, title = "N", content = "c", tag = NoteTag.OTHER,
                createdAt = LocalDate.of(2026, 1, 15).atTime(11, 0),
                updatedAt = LocalDate.of(2026, 1, 15).atTime(11, 0)
            )
        )
        repository.setItems(listOf(eventItem, noteItem))

        viewModel = createViewModel()
        viewModel.setFilter(TimelineFilterType.NOTE)

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val items = (state as UiState.Success).data
            assertEquals(1, items.size)
            assertTrue(items[0] is TimelineItem.NoteItem)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `filter is maintained when date changes`() = runTest {
        viewModel = createViewModel()
        viewModel.setFilter(TimelineFilterType.TASK)
        viewModel.selectDate(testDate)
        assertEquals(TimelineFilterType.TASK, viewModel.filterType.value)
    }

    @Test
    fun `toggleCompleted updates event and logs analytics`() = runTest {
        val event = CalendarEvent(
            id = 1L, title = "Task", date = LocalDate.of(2026, 1, 15),
            type = CalendarEventType.TASK, completed = false
        )
        calendarEventRepository.setEvents(listOf(event))

        viewModel = createViewModel()
        viewModel.toggleCompleted(1L, true)

        val updatedEvent = calendarEventRepository.getEventById(1L).firstOrNull()
        assertTrue(updatedEvent?.completed == true)

        assertTrue(analyticsRepository.loggedEvents.any {
            it.first == AppConfig.Analytics.EVENT_CALENDAR_EVENT_COMPLETED
        })
    }

    @Test
    fun `toggleCompleted failure shows snackbar error`() = runTest {
        val event = CalendarEvent(
            id = 1L, title = "Task", date = LocalDate.of(2026, 1, 15),
            type = CalendarEventType.TASK, completed = false
        )
        calendarEventRepository.setEvents(listOf(event))
        calendarEventRepository.shouldFail = true

        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.toggleCompleted(1L, true)

            val snackbarEvent = awaitItem()
            assertTrue(snackbarEvent is SnackbarEvent.WithResId)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `toggleCompleted with non-existent event does nothing`() = runTest {
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.toggleCompleted(999L, true)

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `filter ALL shows all item types`() = runTest {
        val taskItem = TimelineItem.CalendarEventItem(
            event = CalendarEvent(
                id = 1L, title = "Task", date = LocalDate.of(2026, 1, 15),
                type = CalendarEventType.TASK, startTime = LocalTime.of(9, 0)
            )
        )
        val eventItem = TimelineItem.CalendarEventItem(
            event = CalendarEvent(
                id = 2L, title = "Event", date = LocalDate.of(2026, 1, 15),
                type = CalendarEventType.HOSPITAL, startTime = LocalTime.of(10, 0)
            )
        )
        val medItem = TimelineItem.MedicationLogItem(
            log = MedicationLog(
                id = 1L, medicationId = 1L,
                status = MedicationLogStatus.TAKEN,
                scheduledAt = LocalDate.of(2026, 1, 15).atTime(8, 0)
            ),
            medicationName = "TestMed"
        )
        val noteItem = TimelineItem.NoteItem(
            note = Note(
                id = 1L, title = "N", content = "c", tag = NoteTag.OTHER,
                createdAt = LocalDate.of(2026, 1, 15).atTime(11, 0),
                updatedAt = LocalDate.of(2026, 1, 15).atTime(11, 0)
            )
        )
        val hrItem = TimelineItem.HealthRecordItem(
            record = HealthRecord(
                id = 1L,
                recordedAt = LocalDate.of(2026, 1, 15).atTime(7, 0),
                createdAt = LocalDate.of(2026, 1, 15).atTime(7, 0),
                updatedAt = LocalDate.of(2026, 1, 15).atTime(7, 0)
            )
        )
        repository.setItems(listOf(taskItem, eventItem, medItem, noteItem, hrItem))

        viewModel = createViewModel()
        viewModel.setFilter(TimelineFilterType.ALL)

        viewModel.timelineItems.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val items = (state as UiState.Success).data
            assertEquals(5, items.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `toggleCompleted sets correct completed value`() = runTest {
        val event = CalendarEvent(
            id = 1L, title = "Task", date = LocalDate.of(2026, 1, 15),
            type = CalendarEventType.TASK, completed = true
        )
        calendarEventRepository.setEvents(listOf(event))

        viewModel = createViewModel()
        viewModel.toggleCompleted(1L, false)

        val updatedEvent = calendarEventRepository.getEventById(1L).firstOrNull()
        assertTrue(updatedEvent?.completed == false)
    }
}
