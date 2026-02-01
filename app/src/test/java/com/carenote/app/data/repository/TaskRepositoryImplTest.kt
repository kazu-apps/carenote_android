package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.local.entity.TaskEntity
import com.carenote.app.data.mapper.TaskMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TaskRepositoryImplTest {

    private lateinit var dao: TaskDao
    private lateinit var mapper: TaskMapper
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = TaskMapper()
        repository = TaskRepositoryImpl(dao, mapper)
    }

    private fun createEntity(
        id: Long = 1L,
        title: String = "テストタスク",
        description: String = "テスト説明",
        dueDate: String? = "2025-04-10",
        isCompleted: Int = 0,
        priority: String = "MEDIUM"
    ) = TaskEntity(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        isCompleted = isCompleted,
        priority = priority,
        createdAt = "2025-03-15T10:00:00",
        updatedAt = "2025-03-15T10:00:00"
    )

    @Test
    fun `getAllTasks returns flow of tasks`() = runTest {
        val entities = listOf(
            createEntity(1L, title = "タスクA"),
            createEntity(2L, title = "タスクB")
        )
        every { dao.getAllTasks() } returns flowOf(entities)

        val result = repository.getAllTasks().first()

        assertEquals(2, result.size)
        assertEquals("タスクA", result[0].title)
        assertEquals("タスクB", result[1].title)
    }

    @Test
    fun `getAllTasks returns empty list when no tasks`() = runTest {
        every { dao.getAllTasks() } returns flowOf(emptyList())

        val result = repository.getAllTasks().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTaskById returns task when found`() = runTest {
        val entity = createEntity(1L, title = "薬を買う")
        every { dao.getTaskById(1L) } returns flowOf(entity)

        val result = repository.getTaskById(1L).first()

        assertEquals("薬を買う", result?.title)
    }

    @Test
    fun `getTaskById returns null when not found`() = runTest {
        every { dao.getTaskById(999L) } returns flowOf(null)

        val result = repository.getTaskById(999L).first()

        assertNull(result)
    }

    @Test
    fun `getIncompleteTasks returns incomplete tasks`() = runTest {
        val entities = listOf(
            createEntity(1L, title = "未完了タスク", isCompleted = 0),
            createEntity(2L, title = "別の未完了タスク", isCompleted = 0)
        )
        every { dao.getIncompleteTasks() } returns flowOf(entities)

        val result = repository.getIncompleteTasks().first()

        assertEquals(2, result.size)
        result.forEach { assertFalse(it.isCompleted) }
    }

    @Test
    fun `getTasksByDueDate returns tasks for specific date`() = runTest {
        val entities = listOf(
            createEntity(1L, title = "今日のタスク", dueDate = "2025-04-10"),
            createEntity(2L, title = "今日の別タスク", dueDate = "2025-04-10")
        )
        every { dao.getTasksByDueDate("2025-04-10") } returns flowOf(entities)

        val result = repository.getTasksByDueDate(LocalDate.of(2025, 4, 10)).first()

        assertEquals(2, result.size)
        assertEquals("今日のタスク", result[0].title)
        assertEquals("今日の別タスク", result[1].title)
    }

    @Test
    fun `insertTask returns Success with id`() = runTest {
        coEvery { dao.insertTask(any()) } returns 1L

        val task = Task(
            title = "テストタスク",
            dueDate = LocalDate.of(2025, 4, 10),
            priority = TaskPriority.HIGH,
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.insertTask(task)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).value)
    }

    @Test
    fun `insertTask returns Failure on db error`() = runTest {
        coEvery { dao.insertTask(any()) } throws RuntimeException("DB error")

        val task = Task(
            title = "テストタスク",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.insertTask(task)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `updateTask returns Success`() = runTest {
        coEvery { dao.updateTask(any()) } returns Unit

        val task = Task(
            id = 1L,
            title = "更新タスク",
            isCompleted = true,
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 11, 0)
        )
        val result = repository.updateTask(task)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateTask returns Failure on db error`() = runTest {
        coEvery { dao.updateTask(any()) } throws RuntimeException("DB error")

        val task = Task(
            id = 1L,
            title = "更新タスク",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 11, 0)
        )
        val result = repository.updateTask(task)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `deleteTask returns Success`() = runTest {
        coEvery { dao.deleteTask(1L) } returns Unit

        val result = repository.deleteTask(1L)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteTask(1L) }
    }

    @Test
    fun `deleteTask returns Failure on db error`() = runTest {
        coEvery { dao.deleteTask(1L) } throws RuntimeException("DB error")

        val result = repository.deleteTask(1L)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    private fun assertFalse(value: Boolean) {
        org.junit.Assert.assertFalse(value)
    }
}
