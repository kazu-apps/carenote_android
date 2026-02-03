package com.carenote.app.ui.screens.tasks

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
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

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTaskRepository
    private lateinit var viewModel: TasksViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TasksViewModel {
        return TasksViewModel(repository)
    }

    private fun createTask(
        id: Long = 1L,
        title: String = "テストタスク",
        description: String = "テスト説明",
        dueDate: LocalDate? = null,
        isCompleted: Boolean = false,
        priority: TaskPriority = TaskPriority.MEDIUM,
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ) = Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        isCompleted = isCompleted,
        priority = priority,
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
}
