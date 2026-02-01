package com.carenote.app.ui.screens.calendar

import app.cash.turbine.test
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.fakes.FakeCalendarEventRepository
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeCalendarEventRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCalendarEventRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CalendarViewModel {
        return CalendarViewModel(repository)
    }

    private fun createEvent(
        id: Long = 1L,
        title: String = "テスト予定",
        description: String = "テスト説明",
        date: LocalDate = LocalDate.of(2025, 3, 15),
        startTime: LocalTime? = null,
        endTime: LocalTime? = null,
        isAllDay: Boolean = true,
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ) = CalendarEvent(
        id = id,
        title = title,
        description = description,
        date = date,
        startTime = startTime,
        endTime = endTime,
        isAllDay = isAllDay,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    @Test
    fun `initial selected date is today`() {
        viewModel = createViewModel()

        assertEquals(LocalDate.now(), viewModel.selectedDate.value)
    }

    @Test
    fun `initial current month is this month`() {
        viewModel = createViewModel()

        assertEquals(YearMonth.now(), viewModel.currentMonth.value)
    }

    @Test
    fun `initial events state is Loading`() {
        viewModel = createViewModel()

        assertTrue(viewModel.eventsForSelectedDate.value is UiState.Loading)
    }

    @Test
    fun `events loaded as Success for selected date`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        val events = listOf(
            createEvent(id = 1L, title = "予定A", date = today),
            createEvent(id = 2L, title = "予定B", date = today)
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
    fun `empty events shows Success with empty list`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(0, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `selectDate updates selected date`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        val newDate = LocalDate.of(2025, 6, 1)

        viewModel.selectDate(newDate)

        assertEquals(newDate, viewModel.selectedDate.value)
    }

    @Test
    fun `selectDate updates events for selected date`() = runTest(testDispatcher) {
        val dateA = LocalDate.of(2025, 3, 15)
        val dateB = LocalDate.of(2025, 3, 16)
        val events = listOf(
            createEvent(id = 1L, title = "予定A", date = dateA),
            createEvent(id = 2L, title = "予定B", date = dateB)
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
    fun `changeMonth updates current month`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        val newMonth = YearMonth.of(2025, 6)

        viewModel.changeMonth(newMonth)

        assertEquals(newMonth, viewModel.currentMonth.value)
    }

    @Test
    fun `eventsForMonth returns events grouped by date`() = runTest(testDispatcher) {
        val date1 = LocalDate.of(2025, 3, 15)
        val date2 = LocalDate.of(2025, 3, 16)
        val events = listOf(
            createEvent(id = 1L, title = "予定A", date = date1),
            createEvent(id = 2L, title = "予定B", date = date1),
            createEvent(id = 3L, title = "予定C", date = date2)
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
    fun `eventsForMonth updates when month changes`() = runTest(testDispatcher) {
        val marchDate = LocalDate.of(2025, 3, 15)
        val aprilDate = LocalDate.of(2025, 4, 10)
        val events = listOf(
            createEvent(id = 1L, title = "3月の予定", date = marchDate),
            createEvent(id = 2L, title = "4月の予定", date = aprilDate)
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
    fun `deleteEvent removes event`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        val events = listOf(
            createEvent(id = 1L, title = "予定A", date = today),
            createEvent(id = 2L, title = "予定B", date = today)
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
    fun `snackbar emitted on delete success`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        repository.setEvents(listOf(createEvent(id = 1L, date = today)))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteEvent(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertEquals(CalendarViewModel.SNACKBAR_DELETED, event.message)
        }
    }

    @Test
    fun `snackbar emitted on delete failure`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        repository.setEvents(listOf(createEvent(id = 1L, date = today)))
        repository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteEvent(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertEquals(CalendarViewModel.SNACKBAR_DELETE_FAILED, event.message)
        }
    }

    @Test
    fun `events update reactively when repository changes`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        viewModel = createViewModel()

        viewModel.eventsForSelectedDate.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(0, (initial as UiState.Success).data.size)

            repository.setEvents(listOf(createEvent(id = 1L, date = today)))
            advanceUntilIdle()
            val updated = expectMostRecentItem()
            assertTrue(updated is UiState.Success)
            assertEquals(1, (updated as UiState.Success).data.size)
        }
    }
}
