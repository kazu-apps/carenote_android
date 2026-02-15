package com.carenote.app.ui.screens.calendar

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.fakes.FakeCalendarEventRepository
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aCalendarEvent
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val fakeClock = FakeClock()
    private lateinit var repository: FakeCalendarEventRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        repository = FakeCalendarEventRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    private fun createViewModel(): CalendarViewModel {
        return CalendarViewModel(repository, analyticsRepository, fakeClock)
    }


    @Test
    fun `initial selected date is today`() {
        viewModel = createViewModel()

        assertEquals(LocalDate.of(2026, 1, 15), viewModel.selectedDate.value)
    }

    @Test
    fun `initial current month is this month`() {
        viewModel = createViewModel()

        assertEquals(YearMonth.of(2026, 1), viewModel.currentMonth.value)
    }

    @Test
    fun `initial events state is Loading`() {
        viewModel = createViewModel()

        assertTrue(viewModel.eventsForSelectedDate.value is UiState.Loading)
    }

    @Test
    fun `eventsForSelectedDate transitions from Loading to Success`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val events = listOf(aCalendarEvent(id = 1L, title = "予定A", date = today))
        repository.setEvents(events)
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(1, (success as UiState.Success).data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `events loaded as Success for selected date`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val events = listOf(
            aCalendarEvent(id = 1L, title = "予定A", date = today),
            aCalendarEvent(id = 2L, title = "予定B", date = today)
        )
        repository.setEvents(events)
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(2, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `empty events shows Success with empty list`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(0, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `selectDate updates selected date`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        val newDate = LocalDate.of(2025, 6, 1)

        viewModel.selectDate(newDate)

        assertEquals(newDate, viewModel.selectedDate.value)
    }

    @Test
    fun `selectDate updates events for selected date`() = runTest(mainCoroutineRule.testDispatcher) {
        val dateA = LocalDate.of(2025, 3, 15)
        val dateB = LocalDate.of(2025, 3, 16)
        val events = listOf(
            aCalendarEvent(id = 1L, title = "予定A", date = dateA),
            aCalendarEvent(id = 2L, title = "予定B", date = dateB)
        )
        repository.setEvents(events)
        viewModel = createViewModel()
        viewModel.selectDate(dateA)

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("予定A", data[0].title)
        }
    }

    @Test
    fun `changeMonth updates current month`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        val newMonth = YearMonth.of(2025, 6)

        viewModel.changeMonth(newMonth)

        assertEquals(newMonth, viewModel.currentMonth.value)
    }

    @Test
    fun `eventsForMonth returns events grouped by date`() = runTest(mainCoroutineRule.testDispatcher) {
        val date1 = LocalDate.of(2025, 3, 15)
        val date2 = LocalDate.of(2025, 3, 16)
        val events = listOf(
            aCalendarEvent(id = 1L, title = "予定A", date = date1),
            aCalendarEvent(id = 2L, title = "予定B", date = date1),
            aCalendarEvent(id = 3L, title = "予定C", date = date2)
        )
        repository.setEvents(events)
        viewModel = createViewModel()
        viewModel.changeMonth(YearMonth.of(2025, 3))

        viewModel.eventsForMonth.test {
            advanceUntilIdle()
            val monthMap = expectMostRecentItem()
            assertEquals(2, monthMap.size)
            assertEquals(2, monthMap[date1]?.size)
            assertEquals(1, monthMap[date2]?.size)
        }
    }

    @Test
    fun `eventsForMonth updates when month changes`() = runTest(mainCoroutineRule.testDispatcher) {
        val marchDate = LocalDate.of(2025, 3, 15)
        val aprilDate = LocalDate.of(2025, 4, 10)
        val events = listOf(
            aCalendarEvent(id = 1L, title = "3月の予定", date = marchDate),
            aCalendarEvent(id = 2L, title = "4月の予定", date = aprilDate)
        )
        repository.setEvents(events)
        viewModel = createViewModel()

        viewModel.eventsForMonth.test {
            viewModel.changeMonth(YearMonth.of(2025, 3))
            advanceUntilIdle()
            val marchMap = expectMostRecentItem()
            assertEquals(1, marchMap.size)
            assertTrue(marchMap.containsKey(marchDate))

            viewModel.changeMonth(YearMonth.of(2025, 4))
            advanceUntilIdle()
            val aprilMap = expectMostRecentItem()
            assertEquals(1, aprilMap.size)
            assertTrue(aprilMap.containsKey(aprilDate))
        }
    }

    @Test
    fun `deleteEvent removes event`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val events = listOf(
            aCalendarEvent(id = 1L, title = "予定A", date = today),
            aCalendarEvent(id = 2L, title = "予定B", date = today)
        )
        repository.setEvents(events)
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()

            viewModel.deleteEvent(1L)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("予定B", data[0].title)
        }
    }

    @Test
    fun `snackbar emitted on delete success`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        repository.setEvents(listOf(aCalendarEvent(id = 1L, date = today)))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteEvent(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.calendar_event_deleted, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `snackbar emitted on delete failure`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        repository.setEvents(listOf(aCalendarEvent(id = 1L, date = today)))
        repository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteEvent(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.calendar_event_delete_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `events update reactively when repository changes`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(0, (initial as UiState.Success).data.size)

            repository.setEvents(listOf(aCalendarEvent(id = 1L, date = today)))
            advanceUntilIdle()
            val updated = expectMostRecentItem()
            assertTrue(updated is UiState.Success)
            assertEquals(1, (updated as UiState.Success).data.size)
        }
    }

    @Test
    fun `refresh triggers data reload`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val events = listOf(aCalendarEvent(id = 1L, title = "予定A", date = today))
        repository.setEvents(events)
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(1, (initial as UiState.Success).data.size)

            repository.setEvents(
                listOf(
                    aCalendarEvent(id = 1L, title = "予定A", date = today),
                    aCalendarEvent(id = 2L, title = "予定B", date = today)
                )
            )
            viewModel.refresh()
            advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertTrue(refreshed is UiState.Success)
            assertEquals(2, (refreshed as UiState.Success).data.size)
        }
    }

    @Test
    fun `isRefreshing becomes false after data loads`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        repository.setEvents(listOf(aCalendarEvent(id = 1L, date = today)))
        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isRefreshing.value)

        viewModel.refresh()
        assertTrue(viewModel.isRefreshing.value)

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `toggleCompleted updates event completion status`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val event = aCalendarEvent(id = 1L, title = "予定A", date = today)
        repository.setEvents(listOf(event))
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()

            viewModel.toggleCompleted(event)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertTrue(data[0].completed)
        }
    }

    @Test
    fun `toggleCompleted logs analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val event = aCalendarEvent(id = 1L, date = today)
        repository.setEvents(listOf(event))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCompleted(event)
        advanceUntilIdle()

        assertTrue(analyticsRepository.loggedEvents.any { it.first == AppConfig.Analytics.EVENT_CALENDAR_EVENT_COMPLETED })
    }

    @Test
    fun `toggleCompleted failure shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val event = aCalendarEvent(id = 1L, date = today)
        repository.setEvents(listOf(event))
        viewModel = createViewModel()
        advanceUntilIdle()

        repository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.toggleCompleted(event)
            advanceUntilIdle()
            val snackEvent = awaitItem()
            assertTrue(snackEvent is SnackbarEvent.WithResId)
            assertEquals(R.string.calendar_event_save_failed, (snackEvent as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `toggleCompleted reverses completed state`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val event = aCalendarEvent(id = 1L, date = today).copy(completed = true)
        repository.setEvents(listOf(event))
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()

            viewModel.toggleCompleted(event)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertFalse((state as UiState.Success).data[0].completed)
        }
    }
}
