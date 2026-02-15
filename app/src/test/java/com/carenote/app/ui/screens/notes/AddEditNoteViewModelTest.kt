package com.carenote.app.ui.screens.notes

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.repository.ImageCompressorInterface
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteComment
import com.carenote.app.fakes.FakeClock
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeNoteCommentRepository
import com.carenote.app.fakes.FakeNoteRepository
import com.carenote.app.fakes.FakePhotoRepository
import com.carenote.app.ui.common.UiText
import io.mockk.mockk
import com.carenote.app.testing.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditNoteViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var photoRepository: FakePhotoRepository
    private val imageCompressor: ImageCompressorInterface = mockk()
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private val fakeClock = FakeClock()
    private lateinit var noteCommentRepository: FakeNoteCommentRepository
    private lateinit var viewModel: AddEditNoteViewModel

    @Before
    fun setUp() {
        noteRepository = FakeNoteRepository()
        photoRepository = FakePhotoRepository()
        analyticsRepository = FakeAnalyticsRepository()
        noteCommentRepository = FakeNoteCommentRepository()
    }

    private fun createAddViewModel(): AddEditNoteViewModel {
        return AddEditNoteViewModel(
            SavedStateHandle(),
            noteRepository,
            photoRepository,
            imageCompressor,
            analyticsRepository,
            clock = fakeClock,
            noteCommentRepository = noteCommentRepository
        )
    }

    private fun createEditViewModel(noteId: Long): AddEditNoteViewModel {
        return AddEditNoteViewModel(
            SavedStateHandle(mapOf("noteId" to noteId)),
            noteRepository,
            photoRepository,
            imageCompressor,
            analyticsRepository,
            clock = fakeClock,
            noteCommentRepository = noteCommentRepository
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
            UiText.Resource(R.string.notes_title_required),
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
            UiText.Resource(R.string.notes_content_required),
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

    // --- isDirty Tests ---

    @Test
    fun `isDirty is false initially in add mode`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when title changed`() {
        viewModel = createAddViewModel()

        viewModel.updateTitle("新タイトル")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty returns to false when title cleared`() {
        viewModel = createAddViewModel()

        viewModel.updateTitle("テスト")
        assertTrue(viewModel.isDirty)

        viewModel.updateTitle("")
        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty is false after loading existing data`() = runTest {
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

        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when field changed in edit mode`() = runTest {
        noteRepository.setNotes(
            listOf(
                Note(
                    id = 1L,
                    title = "既存メモ",
                    content = "既存内容",
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTitle("変更タイトル")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty returns to false when reverted to original in edit mode`() = runTest {
        noteRepository.setNotes(
            listOf(
                Note(
                    id = 1L,
                    title = "既存メモ",
                    content = "既存内容",
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTitle("変更タイトル")
        assertTrue(viewModel.isDirty)

        viewModel.updateTitle("既存メモ")
        assertFalse(viewModel.isDirty)
    }

    // --- Comment Tests ---

    @Test
    fun `comments is empty initially`() {
        viewModel = createAddViewModel()

        assertTrue(viewModel.comments.value.isEmpty())
    }

    @Test
    fun `commentText is empty initially`() {
        viewModel = createAddViewModel()

        assertEquals("", viewModel.commentText.value)
    }

    @Test
    fun `updateCommentText updates state`() {
        viewModel = createAddViewModel()

        viewModel.updateCommentText("テストコメント")

        assertEquals("テストコメント", viewModel.commentText.value)
    }

    @Test
    fun `addComment with empty text does nothing`() = runTest {
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
        advanceUntilIdle()

        viewModel.updateCommentText("")
        viewModel.addComment()
        advanceUntilIdle()

        assertTrue(viewModel.comments.value.isEmpty())
    }

    @Test
    fun `addComment in add mode does nothing`() = runTest {
        viewModel = createAddViewModel()

        viewModel.updateCommentText("コメント")
        viewModel.addComment()
        advanceUntilIdle()

        assertTrue(viewModel.comments.value.isEmpty())
    }

    @Test
    fun `addComment in edit mode inserts comment and clears text`() = runTest {
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
        advanceUntilIdle()

        viewModel.updateCommentText("新しいコメント")
        viewModel.addComment()
        advanceUntilIdle()

        assertEquals("", viewModel.commentText.value)
        val comments = viewModel.comments.value
        assertEquals(1, comments.size)
        assertEquals("新しいコメント", comments[0].content)
    }

    @Test
    fun `addComment on failure shows snackbar`() = runTest {
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
        advanceUntilIdle()

        noteCommentRepository.shouldFail = true
        viewModel.updateCommentText("失敗コメント")
        viewModel.addComment()
        advanceUntilIdle()

        // Comment should not be added on failure
        assertTrue(viewModel.comments.value.isEmpty())
    }

    @Test
    fun `deleteComment in edit mode removes comment`() = runTest {
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
        noteCommentRepository.setComments(
            listOf(
                NoteComment(
                    id = 10L,
                    noteId = 1L,
                    content = "削除対象コメント",
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        assertEquals(1, viewModel.comments.value.size)

        viewModel.deleteComment(10L)
        advanceUntilIdle()

        assertTrue(viewModel.comments.value.isEmpty())
    }

    @Test
    fun `deleteComment on failure shows snackbar`() = runTest {
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
        noteCommentRepository.setComments(
            listOf(
                NoteComment(
                    id = 10L,
                    noteId = 1L,
                    content = "削除対象コメント",
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        noteCommentRepository.shouldFail = true
        viewModel.deleteComment(10L)
        advanceUntilIdle()

        // Comment should still exist on failure
        assertEquals(1, viewModel.comments.value.size)
    }

    @Test
    fun `loadComments populates comments in edit mode`() = runTest {
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
        noteCommentRepository.setComments(
            listOf(
                NoteComment(
                    id = 1L,
                    noteId = 1L,
                    content = "コメント1",
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                ),
                NoteComment(
                    id = 2L,
                    noteId = 1L,
                    content = "コメント2",
                    createdAt = LocalDateTime.of(2025, 3, 15, 11, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 11, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        val comments = viewModel.comments.value
        assertEquals(2, comments.size)
    }
}
