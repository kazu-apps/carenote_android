package com.carenote.app.ui.screens.notes

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.fakes.FakeNoteRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        noteRepository = FakeNoteRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): NotesViewModel {
        return NotesViewModel(noteRepository)
    }

    private fun createNote(
        id: Long = 1L,
        title: String = "テストメモ",
        content: String = "テスト内容",
        tag: NoteTag = NoteTag.OTHER,
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ) = Note(
        id = id,
        title = title,
        content = content,
        tag = tag,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        assertTrue(viewModel.notes.value is UiState.Loading)
    }

    @Test
    fun `notes transitions from Loading to Success`() = runTest(testDispatcher) {
        val notes = listOf(createNote(id = 1L, title = "メモA"))
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.notes.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(1, (success as UiState.Success).data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `notes are loaded as Success state`() = runTest(testDispatcher) {
        val notes = listOf(
            createNote(id = 1L, title = "メモA"),
            createNote(id = 2L, title = "メモB")
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.notes.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(2, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `empty notes list shows Success with empty list`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.notes.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(0, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `initial search query is empty`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `initial selected tag is null`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        assertNull(viewModel.selectedTag.value)
    }

    @Test
    fun `updateSearchQuery updates search query`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.updateSearchQuery("テスト")

        assertEquals("テスト", viewModel.searchQuery.value)
    }

    @Test
    fun `selectTag updates selected tag`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.selectTag(NoteTag.CONDITION)

        assertEquals(NoteTag.CONDITION, viewModel.selectedTag.value)
    }

    @Test
    fun `selectTag with null clears tag filter`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        viewModel.selectTag(NoteTag.CONDITION)

        viewModel.selectTag(null)

        assertNull(viewModel.selectedTag.value)
    }

    @Test
    fun `tag filter shows only matching notes`() = runTest(testDispatcher) {
        val notes = listOf(
            createNote(id = 1L, title = "体調メモ", tag = NoteTag.CONDITION),
            createNote(id = 2L, title = "食事メモ", tag = NoteTag.MEAL),
            createNote(id = 3L, title = "申し送り", tag = NoteTag.REPORT)
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.notes.test {
            advanceUntilIdle()

            viewModel.selectTag(NoteTag.CONDITION)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("体調メモ", data[0].title)
        }
    }

    @Test
    fun `deleteNote removes note from list`() = runTest(testDispatcher) {
        val notes = listOf(
            createNote(id = 1L, title = "メモA"),
            createNote(id = 2L, title = "メモB")
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.notes.test {
            advanceUntilIdle()

            viewModel.deleteNote(1L)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("メモB", data[0].title)
        }
    }

    @Test
    fun `snackbar emitted on delete success`() = runTest(testDispatcher) {
        val notes = listOf(createNote(id = 1L))
        noteRepository.setNotes(notes)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteNote(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.notes_deleted, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `snackbar emitted on delete failure`() = runTest(testDispatcher) {
        val notes = listOf(createNote(id = 1L))
        noteRepository.setNotes(notes)
        noteRepository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteNote(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.notes_delete_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `notes update reactively when repository changes`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.notes.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(0, (initial as UiState.Success).data.size)

            noteRepository.setNotes(listOf(createNote(id = 1L)))
            advanceUntilIdle()
            val updated = expectMostRecentItem()
            assertTrue(updated is UiState.Success)
            assertEquals(1, (updated as UiState.Success).data.size)
        }
    }

    @Test
    fun `search filters notes by title`() = runTest(testDispatcher) {
        val notes = listOf(
            createNote(id = 1L, title = "体調メモ", content = "良好"),
            createNote(id = 2L, title = "食事メモ", content = "朝食")
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.notes.test {
            advanceUntilIdle()

            viewModel.updateSearchQuery("体調")
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("体調メモ", data[0].title)
        }
    }

    @Test
    fun `search filters notes by content`() = runTest(testDispatcher) {
        val notes = listOf(
            createNote(id = 1L, title = "メモA", content = "熱が37度"),
            createNote(id = 2L, title = "メモB", content = "昼食は完食")
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.notes.test {
            advanceUntilIdle()

            viewModel.updateSearchQuery("完食")
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("メモB", data[0].title)
        }
    }

    @Test
    fun `combined search and tag filter`() = runTest(testDispatcher) {
        val notes = listOf(
            createNote(id = 1L, title = "体調A", tag = NoteTag.CONDITION),
            createNote(id = 2L, title = "体調B", tag = NoteTag.MEAL),
            createNote(id = 3L, title = "食事C", tag = NoteTag.CONDITION)
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.notes.test {
            advanceUntilIdle()

            viewModel.updateSearchQuery("体調")
            viewModel.selectTag(NoteTag.CONDITION)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("体調A", data[0].title)
        }
    }
}
