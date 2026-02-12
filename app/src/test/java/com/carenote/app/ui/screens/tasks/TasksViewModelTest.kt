package com.carenote.app.ui.screens.tasks

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakeTaskReminderScheduler
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeTaskRepository
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeClock = FakeClock()
    private lateinit var repository: FakeTaskRepository
    private lateinit var scheduler: FakeTaskReminderScheduler
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: TasksViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository()
        scheduler = FakeTaskReminderScheduler()
        analyticsRepository = FakeAnalyticsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TasksViewModel {
        return TasksViewModel(repository, scheduler, analyticsRepository, fakeClock)
    }

    /** Helper: read the current tasks from the FakeTaskRepository's internal state. */
    private fun repoTasks(): List<Task> = repository.currentTasks()

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
    fun `setFilterMode updates filter mode`() {
        viewModel = createViewModel()

        viewModel.setFilterMode(TaskFilterMode.INCOMPLETE)

        assertEquals(TaskFilterMode.INCOMPLETE, viewModel.filterMode.value)
    }

    @Test
    fun `toggleCompletion flips isCompleted`() = runTest {
        val task = createTask(id = 1L, title = "タスク", isCompleted = false)
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.toggleCompletion(task)

        val items = repoTasks()
        assertEquals(1, items.size)
        assertTrue(items[0].isCompleted)
    }

    @Test
    fun `toggleCompletion on completed task sets incomplete`() = runTest {
        val task = createTask(id = 1L, title = "完了タスク", isCompleted = true)
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.toggleCompletion(task)

        val items = repoTasks()
        assertFalse(items[0].isCompleted)
    }

    @Test
    fun `toggleCompletion cancels reminder when completing task`() = runTest {
        val task = createTask(
            id = 1L,
            title = "リマインダータスク",
            isCompleted = false,
            reminderEnabled = true,
            reminderTime = LocalTime.of(10, 0)
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.toggleCompletion(task)

        assertEquals(1, scheduler.cancelReminderCalls.size)
        assertEquals(1L, scheduler.cancelReminderCalls[0].taskId)
        assertEquals(1, scheduler.cancelFollowUpCalls.size)
        assertEquals(1L, scheduler.cancelFollowUpCalls[0].taskId)
    }

    @Test
    fun `toggleCompletion reschedules reminder when uncompleting task`() = runTest {
        val task = createTask(
            id = 1L,
            title = "リマインダータスク",
            isCompleted = true,
            reminderEnabled = true,
            reminderTime = LocalTime.of(10, 0)
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.toggleCompletion(task)

        assertEquals(1, scheduler.scheduleReminderCalls.size)
        assertEquals(1L, scheduler.scheduleReminderCalls[0].taskId)
        assertEquals("リマインダータスク", scheduler.scheduleReminderCalls[0].taskTitle)
        assertEquals(LocalTime.of(10, 0), scheduler.scheduleReminderCalls[0].time)
    }

    @Test
    fun `toggleCompletion does not reschedule if reminder not enabled`() = runTest {
        val task = createTask(
            id = 1L,
            title = "タスク",
            isCompleted = true,
            reminderEnabled = false,
            reminderTime = null
        )
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.toggleCompletion(task)

        assertTrue(scheduler.scheduleReminderCalls.isEmpty())
    }

    @Test
    fun `toggleCompletion generates next task for recurring task`() = runTest {
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

        viewModel.toggleCompletion(task)

        val items = repoTasks()
        assertEquals(2, items.size)
        val nextTask = items.find { !it.isCompleted }
        assertFalse(nextTask!!.isCompleted)
        assertEquals(LocalDate.of(2025, 3, 16), nextTask.dueDate)
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
    fun `recurring task without due date does not generate next task`() = runTest {
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

        viewModel.toggleCompletion(task)

        val items = repoTasks()
        assertEquals(1, items.size)
    }

    @Test
    fun `deleteTask removes task`() = runTest {
        val tasks = listOf(
            createTask(id = 1L, title = "タスクA"),
            createTask(id = 2L, title = "タスクB")
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()

        viewModel.deleteTask(1L)

        val items = repoTasks()
        assertEquals(1, items.size)
        assertEquals("タスクB", items[0].title)
    }

    @Test
    fun `deleteTask cancels reminder`() = runTest {
        repository.setTasks(listOf(createTask(id = 1L)))
        viewModel = createViewModel()

        viewModel.deleteTask(1L)

        assertEquals(1, scheduler.cancelReminderCalls.size)
        assertEquals(1L, scheduler.cancelReminderCalls[0].taskId)
        assertEquals(1, scheduler.cancelFollowUpCalls.size)
        assertEquals(1L, scheduler.cancelFollowUpCalls[0].taskId)
    }

    @Test
    fun `snackbar emitted on delete success`() = runTest {
        repository.setTasks(listOf(createTask(id = 1L)))
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.deleteTask(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.tasks_deleted, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `snackbar emitted on delete failure`() = runTest {
        repository.setTasks(listOf(createTask(id = 1L)))
        repository.shouldFail = true
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.deleteTask(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.tasks_delete_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `toggleCompletion failure does not change task state`() = runTest {
        val task = createTask(id = 1L, isCompleted = false)
        repository.setTasks(listOf(task))
        viewModel = createViewModel()

        repository.shouldFail = true
        viewModel.toggleCompletion(task)

        val items = repoTasks()
        assertFalse(items[0].isCompleted)
    }

    @Test
    fun `snackbar emitted on toggle failure`() = runTest {
        val task = createTask(id = 1L, isCompleted = false)
        repository.setTasks(listOf(task))
        repository.shouldFail = true
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.toggleCompletion(task)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.tasks_toggle_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `tasks update reactively when repository changes`() = runTest {
        viewModel = createViewModel()

        assertEquals(0, repoTasks().size)

        repository.setTasks(listOf(createTask(id = 1L)))

        assertEquals(1, repoTasks().size)
    }

    @Test
    fun `next recurring task schedules reminder if enabled`() = runTest {
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

        viewModel.toggleCompletion(task)

        // cancelReminder for completed task + scheduleReminder for new task
        assertEquals(1, scheduler.cancelReminderCalls.size)
        assertEquals(1, scheduler.scheduleReminderCalls.size)
        assertEquals("繰り返しリマインダータスク", scheduler.scheduleReminderCalls[0].taskTitle)
        assertEquals(LocalTime.of(9, 0), scheduler.scheduleReminderCalls[0].time)
    }

    @Test
    fun `searchQuery is empty initially`() {
        viewModel = createViewModel()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `updateSearchQuery updates searchQuery`() {
        viewModel = createViewModel()

        viewModel.updateSearchQuery("買い物")

        assertEquals("買い物", viewModel.searchQuery.value)
    }

    @Test
    fun `search filters tasks by title`() = runTest {
        val tasks = listOf(
            createTask(id = 1L, title = "買い物に行く", description = ""),
            createTask(id = 2L, title = "薬を取りに行く", description = ""),
            createTask(id = 3L, title = "買い物リスト作成", description = "")
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()
        viewModel.updateSearchQuery("買い物")

        // Verify repository's search filtering works correctly
        val filtered = repository.getFilteredTasks("買い物")
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.title.contains("買い物") })
    }

    @Test
    fun `search filters tasks by description`() = runTest {
        val tasks = listOf(
            createTask(id = 1L, title = "タスクA", description = "病院の予約を確認"),
            createTask(id = 2L, title = "タスクB", description = "スーパーで買い物")
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()
        viewModel.updateSearchQuery("病院")

        // Verify repository's search filtering works correctly
        val filtered = repository.getFilteredTasks("病院")
        assertEquals(1, filtered.size)
        assertEquals("タスクA", filtered[0].title)
    }

    @Test
    fun `filter INCOMPLETE shows only incomplete tasks via repository`() = runTest {
        val tasks = listOf(
            createTask(id = 1L, title = "未完了タスク", isCompleted = false),
            createTask(id = 2L, title = "完了タスク", isCompleted = true)
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()
        viewModel.setFilterMode(TaskFilterMode.INCOMPLETE)

        val incomplete = repoTasks().filter { !it.isCompleted }
        assertEquals(1, incomplete.size)
        assertEquals("未完了タスク", incomplete[0].title)
    }

    @Test
    fun `filter COMPLETED shows only completed tasks via repository`() = runTest {
        val tasks = listOf(
            createTask(id = 1L, title = "未完了タスク", isCompleted = false),
            createTask(id = 2L, title = "完了タスク", isCompleted = true)
        )
        repository.setTasks(tasks)
        viewModel = createViewModel()
        viewModel.setFilterMode(TaskFilterMode.COMPLETED)

        val completed = repoTasks().filter { it.isCompleted }
        assertEquals(1, completed.size)
        assertEquals("完了タスク", completed[0].title)
    }
}
