package com.carenote.app.ui.screens.notes

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeNoteRepository
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        noteRepository = FakeNoteRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): NotesViewModel {
        return NotesViewModel(noteRepository, analyticsRepository)
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
    fun `initial search query is empty`() {
        viewModel = createViewModel()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `initial selected tag is null`() {
        viewModel = createViewModel()

        assertNull(viewModel.selectedTag.value)
    }

    @Test
    fun `updateSearchQuery updates search query`() {
        viewModel = createViewModel()

        viewModel.updateSearchQuery("テスト")

        assertEquals("テスト", viewModel.searchQuery.value)
    }

    @Test
    fun `selectTag updates selected tag`() {
        viewModel = createViewModel()

        viewModel.selectTag(NoteTag.CONDITION)

        assertEquals(NoteTag.CONDITION, viewModel.selectedTag.value)
    }

    @Test
    fun `selectTag with null clears tag filter`() {
        viewModel = createViewModel()
        viewModel.selectTag(NoteTag.CONDITION)

        viewModel.selectTag(null)

        assertNull(viewModel.selectedTag.value)
    }

    @Test
    fun `tag filter shows only matching notes`() = runTest {
        val notes = listOf(
            createNote(id = 1L, title = "体調メモ", tag = NoteTag.CONDITION),
            createNote(id = 2L, title = "食事メモ", tag = NoteTag.MEAL),
            createNote(id = 3L, title = "申し送り", tag = NoteTag.REPORT)
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        val filtered = noteRepository.getFilteredNotes("", NoteTag.CONDITION)
        assertEquals(1, filtered.size)
        assertEquals("体調メモ", filtered[0].title)
    }

    @Test
    fun `deleteNote removes note from list`() = runTest {
        val notes = listOf(
            createNote(id = 1L, title = "メモA"),
            createNote(id = 2L, title = "メモB")
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.deleteNote(1L)

        val remaining = noteRepository.currentNotes()
        assertEquals(1, remaining.size)
        assertEquals("メモB", remaining[0].title)
    }

    @Test
    fun `snackbar emitted on delete success`() = runTest {
        val notes = listOf(createNote(id = 1L))
        noteRepository.setNotes(notes)
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.deleteNote(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.notes_deleted, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `snackbar emitted on delete failure`() = runTest {
        val notes = listOf(createNote(id = 1L))
        noteRepository.setNotes(notes)
        noteRepository.shouldFail = true
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.deleteNote(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.notes_delete_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `notes update reactively when repository changes`() = runTest {
        viewModel = createViewModel()

        assertEquals(0, noteRepository.currentNotes().size)

        noteRepository.setNotes(listOf(createNote(id = 1L)))

        assertEquals(1, noteRepository.currentNotes().size)
    }

    @Test
    fun `search filters notes by title`() = runTest {
        val notes = listOf(
            createNote(id = 1L, title = "体調メモ", content = "良好"),
            createNote(id = 2L, title = "食事メモ", content = "朝食")
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()
        viewModel.updateSearchQuery("体調")

        val filtered = noteRepository.getFilteredNotes("体調")
        assertEquals(1, filtered.size)
        assertEquals("体調メモ", filtered[0].title)
    }

    @Test
    fun `search filters notes by content`() = runTest {
        val notes = listOf(
            createNote(id = 1L, title = "メモA", content = "熱が37度"),
            createNote(id = 2L, title = "メモB", content = "昼食は完食")
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()
        viewModel.updateSearchQuery("完食")

        val filtered = noteRepository.getFilteredNotes("完食")
        assertEquals(1, filtered.size)
        assertEquals("メモB", filtered[0].title)
    }

    @Test
    fun `combined search and tag filter`() = runTest {
        val notes = listOf(
            createNote(id = 1L, title = "体調A", tag = NoteTag.CONDITION),
            createNote(id = 2L, title = "体調B", tag = NoteTag.MEAL),
            createNote(id = 3L, title = "食事C", tag = NoteTag.CONDITION)
        )
        noteRepository.setNotes(notes)
        viewModel = createViewModel()
        viewModel.updateSearchQuery("体調")
        viewModel.selectTag(NoteTag.CONDITION)

        val filtered = noteRepository.getFilteredNotes("体調", NoteTag.CONDITION)
        assertEquals(1, filtered.size)
        assertEquals("体調A", filtered[0].title)
    }
}
