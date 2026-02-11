package com.carenote.app.ui.screens.timeline

import app.cash.turbine.test
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakeTimelineRepository
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeClock = FakeClock()
    private lateinit var repository: FakeTimelineRepository
    private lateinit var viewModel: TimelineViewModel

    private val testDate = LocalDate.of(2025, 6, 15)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTimelineRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TimelineViewModel {
        return TimelineViewModel(repository, fakeClock)
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
}
