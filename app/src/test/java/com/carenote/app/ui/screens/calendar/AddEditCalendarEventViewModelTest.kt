package com.carenote.app.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.fakes.FakeCalendarEventRepository
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.ui.common.UiText
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
class AddEditCalendarEventViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeCalendarEventRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private val fakeClock = FakeClock()
    private lateinit var viewModel: AddEditCalendarEventViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCalendarEventRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createAddViewModel(): AddEditCalendarEventViewModel {
        return AddEditCalendarEventViewModel(SavedStateHandle(), repository, analyticsRepository, clock = fakeClock)
    }

    private fun createEditViewModel(eventId: Long): AddEditCalendarEventViewModel {
        return AddEditCalendarEventViewModel(
            SavedStateHandle(mapOf("eventId" to eventId)),
            repository,
            analyticsRepository,
            clock = fakeClock
        )
    }

    @Test
    fun `initial form state has defaults for add mode`() {
        viewModel = createAddViewModel()

        val state = viewModel.formState.value

        assertEquals("", state.title)
        assertEquals("", state.description)
        assertNull(state.startTime)
        assertNull(state.endTime)
        assertTrue(state.isAllDay)
        assertNull(state.titleError)
        assertFalse(state.isSaving)
        assertFalse(state.isEditMode)
    }

    @Test
    fun `isEditMode is false in add mode`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.formState.value.isEditMode)
    }

    @Test
    fun `isEditMode is true when eventId provided`() {
        repository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1L,
                    title = "テスト",
                    date = LocalDate.of(2025, 3, 15),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)

        assertTrue(viewModel.formState.value.isEditMode)
    }

    @Test
    fun `edit mode loads existing event data`() = runTest {
        repository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1L,
                    title = "既存予定",
                    description = "既存説明",
                    date = LocalDate.of(2025, 3, 15),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(10, 0),
                    isAllDay = false,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals("既存予定", state.title)
        assertEquals("既存説明", state.description)
        assertEquals(LocalDate.of(2025, 3, 15), state.date)
        assertEquals(LocalTime.of(9, 0), state.startTime)
        assertEquals(LocalTime.of(10, 0), state.endTime)
        assertFalse(state.isAllDay)
    }

    @Test
    fun `updateTitle updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateTitle("新しいタイトル")

        assertEquals("新しいタイトル", viewModel.formState.value.title)
    }

    @Test
    fun `updateTitle clears title error`() {
        viewModel = createAddViewModel()
        viewModel.saveEvent()
        assertNotNull(viewModel.formState.value.titleError)

        viewModel.updateTitle("タイトル")

        assertNull(viewModel.formState.value.titleError)
    }

    @Test
    fun `updateDescription updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateDescription("新しい説明")

        assertEquals("新しい説明", viewModel.formState.value.description)
    }

    @Test
    fun `updateDate updates form state`() {
        viewModel = createAddViewModel()
        val newDate = LocalDate.of(2025, 6, 1)

        viewModel.updateDate(newDate)

        assertEquals(newDate, viewModel.formState.value.date)
    }

    @Test
    fun `updateStartTime updates form state`() {
        viewModel = createAddViewModel()
        val time = LocalTime.of(9, 30)

        viewModel.updateStartTime(time)

        assertEquals(time, viewModel.formState.value.startTime)
    }

    @Test
    fun `updateEndTime updates form state`() {
        viewModel = createAddViewModel()
        val time = LocalTime.of(17, 0)

        viewModel.updateEndTime(time)

        assertEquals(time, viewModel.formState.value.endTime)
    }

    @Test
    fun `toggleAllDay toggles isAllDay`() {
        viewModel = createAddViewModel()
        assertTrue(viewModel.formState.value.isAllDay)

        viewModel.toggleAllDay()

        assertFalse(viewModel.formState.value.isAllDay)

        viewModel.toggleAllDay()

        assertTrue(viewModel.formState.value.isAllDay)
    }

    @Test
    fun `saveEvent with empty title sets title error`() {
        viewModel = createAddViewModel()

        viewModel.saveEvent()

        assertNotNull(viewModel.formState.value.titleError)
        assertEquals(
            UiText.Resource(R.string.calendar_event_title_required),
            viewModel.formState.value.titleError
        )
    }

    @Test
    fun `saveEvent with blank title sets title error`() {
        viewModel = createAddViewModel()
        viewModel.updateTitle("   ")

        viewModel.saveEvent()

        assertNotNull(viewModel.formState.value.titleError)
    }

    @Test
    fun `saveEvent with valid data succeeds in add mode`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("テスト予定")

        viewModel.saveEvent()
        advanceUntilIdle()

        viewModel.savedEvent.test {
            val saved = awaitItem()
            assertTrue(saved)
        }
    }

    @Test
    fun `saveEvent inserts event to repository in add mode`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("テスト予定")
        viewModel.updateDescription("テスト説明")

        viewModel.saveEvent()
        advanceUntilIdle()

        repository.getAllEvents().test {
            val events = awaitItem()
            assertEquals(1, events.size)
            assertEquals("テスト予定", events[0].title)
            assertEquals("テスト説明", events[0].description)
        }
    }

    @Test
    fun `saveEvent trims title and description`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("  テスト  ")
        viewModel.updateDescription("  説明  ")

        viewModel.saveEvent()
        advanceUntilIdle()

        repository.getAllEvents().test {
            val events = awaitItem()
            assertEquals("テスト", events[0].title)
            assertEquals("説明", events[0].description)
        }
    }

    @Test
    fun `saveEvent updates existing event in edit mode`() = runTest {
        repository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1L,
                    title = "旧タイトル",
                    description = "旧説明",
                    date = LocalDate.of(2025, 3, 15),
                    isAllDay = true,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTitle("新タイトル")
        viewModel.updateDescription("新説明")
        viewModel.saveEvent()
        advanceUntilIdle()

        repository.getAllEvents().test {
            val events = awaitItem()
            assertEquals(1, events.size)
            assertEquals("新タイトル", events[0].title)
            assertEquals("新説明", events[0].description)
        }
    }

    @Test
    fun `isSaving is false initially`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `formState is immutable across updates`() {
        viewModel = createAddViewModel()
        val before = viewModel.formState.value

        viewModel.updateTitle("新しい名前")
        val after = viewModel.formState.value

        assertEquals("", before.title)
        assertEquals("新しい名前", after.title)
    }

    @Test
    fun `save failure keeps isSaving false`() = runTest {
        repository.shouldFail = true
        viewModel = createAddViewModel()
        viewModel.updateTitle("テスト")

        viewModel.saveEvent()
        advanceUntilIdle()

        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `default isAllDay is true`() {
        viewModel = createAddViewModel()

        assertTrue(viewModel.formState.value.isAllDay)
    }

    // --- isDirty Tests ---

    @Test
    fun `isDirty is false initially in add mode`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when title changed`() {
        viewModel = createAddViewModel()

        viewModel.updateTitle("新予定")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty returns to false when title cleared`() {
        viewModel = createAddViewModel()

        viewModel.updateTitle("テスト")
        assertTrue(viewModel.isDirty)

        viewModel.updateTitle("")
        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty is false after loading existing data`() = runTest {
        repository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1L,
                    title = "既存予定",
                    description = "既存説明",
                    date = LocalDate.of(2025, 3, 15),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(10, 0),
                    isAllDay = false,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when field changed in edit mode`() = runTest {
        repository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1L,
                    title = "既存予定",
                    date = LocalDate.of(2025, 3, 15),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTitle("変更予定")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty returns to false when reverted to original in edit mode`() = runTest {
        repository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1L,
                    title = "既存予定",
                    date = LocalDate.of(2025, 3, 15),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTitle("変更予定")
        assertTrue(viewModel.isDirty)

        viewModel.updateTitle("既存予定")
        assertFalse(viewModel.isDirty)
    }

    // --- Type Tests ---

    @Test
    fun `default type is OTHER`() {
        viewModel = createAddViewModel()

        assertEquals(CalendarEventType.OTHER, viewModel.formState.value.type)
    }

    @Test
    fun `updateType updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateType(CalendarEventType.HOSPITAL)

        assertEquals(CalendarEventType.HOSPITAL, viewModel.formState.value.type)
    }

    @Test
    fun `saveEvent includes type in new event`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("通院予定")
        viewModel.updateType(CalendarEventType.HOSPITAL)

        viewModel.saveEvent()
        advanceUntilIdle()

        repository.getAllEvents().test {
            val events = awaitItem()
            assertEquals(1, events.size)
            assertEquals(CalendarEventType.HOSPITAL, events[0].type)
        }
    }

    @Test
    fun `saveEvent includes type in updated event`() = runTest {
        repository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1L,
                    title = "旧タイトル",
                    date = LocalDate.of(2025, 3, 15),
                    type = CalendarEventType.OTHER,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateType(CalendarEventType.VISIT)
        viewModel.saveEvent()
        advanceUntilIdle()

        repository.getAllEvents().test {
            val events = awaitItem()
            assertEquals(CalendarEventType.VISIT, events[0].type)
        }
    }

    @Test
    fun `edit mode loads type from existing event`() = runTest {
        repository.setEvents(
            listOf(
                CalendarEvent(
                    id = 1L,
                    title = "通院",
                    date = LocalDate.of(2025, 3, 15),
                    type = CalendarEventType.HOSPITAL,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        assertEquals(CalendarEventType.HOSPITAL, viewModel.formState.value.type)
    }

    @Test
    fun `isDirty detects type change`() {
        viewModel = createAddViewModel()

        viewModel.updateType(CalendarEventType.DAYSERVICE)

        assertTrue(viewModel.isDirty)
    }
}
