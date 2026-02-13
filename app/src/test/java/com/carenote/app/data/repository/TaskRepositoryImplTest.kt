package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.local.entity.TaskEntity
import com.carenote.app.data.mapper.TaskMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.domain.model.User
import com.carenote.app.domain.repository.AuthRepository
import app.cash.turbine.test
import com.carenote.app.fakes.FakeActiveCareRecipientProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
    private lateinit var activeRecipientProvider: FakeActiveCareRecipientProvider
    private lateinit var authRepository: AuthRepository
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = TaskMapper()
        activeRecipientProvider = FakeActiveCareRecipientProvider()
        authRepository = mockk()
        every { authRepository.getCurrentUser() } returns User(
            uid = "test-uid",
            name = "Test User",
            email = "test@example.com",
            createdAt = LocalDateTime.of(2025, 1, 1, 0, 0)
        )
        repository = TaskRepositoryImpl(dao, mapper, activeRecipientProvider, authRepository)
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
        every { dao.getAllTasks(1L) } returns flowOf(entities)

        repository.getAllTasks().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("タスクA", result[0].title)
            assertEquals("タスクB", result[1].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllTasks returns empty list when no tasks`() = runTest {
        every { dao.getAllTasks(1L) } returns flowOf(emptyList())

        repository.getAllTasks().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTaskById returns task when found`() = runTest {
        val entity = createEntity(1L, title = "薬を買う")
        every { dao.getTaskById(1L) } returns flowOf(entity)

        repository.getTaskById(1L).test {
            val result = awaitItem()
            assertEquals("薬を買う", result?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTaskById returns null when not found`() = runTest {
        every { dao.getTaskById(999L) } returns flowOf(null)

        repository.getTaskById(999L).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getIncompleteTasks returns incomplete tasks`() = runTest {
        val entities = listOf(
            createEntity(1L, title = "未完了タスク", isCompleted = 0),
            createEntity(2L, title = "別の未完了タスク", isCompleted = 0)
        )
        every { dao.getIncompleteTasks(1L) } returns flowOf(entities)

        repository.getIncompleteTasks().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            result.forEach { assertFalse(it.isCompleted) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTasksByDueDate returns tasks for specific date`() = runTest {
        val entities = listOf(
            createEntity(1L, title = "今日のタスク", dueDate = "2025-04-10"),
            createEntity(2L, title = "今日の別タスク", dueDate = "2025-04-10")
        )
        every { dao.getTasksByDueDate("2025-04-10", 1L) } returns flowOf(entities)

        repository.getTasksByDueDate(LocalDate.of(2025, 4, 10)).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("今日のタスク", result[0].title)
            assertEquals("今日の別タスク", result[1].title)
            cancelAndIgnoreRemainingEvents()
        }
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

    @Test
    fun `getIncompleteTaskCount returns flow of count`() = runTest {
        every { dao.getIncompleteTaskCount(1L) } returns flowOf(3)

        repository.getIncompleteTaskCount().test {
            assertEquals(3, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getIncompleteTaskCount returns zero when no tasks`() = runTest {
        every { dao.getIncompleteTaskCount(1L) } returns flowOf(0)

        repository.getIncompleteTaskCount().test {
            assertEquals(0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun assertFalse(value: Boolean) {
        org.junit.Assert.assertFalse(value)
    }
}
