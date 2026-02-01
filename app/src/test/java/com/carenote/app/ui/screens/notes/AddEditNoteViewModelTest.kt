package com.carenote.app.ui.screens.notes

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.fakes.FakeNoteRepository
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
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditNoteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var viewModel: AddEditNoteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        noteRepository = FakeNoteRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createAddViewModel(): AddEditNoteViewModel {
        return AddEditNoteViewModel(SavedStateHandle(), noteRepository)
    }

    private fun createEditViewModel(noteId: Long): AddEditNoteViewModel {
        return AddEditNoteViewModel(
            SavedStateHandle(mapOf("noteId" to noteId)),
            noteRepository
        )
    }

    @Test
    fun `initial form state has empty fields for add mode`() {
        viewModel = createAddViewModel()

        val state = viewModel.formState.value

        assertEquals("", state.title)
        assertEquals("", state.content)
        assertEquals(NoteTag.OTHER, state.tag)
        assertNull(state.titleError)
        assertNull(state.contentError)
        assertFalse(state.isSaving)
        assertFalse(state.isEditMode)
    }

    @Test
    fun `edit mode is true when noteId is provided`() {
        noteRepository.setNotes(
            listOf(
                Note(
                    id = 1L,
                    title = "テスト",
                    content = "内容",
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)

        assertTrue(viewModel.formState.value.isEditMode)
    }

    @Test
    fun `edit mode loads existing note data`() = runTest {
        noteRepository.setNotes(
            listOf(
                Note(
                    id = 1L,
                    title = "既存メモ",
                    content = "既存内容",
                    tag = NoteTag.CONDITION,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals("既存メモ", state.title)
        assertEquals("既存内容", state.content)
        assertEquals(NoteTag.CONDITION, state.tag)
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
        viewModel.saveNote()
        assertNotNull(viewModel.formState.value.titleError)

        viewModel.updateTitle("タイトル")

        assertNull(viewModel.formState.value.titleError)
    }

    @Test
    fun `updateContent updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateContent("新しい内容")

        assertEquals("新しい内容", viewModel.formState.value.content)
    }

    @Test
    fun `updateContent clears content error`() {
        viewModel = createAddViewModel()
        viewModel.updateTitle("タイトル")
        viewModel.saveNote()
        assertNotNull(viewModel.formState.value.contentError)

        viewModel.updateContent("内容")

        assertNull(viewModel.formState.value.contentError)
    }

    @Test
    fun `updateTag updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateTag(NoteTag.REPORT)

        assertEquals(NoteTag.REPORT, viewModel.formState.value.tag)
    }

    @Test
    fun `saveNote with empty title sets title error`() {
        viewModel = createAddViewModel()
        viewModel.updateContent("内容あり")

        viewModel.saveNote()

        assertNotNull(viewModel.formState.value.titleError)
        assertEquals(
            AddEditNoteViewModel.TITLE_REQUIRED_ERROR,
            viewModel.formState.value.titleError
        )
    }

    @Test
    fun `saveNote with blank title sets title error`() {
        viewModel = createAddViewModel()
        viewModel.updateTitle("   ")
        viewModel.updateContent("内容あり")

        viewModel.saveNote()

        assertNotNull(viewModel.formState.value.titleError)
    }

    @Test
    fun `saveNote with empty content sets content error`() {
        viewModel = createAddViewModel()
        viewModel.updateTitle("タイトル")

        viewModel.saveNote()

        assertNotNull(viewModel.formState.value.contentError)
        assertEquals(
            AddEditNoteViewModel.CONTENT_REQUIRED_ERROR,
            viewModel.formState.value.contentError
        )
    }

    @Test
    fun `saveNote with blank content sets content error`() {
        viewModel = createAddViewModel()
        viewModel.updateTitle("タイトル")
        viewModel.updateContent("   ")

        viewModel.saveNote()

        assertNotNull(viewModel.formState.value.contentError)
    }

    @Test
    fun `saveNote with both empty sets both errors`() {
        viewModel = createAddViewModel()

        viewModel.saveNote()

        assertNotNull(viewModel.formState.value.titleError)
        assertNotNull(viewModel.formState.value.contentError)
    }

    @Test
    fun `saveNote with valid data succeeds in add mode`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("テストメモ")
        viewModel.updateContent("テスト内容")

        viewModel.saveNote()
        advanceUntilIdle()

        viewModel.savedEvent.test {
            val saved = awaitItem()
            assertTrue(saved)
        }
    }

    @Test
    fun `saveNote inserts note to repository in add mode`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("テストメモ")
        viewModel.updateContent("テスト内容")
        viewModel.updateTag(NoteTag.MEAL)

        viewModel.saveNote()
        advanceUntilIdle()

        noteRepository.getAllNotes().test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("テストメモ", notes[0].title)
            assertEquals("テスト内容", notes[0].content)
            assertEquals(NoteTag.MEAL, notes[0].tag)
        }
    }

    @Test
    fun `saveNote trims title and content`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTitle("  テスト  ")
        viewModel.updateContent("  内容  ")

        viewModel.saveNote()
        advanceUntilIdle()

        noteRepository.getAllNotes().test {
            val notes = awaitItem()
            assertEquals("テスト", notes[0].title)
            assertEquals("内容", notes[0].content)
        }
    }

    @Test
    fun `saveNote updates existing note in edit mode`() = runTest {
        noteRepository.setNotes(
            listOf(
                Note(
                    id = 1L,
                    title = "旧タイトル",
                    content = "旧内容",
                    tag = NoteTag.OTHER,
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTitle("新タイトル")
        viewModel.updateContent("新内容")
        viewModel.updateTag(NoteTag.REPORT)
        viewModel.saveNote()
        advanceUntilIdle()

        noteRepository.getAllNotes().test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("新タイトル", notes[0].title)
            assertEquals("新内容", notes[0].content)
            assertEquals(NoteTag.REPORT, notes[0].tag)
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
        noteRepository.shouldFail = true
        viewModel = createAddViewModel()
        viewModel.updateTitle("テスト")
        viewModel.updateContent("内容")

        viewModel.saveNote()
        advanceUntilIdle()

        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `default tag is OTHER`() {
        viewModel = createAddViewModel()

        assertEquals(NoteTag.OTHER, viewModel.formState.value.tag)
    }
}
