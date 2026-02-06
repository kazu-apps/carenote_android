package com.carenote.app.ui.screens.tasks

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.fakes.FakeTaskReminderScheduler
import com.carenote.app.fakes.FakeTaskRepository
import com.carenote.app.ui.util.SnackbarEvent
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTaskRepository
    private lateinit var scheduler: FakeTaskReminderScheduler
    private lateinit var viewModel: TasksViewModel

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

    private fun createViewModel(): TasksViewModel {
        return TasksViewModel(repository, scheduler)
    }

    private fun createTask(
        id: Long = 1L,
        title: String = "テストタスク",
        description: String = "テスト説明",
        dueDate: LocalDate? = null,
        isCompleted: Boolean = false,
        priority: TaskPriority = TaskPriority.MEDIUM,
        recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
        recurrenceInterval: Int = 1,
        reminderEnabled: Boolean = false,
        reminderTime: LocalTime? = null,
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ) = Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        isCompleted = isCompleted,
        priority = priority,
        recurrenceFrequency = recurrenceFrequency,
        recurrenceInterval = recurrenceInterval,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    @Test
    fun `initial filter mode is ALL`() {
        viewModel = createViewModel()

        assertEquals(TaskFilterMode.ALL, viewModel.filterMode.value)
    }

    @Test
    fun `initial tasks state is Loading`() {
        viewModel = createViewModel()

        assertTrue(viewModel.tasks.value is UiState.Loading)
    }

    @Test
    fun `tasks transitions from Loading to Success`() = runTest(testDispatcher) {
        val tasks = listOf(createTask(id = 1L, title = "タスクA"))
        repository.setTasks(tasks)
        viewModel = createViewModel()

        viewModel.tasks.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(1, (success as UiState.Success).data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tasks loaded as Success`() = runTest(testDispatcher) {
        val tasks = listOf(
            createTask(id = 1L, title = "タスクA"),
            createTask(id = 2L, title = "タスクB")
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(2, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `empty tasks shows Success with empty list`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(0, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `setFilterMode updates filter mode`() {
        viewModel = createViewModel()

        viewModel.setFilterMode(TaskFilterMode.INCOMPLETE)

        assertEquals(TaskFilterMode.INCOMPLETE, viewModel.filterMode.value)
    }

    @Test
    fun `filter INCOMPLETE shows only incomplete tasks`() = runTest(testDispatcher) {
        val tasks = listOf(
            createTask(id = 1L, title = "未完了タスク", isCompleted = false),
            createTask(id = 2L, title = "完了タスク", isCompleted = true)
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()
        viewModel.setFilterMode(TaskFilterMode.INCOMPLETE)

        viewModel.tasks.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("未完了タスク", data[0].title)
            assertFalse(data[0].isCompleted)
        }
    }

    @Test
    fun `filter COMPLETED shows only completed tasks`() = runTest(testDispatcher) {
        val tasks = listOf(
            createTask(id = 1L, title = "未完了タスク", isCompleted = false),
            createTask(id = 2L, title = "完了タスク", isCompleted = true)
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()
        viewModel.setFilterMode(TaskFilterMode.COMPLETED)

        viewModel.tasks.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("完了タスク", data[0].title)
            assertTrue(data[0].isCompleted)
        }
    }

    @Test
    fun `filter ALL shows all tasks`() = runTest(testDispatcher) {
        val tasks = listOf(
            createTask(id = 1L, title = "未完了タスク", isCompleted = false),
            createTask(id = 2L, title = "完了タスク", isCompleted = true)
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()
        viewModel.setFilterMode(TaskFilterMode.ALL)

        viewModel.tasks.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(2, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `filter mode change updates task list`() = runTest(testDispatcher) {
        val tasks = listOf(
            createTask(id = 1L, title = "未完了タスク", isCompleted = false),
            createTask(id = 2L, title = "完了タスク", isCompleted = true)
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()
            val allState = expectMostRecentItem()
            assertTrue(allState is UiState.Success)
            assertEquals(2, (allState as UiState.Success).data.size)

            viewModel.setFilterMode(TaskFilterMode.INCOMPLETE)
            advanceUntilIdle()
            val incompleteState = expectMostRecentItem()
            assertTrue(incompleteState is UiState.Success)
            assertEquals(1, (incompleteState as UiState.Success).data.size)
        }
    }

    @Test
    fun `toggleCompletion flips isCompleted`() = runTest(testDispatcher) {
        val task = createTask(id = 1L, title = "タスク", isCompleted = false)
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()

            viewModel.toggleCompletion(task)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertTrue(data[0].isCompleted)
        }
    }

    @Test
    fun `toggleCompletion on completed task sets incomplete`() = runTest(testDispatcher) {
        val task = createTask(id = 1L, title = "完了タスク", isCompleted = true)
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()

            viewModel.toggleCompletion(task)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertFalse(data[0].isCompleted)
        }
    }

    @Test
    fun `toggleCompletion cancels reminder when completing task`() = runTest(testDispatcher) {
        val task = createTask(
            id = 1L,
            title = "リマインダータスク",
            isCompleted = false,
            reminderEnabled = true,
            reminderTime = LocalTime.of(10, 0)
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCompletion(task)
        advanceUntilIdle()

        assertEquals(1, scheduler.cancelReminderCalls.size)
        assertEquals(1L, scheduler.cancelReminderCalls[0].taskId)
        assertEquals(1, scheduler.cancelFollowUpCalls.size)
        assertEquals(1L, scheduler.cancelFollowUpCalls[0].taskId)
    }

    @Test
    fun `toggleCompletion reschedules reminder when uncompleting task`() = runTest(testDispatcher) {
        val task = createTask(
            id = 1L,
            title = "リマインダータスク",
            isCompleted = true,
            reminderEnabled = true,
            reminderTime = LocalTime.of(10, 0)
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCompletion(task)
        advanceUntilIdle()

        assertEquals(1, scheduler.scheduleReminderCalls.size)
        assertEquals(1L, scheduler.scheduleReminderCalls[0].taskId)
        assertEquals("リマインダータスク", scheduler.scheduleReminderCalls[0].taskTitle)
        assertEquals(LocalTime.of(10, 0), scheduler.scheduleReminderCalls[0].time)
    }

    @Test
    fun `toggleCompletion does not reschedule if reminder not enabled`() = runTest(testDispatcher) {
        val task = createTask(
            id = 1L,
            title = "タスク",
            isCompleted = true,
            reminderEnabled = false,
            reminderTime = null
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCompletion(task)
        advanceUntilIdle()

        assertTrue(scheduler.scheduleReminderCalls.isEmpty())
    }

    @Test
    fun `toggleCompletion generates next task for recurring task`() = runTest(testDispatcher) {
        val task = createTask(
            id = 1L,
            title = "繰り返しタスク",
            isCompleted = false,
            dueDate = LocalDate.of(2025, 3, 15),
            recurrenceFrequency = RecurrenceFrequency.DAILY,
            recurrenceInterval = 1
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()

            viewModel.toggleCompletion(task)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(2, data.size)
            val nextTask = data.find { !it.isCompleted }
            assertFalse(nextTask!!.isCompleted)
            assertEquals(LocalDate.of(2025, 3, 16), nextTask.dueDate)
        }
    }

    @Test
    fun `next task has correct due date for daily recurrence`() {
        val result = TasksViewModel.calculateNextDueDate(
            LocalDate.of(2025, 3, 15),
            RecurrenceFrequency.DAILY,
            3
        )
        assertEquals(LocalDate.of(2025, 3, 18), result)
    }

    @Test
    fun `next task has correct due date for weekly recurrence`() {
        val result = TasksViewModel.calculateNextDueDate(
            LocalDate.of(2025, 3, 15),
            RecurrenceFrequency.WEEKLY,
            2
        )
        assertEquals(LocalDate.of(2025, 3, 29), result)
    }

    @Test
    fun `next task has correct due date for monthly recurrence`() {
        val result = TasksViewModel.calculateNextDueDate(
            LocalDate.of(2025, 1, 31),
            RecurrenceFrequency.MONTHLY,
            1
        )
        assertEquals(LocalDate.of(2025, 2, 28), result)
    }

    @Test
    fun `recurring task without due date does not generate next task`() = runTest(testDispatcher) {
        val task = createTask(
            id = 1L,
            title = "繰り返しタスク",
            isCompleted = false,
            dueDate = null,
            recurrenceFrequency = RecurrenceFrequency.DAILY,
            recurrenceInterval = 1
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()

            viewModel.toggleCompletion(task)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
        }
    }

    @Test
    fun `deleteTask removes task`() = runTest(testDispatcher) {
        val tasks = listOf(
            createTask(id = 1L, title = "タスクA"),
            createTask(id = 2L, title = "タスクB")
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()

            viewModel.deleteTask(1L)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("タスクB", data[0].title)
        }
    }

    @Test
    fun `deleteTask cancels reminder`() = runTest(testDispatcher) {
        repository.setTasks(listOf(createTask(id = 1L)))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteTask(1L)
        advanceUntilIdle()

        assertEquals(1, scheduler.cancelReminderCalls.size)
        assertEquals(1L, scheduler.cancelReminderCalls[0].taskId)
        assertEquals(1, scheduler.cancelFollowUpCalls.size)
        assertEquals(1L, scheduler.cancelFollowUpCalls[0].taskId)
    }

    @Test
    fun `snackbar emitted on delete success`() = runTest(testDispatcher) {
        repository.setTasks(listOf(createTask(id = 1L)))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteTask(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.tasks_deleted, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `snackbar emitted on delete failure`() = runTest(testDispatcher) {
        repository.setTasks(listOf(createTask(id = 1L)))
        repository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteTask(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.tasks_delete_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `toggleCompletion failure does not change task state`() = runTest(testDispatcher) {
        val task = createTask(id = 1L, isCompleted = false)
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()

            repository.shouldFail = true
            viewModel.toggleCompletion(task)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertFalse((state as UiState.Success).data[0].isCompleted)
        }
    }

    @Test
    fun `snackbar emitted on toggle failure`() = runTest(testDispatcher) {
        val task = createTask(id = 1L, isCompleted = false)
        repository.setTasks(listOf(task))
        repository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.toggleCompletion(task)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.tasks_toggle_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `tasks update reactively when repository changes`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.tasks.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(0, (initial as UiState.Success).data.size)

            repository.setTasks(listOf(createTask(id = 1L)))
            advanceUntilIdle()
            val updated = expectMostRecentItem()
            assertTrue(updated is UiState.Success)
            assertEquals(1, (updated as UiState.Success).data.size)
        }
    }

    @Test
    fun `next recurring task schedules reminder if enabled`() = runTest(testDispatcher) {
        val task = createTask(
            id = 1L,
            title = "繰り返しリマインダータスク",
            isCompleted = false,
            dueDate = LocalDate.of(2025, 3, 15),
            recurrenceFrequency = RecurrenceFrequency.WEEKLY,
            recurrenceInterval = 1,
            reminderEnabled = true,
            reminderTime = LocalTime.of(9, 0)
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCompletion(task)
        advanceUntilIdle()

        // cancelReminder for completed task + scheduleReminder for new task
        assertEquals(1, scheduler.cancelReminderCalls.size)
        assertEquals(1, scheduler.scheduleReminderCalls.size)
        assertEquals("繰り返しリマインダータスク", scheduler.scheduleReminderCalls[0].taskTitle)
        assertEquals(LocalTime.of(9, 0), scheduler.scheduleReminderCalls[0].time)
    }
}
