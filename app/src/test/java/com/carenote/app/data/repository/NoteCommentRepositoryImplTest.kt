package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.NoteCommentDao
import com.carenote.app.data.local.entity.NoteCommentEntity
import com.carenote.app.data.mapper.NoteCommentMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.NoteComment
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class NoteCommentRepositoryImplTest {

    private lateinit var dao: NoteCommentDao
    private lateinit var mapper: NoteCommentMapper
    private lateinit var activeRecipientProvider: FakeActiveCareRecipientProvider
    private lateinit var authRepository: AuthRepository
    private lateinit var repository: NoteCommentRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = NoteCommentMapper()
        activeRecipientProvider = FakeActiveCareRecipientProvider()
        authRepository = mockk()
        every { authRepository.getCurrentUser() } returns User(
            uid = "test-uid",
            name = "Test User",
            email = "test@example.com",
            createdAt = LocalDateTime.of(2025, 1, 1, 0, 0)
        )
        repository = NoteCommentRepositoryImpl(dao, mapper, activeRecipientProvider, authRepository)
    }

    private fun createEntity(
        id: Long = 1L,
        noteId: Long = 10L,
        content: String = "テストコメント"
    ) = NoteCommentEntity(
        id = id,
        noteId = noteId,
        content = content,
        createdBy = "user123",
        createdAt = "2025-03-15T08:00:00",
        updatedAt = "2025-03-15T08:05:00"
    )

    @Test
    fun `getCommentsForNote returns flow of comments`() = runTest {
        val entities = listOf(
            createEntity(1L, 10L, "コメント1"),
            createEntity(2L, 10L, "コメント2")
        )
        every { dao.getCommentsForNote(10L) } returns flowOf(entities)

        repository.getCommentsForNote(10L).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("コメント1", result[0].content)
            assertEquals("コメント2", result[1].content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCommentsForNote returns empty list when no comments`() = runTest {
        every { dao.getCommentsForNote(999L) } returns flowOf(emptyList())

        repository.getCommentsForNote(999L).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllComments returns flow of all comments`() = runTest {
        val entities = listOf(createEntity(1L, 10L))
        every { dao.getAllComments(1L) } returns flowOf(entities)

        repository.getAllComments().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertComment returns Success with id`() = runTest {
        coEvery { dao.insertComment(any()) } returns 1L

        val comment = NoteComment(
            noteId = 10L,
            content = "新しいコメント",
            createdAt = LocalDateTime.of(2025, 3, 15, 8, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 8, 0)
        )
        val result = repository.insertComment(comment)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).value)
    }

    @Test
    fun `insertComment sets createdBy from auth user`() = runTest {
        coEvery { dao.insertComment(any()) } returns 1L

        val comment = NoteComment(
            noteId = 10L,
            content = "テスト",
            createdAt = LocalDateTime.of(2025, 3, 15, 8, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 8, 0)
        )
        repository.insertComment(comment)

        coVerify {
            dao.insertComment(match {
                it.createdBy == "test-uid" && it.careRecipientId == 1L
            })
        }
    }

    @Test
    fun `insertComment returns Failure on db error`() = runTest {
        coEvery { dao.insertComment(any()) } throws RuntimeException("DB error")

        val comment = NoteComment(
            noteId = 10L,
            content = "テスト",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val result = repository.insertComment(comment)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `updateComment returns Success`() = runTest {
        coEvery { dao.updateComment(any()) } returns Unit

        val comment = NoteComment(
            id = 1L,
            noteId = 10L,
            content = "更新コメント",
            createdAt = LocalDateTime.of(2025, 3, 15, 8, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 8, 5)
        )
        val result = repository.updateComment(comment)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateComment returns Failure on db error`() = runTest {
        coEvery { dao.updateComment(any()) } throws RuntimeException("DB error")

        val comment = NoteComment(
            id = 1L,
            noteId = 10L,
            content = "更新テスト",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val result = repository.updateComment(comment)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `deleteComment returns Success`() = runTest {
        coEvery { dao.deleteComment(1L) } returns Unit

        val result = repository.deleteComment(1L)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteComment(1L) }
    }

    @Test
    fun `deleteComment returns Failure on db error`() = runTest {
        coEvery { dao.deleteComment(1L) } throws RuntimeException("DB error")

        val result = repository.deleteComment(1L)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `deleteCommentsForNote returns Success`() = runTest {
        coEvery { dao.deleteCommentsForNote(10L) } returns Unit

        val result = repository.deleteCommentsForNote(10L)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteCommentsForNote(10L) }
    }

    @Test
    fun `deleteCommentsForNote returns Failure on db error`() = runTest {
        coEvery { dao.deleteCommentsForNote(10L) } throws RuntimeException("DB error")

        val result = repository.deleteCommentsForNote(10L)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `getCommentsForNote maps all fields correctly`() = runTest {
        val entity = NoteCommentEntity(
            id = 5L,
            noteId = 20L,
            content = "詳細コメント",
            createdBy = "user456",
            createdAt = "2025-03-15T18:00:00",
            updatedAt = "2025-03-15T18:30:00"
        )
        every { dao.getCommentsForNote(20L) } returns flowOf(listOf(entity))

        repository.getCommentsForNote(20L).test {
            val result = awaitItem()
            assertEquals(5L, result[0].id)
            assertEquals(20L, result[0].noteId)
            assertEquals("詳細コメント", result[0].content)
            assertEquals("user456", result[0].createdBy)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
