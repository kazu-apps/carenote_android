package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.entity.NoteEntity
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import app.cash.turbine.test
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
    private lateinit var repository: NoteRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = NoteMapper()
        repository = NoteRepositoryImpl(dao, mapper)
    }

    private fun createEntity(
        id: Long = 1L,
        title: String = "テストメモ",
        content: String = "テスト内容",
        tag: String = "OTHER"
    ) = NoteEntity(
        id = id,
        title = title,
        content = content,
        tag = tag,
        authorId = "",
        createdAt = "2025-03-15T10:00:00",
        updatedAt = "2025-03-15T10:00:00"
    )

    @Test
    fun `getAllNotes returns flow of notes`() = runTest {
        val entities = listOf(
            createEntity(1L, "メモA"),
            createEntity(2L, "メモB")
        )
        every { dao.getAllNotes() } returns flowOf(entities)

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
        every { dao.getAllNotes() } returns flowOf(emptyList())

        repository.getAllNotes().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getNoteById returns note when found`() = runTest {
        val entity = createEntity(1L, "テストメモ")
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
        val entities = listOf(createEntity(1L, "メモA"), createEntity(2L, "メモB"))
        every { dao.getAllNotes() } returns flowOf(entities)

        repository.searchNotes("", null).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchNotes with query and null tag searches by text`() = runTest {
        val entities = listOf(createEntity(1L, "体調メモ"))
        every { dao.searchNotes("体調") } returns flowOf(entities)

        repository.searchNotes("体調", null).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("体調メモ", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchNotes with empty query and tag filters by tag`() = runTest {
        val entities = listOf(createEntity(1L, "体調メモ", tag = "CONDITION"))
        every { dao.getNotesByTag("CONDITION") } returns flowOf(entities)

        repository.searchNotes("", NoteTag.CONDITION).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchNotes with query and tag searches by both`() = runTest {
        val entities = listOf(
            createEntity(1L, "体調メモ", tag = "CONDITION")
        )
        every { dao.searchNotesByTag("体調", "CONDITION") } returns flowOf(entities)

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

        val note = Note(
            title = "テストメモ",
            content = "テスト内容",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.insertNote(note)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).value)
    }

    @Test
    fun `insertNote returns Failure on db error`() = runTest {
        coEvery { dao.insertNote(any()) } throws RuntimeException("DB error")

        val note = Note(
            title = "テストメモ",
            content = "テスト内容",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.insertNote(note)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `updateNote returns Success`() = runTest {
        coEvery { dao.updateNote(any()) } returns Unit

        val note = Note(
            id = 1L,
            title = "更新メモ",
            content = "更新内容",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.updateNote(note)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateNote returns Failure on db error`() = runTest {
        coEvery { dao.updateNote(any()) } throws RuntimeException("DB error")

        val note = Note(
            id = 1L,
            title = "更新メモ",
            content = "更新内容",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.updateNote(note)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `deleteNote returns Success`() = runTest {
        coEvery { dao.deleteNote(1L) } returns Unit

        val result = repository.deleteNote(1L)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteNote(1L) }
    }

    @Test
    fun `deleteNote returns Failure on db error`() = runTest {
        coEvery { dao.deleteNote(1L) } throws RuntimeException("DB error")

        val result = repository.deleteNote(1L)

        assertTrue(result is Result.Failure)
    }
}
