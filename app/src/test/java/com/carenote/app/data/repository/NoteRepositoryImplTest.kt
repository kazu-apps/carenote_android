package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.entity.NoteEntity
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.model.User
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.PhotoRepository
import com.carenote.app.testing.TestDataFixtures
import com.carenote.app.testing.aNote
import com.carenote.app.testing.assertDatabaseError
import com.carenote.app.testing.assertFailure
import com.carenote.app.testing.assertSuccess
import com.carenote.app.testing.assertSuccessValue
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
import java.time.LocalDateTime

class NoteRepositoryImplTest {

    private lateinit var dao: NoteDao
    private lateinit var mapper: NoteMapper
    private lateinit var photoRepository: PhotoRepository
    private lateinit var activeRecipientProvider: FakeActiveCareRecipientProvider
    private lateinit var authRepository: AuthRepository
    private lateinit var repository: NoteRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = NoteMapper()
        photoRepository = mockk()
        activeRecipientProvider = FakeActiveCareRecipientProvider()
        authRepository = mockk()
        every { authRepository.getCurrentUser() } returns User(
            uid = "test-uid",
            name = "Test User",
            email = "test@example.com",
            createdAt = LocalDateTime.of(2025, 1, 1, 0, 0)
        )
        repository = NoteRepositoryImpl(dao, mapper, photoRepository, activeRecipientProvider, authRepository)
    }

    private fun createEntity(
        id: Long = 1L,
        careRecipientId: Long = 1L,
        title: String = "テストメモ",
        content: String = "テスト内容",
        tag: String = "OTHER",
        createdBy: String = "test-uid"
    ) = NoteEntity(
        id = id,
        careRecipientId = careRecipientId,
        title = title,
        content = content,
        tag = tag,
        createdBy = createdBy,
        createdAt = TestDataFixtures.NOW_STRING,
        updatedAt = TestDataFixtures.NOW_STRING
    )

    @Test
    fun `getAllNotes returns flow of notes`() = runTest {
        val entities = listOf(
            createEntity(id = 1L, title = "メモA"),
            createEntity(id = 2L, title = "メモB")
        )
        every { dao.getAllNotes(1L) } returns flowOf(entities)

        repository.getAllNotes().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("メモA", result[0].title)
            assertEquals("メモB", result[1].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllNotes returns empty list when no notes`() = runTest {
        every { dao.getAllNotes(1L) } returns flowOf(emptyList())

        repository.getAllNotes().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getNoteById returns note when found`() = runTest {
        val entity = createEntity(id = 1L, title = "テストメモ")
        every { dao.getNoteById(1L) } returns flowOf(entity)

        repository.getNoteById(1L).test {
            val result = awaitItem()
            assertEquals("テストメモ", result?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getNoteById returns null when not found`() = runTest {
        every { dao.getNoteById(999L) } returns flowOf(null)

        repository.getNoteById(999L).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchNotes with empty query and null tag returns all notes`() = runTest {
        val entities = listOf(createEntity(id = 1L, title = "メモA"), createEntity(id = 2L, title = "メモB"))
        every { dao.getAllNotes(1L) } returns flowOf(entities)

        repository.searchNotes("", null).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchNotes with query and null tag searches by text`() = runTest {
        val entities = listOf(createEntity(id = 1L, title = "体調メモ"))
        every { dao.searchNotes("体調", 1L) } returns flowOf(entities)

        repository.searchNotes("体調", null).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("体調メモ", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchNotes with empty query and tag filters by tag`() = runTest {
        val entities = listOf(createEntity(id = 1L, title = "体調メモ", tag = "CONDITION"))
        every { dao.getNotesByTag("CONDITION", 1L) } returns flowOf(entities)

        repository.searchNotes("", NoteTag.CONDITION).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchNotes with query and tag searches by both`() = runTest {
        val entities = listOf(
            createEntity(id = 1L, title = "体調メモ", tag = "CONDITION")
        )
        every { dao.searchNotesByTag("体調", "CONDITION", 1L) } returns flowOf(entities)

        repository.searchNotes("体調", NoteTag.CONDITION).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("体調メモ", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertNote returns Success with id`() = runTest {
        coEvery { dao.insertNote(any()) } returns 1L

        val note = aNote(title = "テストメモ", content = "テスト内容")
        val result = repository.insertNote(note)

        result.assertSuccessValue(1L)
    }

    @Test
    fun `insertNote returns Failure on db error`() = runTest {
        coEvery { dao.insertNote(any()) } throws RuntimeException("DB error")

        val note = aNote(title = "テストメモ", content = "テスト内容")
        val result = repository.insertNote(note)

        result.assertDatabaseError()
    }

    @Test
    fun `updateNote returns Success`() = runTest {
        coEvery { dao.updateNote(any()) } returns Unit

        val note = aNote(id = 1L, title = "更新メモ", content = "更新内容")
        val result = repository.updateNote(note)

        result.assertSuccess()
    }

    @Test
    fun `updateNote returns Failure on db error`() = runTest {
        coEvery { dao.updateNote(any()) } throws RuntimeException("DB error")

        val note = aNote(id = 1L, title = "更新メモ", content = "更新内容")
        val result = repository.updateNote(note)

        result.assertFailure()
    }

    @Test
    fun `deleteNote returns Success`() = runTest {
        coEvery { photoRepository.deletePhotosForParent("note", 1L) } returns Result.Success(Unit)
        coEvery { dao.deleteNote(1L) } returns Unit

        val result = repository.deleteNote(1L)

        result.assertSuccess()
        coVerify { dao.deleteNote(1L) }
    }

    @Test
    fun `deleteNote returns Failure on db error`() = runTest {
        coEvery { photoRepository.deletePhotosForParent("note", 1L) } returns Result.Success(Unit)
        coEvery { dao.deleteNote(1L) } throws RuntimeException("DB error")

        val result = repository.deleteNote(1L)

        result.assertFailure()
    }

    @Test
    fun `deleteNote cascades photo deletion`() = runTest {
        coEvery { photoRepository.deletePhotosForParent("note", 5L) } returns Result.Success(Unit)
        coEvery { dao.deleteNote(5L) } returns Unit

        repository.deleteNote(5L)

        coVerify { photoRepository.deletePhotosForParent("note", 5L) }
        coVerify { dao.deleteNote(5L) }
    }

    @Test
    fun `deleteNote deletes photos before note`() = runTest {
        val callOrder = mutableListOf<String>()
        coEvery { photoRepository.deletePhotosForParent("note", 1L) } coAnswers {
            callOrder.add("photos")
            Result.Success(Unit)
        }
        coEvery { dao.deleteNote(1L) } coAnswers {
            callOrder.add("note")
            Unit
        }

        repository.deleteNote(1L)

        assertEquals(listOf("photos", "note"), callOrder)
    }

    @Test
    fun `deleteNote still succeeds when photo deletion fails`() = runTest {
        coEvery {
            photoRepository.deletePhotosForParent("note", 1L)
        } returns Result.Failure(DomainError.DatabaseError("Photo DB error"))
        coEvery { dao.deleteNote(1L) } returns Unit

        val result = repository.deleteNote(1L)

        result.assertSuccess()
        coVerify { photoRepository.deletePhotosForParent("note", 1L) }
        coVerify { dao.deleteNote(1L) }
    }
}
