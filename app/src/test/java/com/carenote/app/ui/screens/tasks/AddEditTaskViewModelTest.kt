package com.carenote.app.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.fakes.FakeTaskReminderScheduler
import com.carenote.app.fakes.FakeTaskRepository
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
class AddEditTaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTaskRepository
    private lateinit var scheduler: FakeTaskReminderScheduler
    private lateinit var viewModel: AddEditTaskViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository()
        scheduler = FakeTaskReminderScheduler()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createAddViewModel(): AddEditTaskViewModel {
        return AddEditTaskViewModel(SavedStateHandle(), repository, scheduler)
    }

    private fun createEditViewModel(taskId: Long): AddEditTaskViewModel {
        return AddEditTaskViewModel(
            SavedStateHandle(mapOf("taskId" to taskId)),
            repository,
            scheduler
        )
    }

    @Test
    fun `initial form state has defaults for add mode`() {
        viewModel = createAddViewModel()

        val state = viewModel.formState.value

        assertEquals("", state.title)
        assertEquals("", state.description)
        assertNull(state.dueDate)
        assertEquals(TaskPriority.MEDIUM, state.priority)
        assertNull(state.titleError)
        assertFalse(state.isSaving)
        assertFalse(state.isEditMode)
    }

    @Test
    fun `initial form state has default recurrence and reminder values`() {
        viewModel = createAddViewModel()

        val state = viewModel.formState.value

        assertEquals(RecurrenceFrequency.NONE, state.recurrenceFrequency)
        assertEquals(AppConfig.Task.DEFAULT_RECURRENCE_INTERVAL, state.recurrenceInterval)
        assertFalse(state.reminderEnabled)
        assertNull(state.reminderTime)
        assertNull(state.recurrenceIntervalError)
    }

    @Test
    fun `isEditMode is false in add mode`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.formState.value.isEditMode)
    }

    @Test
    fun `isEditMode is true when taskId provided`() {
        repository.setTasks(
            listOf(
                Task(
                    id = 1L,
                    title = "テスト",
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)

        assertTrue(viewModel.formState.value.isEditMode)
    }

    @Test
    fun `edit mode loads existing task data`() = runTest {
        repository.setTasks(
            listOf(
                Task(
                    id = 1L,
                    title = "既存タスク",
                    description = "既存説明",
                    dueDate = LocalDate.of(2025, 3, 20),
                    priority = TaskPriority.HIGH,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals("既存タスク", state.title)
        assertEquals("既存説明", state.description)
        assertEquals(LocalDate.of(2025, 3, 20), state.dueDate)
        assertEquals(TaskPriority.HIGH, state.priority)
    }

    @Test
    fun `edit mode loads existing recurrence and reminder data`() = runTest {
        repository.setTasks(
            listOf(
                Task(
                    id = 1L,
                    title = "繰り返しタスク",
                    recurrenceFrequency = RecurrenceFrequency.WEEKLY,
                    recurrenceInterval = 2,
                    reminderEnabled = true,
                    reminderTime = LocalTime.of(14, 30),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals(RecurrenceFrequency.WEEKLY, state.recurrenceFrequency)
        assertEquals(2, state.recurrenceInterval)
        assertTrue(state.reminderEnabled)
        assertEquals(LocalTime.of(14, 30), state.reminderTime)
    }

    @Test
    fun `edit mode with nonexistent taskId keeps empty form`() = runTest {
        viewModel = createEditViewModel(999L)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertTrue(state.isEditMode)
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertNull(state.dueDate)
        assertEquals(TaskPriority.MEDIUM, state.priority)
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
        viewModel.saveTask()
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
    fun `updateDueDate updates form state`() {
        viewModel = createAddViewModel()
        val newDate = LocalDate.of(2025, 6, 1)

        viewModel.updateDueDate(newDate)

        assertEquals(newDate, viewModel.formState.value.dueDate)
    }

    @Test
    fun `updateDueDate with null clears due date`() {
        viewModel = createAddViewModel()
        viewModel.updateDueDate(LocalDate.of(2025, 6, 1))

        viewModel.updateDueDate(null)

        assertNull(viewModel.formState.value.dueDate)
    }

    @Test
    fun `updatePriority updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updatePriority(TaskPriority.HIGH)

        assertEquals(TaskPriority.HIGH, viewModel.formState.value.priority)
    }

    @Test
    fun `updatePriority to LOW`() {
        viewModel = createAddViewModel()

        viewModel.updatePriority(TaskPriority.LOW)

        assertEquals(TaskPriority.LOW, viewModel.formState.value.priority)
    }

    @Test
    fun `updateRecurrenceFrequency updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateRecurrenceFrequency(RecurrenceFrequency.DAILY)

        assertEquals(RecurrenceFrequency.DAILY, viewModel.formState.value.recurrenceFrequency)
    }

    @Test
    fun `updateRecurrenceFrequency clears interval error`() {
        viewModel = createAddViewModel()
        viewModel.updateRecurrenceFrequency(RecurrenceFrequency.DAILY)
        viewModel.updateRecurrenceInterval(0)
        viewModel.updateTitle("タスク")
        viewModel.saveTask()
        assertNotNull(viewModel.formState.value.recurrenceIntervalError)

        viewModel.updateRecurrenceFrequency(RecurrenceFrequency.WEEKLY)

        assertNull(viewModel.formState.value.recurrenceIntervalError)
    }

    @Test
    fun `updateRecurrenceInterval updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateRecurrenceInterval(5)

        assertEquals(5, viewModel.formState.value.recurrenceInterval)
    }

    @Test
    fun `updateRecurrenceInterval clears interval error`() {
        viewModel = createAddViewModel()
        viewModel.updateRecurrenceFrequency(RecurrenceFrequency.DAILY)
        viewModel.updateRecurrenceInterval(0)
        viewModel.updateTitle("タスク")
        viewModel.saveTask()
        assertNotNull(viewModel.formState.value.recurrenceIntervalError)

        viewModel.updateRecurrenceInterval(3)

        assertNull(viewModel.formState.value.recurrenceIntervalError)
    }

    @Test
    fun `toggleReminder updates form state`() {
        viewModel = createAddViewModel()
        assertFalse(viewModel.formState.value.reminderEnabled)

        viewModel.toggleReminder()

        assertTrue(viewModel.formState.value.reminderEnabled)
    }

    @Test
    fun `toggleReminder twice reverts to false`() {
        viewModel = createAddViewModel()

        viewModel.toggleReminder()
        viewModel.toggleReminder()

        assertFalse(viewModel.formState.value.reminderEnabled)
    }

    @Test
    fun `updateReminderTime updates form state`() {
        viewModel = createAddViewModel()
        val time = LocalTime.of(8, 30)

        viewModel.updateReminderTime(time)

        assertEquals(time, viewModel.formState.value.reminderTime)
    }

    @Test
    fun `saveTask with empty title sets title error`() {
        viewModel = createAddViewModel()

        viewModel.saveTask()

        assertNotNull(viewModel.formState.value.titleError)
        assertEquals(
            UiText.Resource(R.string.tasks_task_title_required),
            viewModel.formState.value.titleError
        )
    }

    @Test
    fun `saveTask with blank title sets title error`() {
        viewModel = createAddViewModel()
        viewModel.updateTitle("   ")

        viewModel.saveTask()

        assertNotNull(viewModel.formState.value.titleError)
    }

    @Test
    fun `saveTask validates recurrence interval range`() {
        viewModel = createAddViewModel()
        viewModel.updateTitle("タスク")
        viewModel.updateRecurrenceFrequency(RecurrenceFrequency.DAILY)
        viewModel.updateRecurrenceInterval(0)

        viewModel.saveTask()

        assertNotNull(viewModel.formState.value.recurrenceIntervalError)
        assertEquals(
            UiText.Resource(R.string.tasks_recurrence_interval_error),
            viewModel.formState.value.recurrenceIntervalError
        )
    }

    @Test
    fun `saveTask with invalid interval over max shows error`() {
        viewModel = createAddViewModel()
        viewModel.updateTitle("タスク")
        viewModel.updateRecurrenceFrequency(RecurrenceFrequency.WEEKLY)
        viewModel.updateRecurrenceInterval(100)

        viewModel.saveTask()

        assertNotNull(viewModel.formState.value.recurrenceIntervalError)
    }

    @Test
    fun `saveTask skips interval validation when frequency is NONE`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("タスク")
        viewModel.updateRecurrenceFrequency(RecurrenceFrequency.NONE)
        viewModel.updateRecurrenceInterval(0)

        viewModel.saveTask()
        advanceUntilIdle()

        assertNull(viewModel.formState.value.recurrenceIntervalError)
    }

    @Test
    fun `saveTask with valid data succeeds in add mode`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("テストタスク")

        viewModel.saveTask()
        advanceUntilIdle()

        viewModel.savedEvent.test {
            val saved = awaitItem()
            assertTrue(saved)
        }
    }

    @Test
    fun `saveTask inserts task to repository in add mode`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("テストタスク")
        viewModel.updateDescription("テスト説明")

        viewModel.saveTask()
        advanceUntilIdle()

        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("テストタスク", tasks[0].title)
            assertEquals("テスト説明", tasks[0].description)
        }
    }

    @Test
    fun `saveTask trims title and description`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("  テスト  ")
        viewModel.updateDescription("  説明  ")

        viewModel.saveTask()
        advanceUntilIdle()

        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals("テスト", tasks[0].title)
            assertEquals("説明", tasks[0].description)
        }
    }

    @Test
    fun `saveTask updates existing task in edit mode`() = runTest {
        repository.setTasks(
            listOf(
                Task(
                    id = 1L,
                    title = "旧タイトル",
                    description = "旧説明",
                    dueDate = LocalDate.of(2025, 3, 15),
                    priority = TaskPriority.MEDIUM,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTitle("新タイトル")
        viewModel.updateDescription("新説明")
        viewModel.saveTask()
        advanceUntilIdle()

        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("新タイトル", tasks[0].title)
            assertEquals("新説明", tasks[0].description)
        }
    }

    @Test
    fun `saveTask preserves priority in edit mode`() = runTest {
        repository.setTasks(
            listOf(
                Task(
                    id = 1L,
                    title = "タスク",
                    priority = TaskPriority.HIGH,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updatePriority(TaskPriority.LOW)
        viewModel.saveTask()
        advanceUntilIdle()

        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(TaskPriority.LOW, tasks[0].priority)
        }
    }

    @Test
    fun `saveTask schedules reminder when enabled`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("リマインダータスク")
        viewModel.toggleReminder()
        viewModel.updateReminderTime(LocalTime.of(10, 0))

        viewModel.saveTask()
        advanceUntilIdle()

        assertEquals(1, scheduler.scheduleReminderCalls.size)
        assertEquals("リマインダータスク", scheduler.scheduleReminderCalls[0].taskTitle)
        assertEquals(LocalTime.of(10, 0), scheduler.scheduleReminderCalls[0].time)
    }

    @Test
    fun `saveTask cancels reminder when disabled`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("タスク")

        viewModel.saveTask()
        advanceUntilIdle()

        assertEquals(1, scheduler.cancelReminderCalls.size)
        assertTrue(scheduler.scheduleReminderCalls.isEmpty())
    }

    @Test
    fun `saveTask in edit mode cancels then reschedules reminder`() = runTest {
        repository.setTasks(
            listOf(
                Task(
                    id = 1L,
                    title = "タスク",
                    reminderEnabled = true,
                    reminderTime = LocalTime.of(8, 0),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateReminderTime(LocalTime.of(9, 0))
        viewModel.saveTask()
        advanceUntilIdle()

        assertEquals(1, scheduler.cancelReminderCalls.size)
        assertEquals(1L, scheduler.cancelReminderCalls[0].taskId)
        assertEquals(1, scheduler.scheduleReminderCalls.size)
        assertEquals(LocalTime.of(9, 0), scheduler.scheduleReminderCalls[0].time)
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

        viewModel.saveTask()
        advanceUntilIdle()

        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `default priority is MEDIUM`() {
        viewModel = createAddViewModel()

        assertEquals(TaskPriority.MEDIUM, viewModel.formState.value.priority)
    }
}
