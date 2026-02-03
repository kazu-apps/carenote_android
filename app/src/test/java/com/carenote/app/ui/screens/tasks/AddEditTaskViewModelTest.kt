package com.carenote.app.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
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

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTaskRepository
    private lateinit var viewModel: AddEditTaskViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createAddViewModel(): AddEditTaskViewModel {
        return AddEditTaskViewModel(SavedStateHandle(), repository)
    }

    private fun createEditViewModel(taskId: Long): AddEditTaskViewModel {
        return AddEditTaskViewModel(
            SavedStateHandle(mapOf("taskId" to taskId)),
            repository
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
