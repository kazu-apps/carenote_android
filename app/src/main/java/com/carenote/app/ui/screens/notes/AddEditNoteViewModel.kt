package com.carenote.app.ui.screens.notes

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.ImageCompressorInterface
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteComment
import com.carenote.app.domain.util.Clock
import com.carenote.app.domain.model.Photo
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.NoteCommentRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.PhotoRepository
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.FormValidator.combineValidations
import com.carenote.app.ui.util.FormValidator.validateMaxLength
import com.carenote.app.ui.util.FormValidator.validateRequired
import com.carenote.app.ui.viewmodel.PhotoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AddEditNoteFormState(
    val title: String = "",
    val content: String = "",
    val tag: NoteTag = NoteTag.OTHER,
    val titleError: UiText? = null,
    val contentError: UiText? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val photoRepository: PhotoRepository,
    private val imageCompressor: ImageCompressorInterface,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock,
    private val noteCommentRepository: NoteCommentRepository
) : ViewModel() {

    private val noteId: Long? = savedStateHandle.get<Long>("noteId")

    private val _formState = MutableStateFlow(
        AddEditNoteFormState(isEditMode = noteId != null)
    )
    val formState: StateFlow<AddEditNoteFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private var originalNote: Note? = null
    private var _initialFormState: AddEditNoteFormState? = null

    val photoManager = PhotoManager(
        parentType = "note",
        parentId = noteId ?: 0L,
        photoRepository = photoRepository,
        imageCompressor = imageCompressor,
        scope = viewModelScope,
        snackbarController = snackbarController,
        clock = clock
    )

    val photos: StateFlow<List<Photo>> get() = photoManager.photos

    private val _comments = MutableStateFlow<List<NoteComment>>(emptyList())
    val comments: StateFlow<List<NoteComment>> = _comments.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    val isDirty: Boolean
        get() {
            val initial = _initialFormState ?: return false
            val current = _formState.value.copy(
                titleError = null,
                contentError = null,
                isSaving = false,
                isEditMode = false
            )
            val baseline = initial.copy(
                titleError = null,
                contentError = null,
                isSaving = false,
                isEditMode = false
            )
            if (current != baseline) return true
            return photoManager.hasChanges
        }

    init {
        if (noteId != null) {
            loadNote(noteId)
        } else {
            _initialFormState = _formState.value
        }
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(id).firstOrNull()
            if (note != null) {
                originalNote = note
                _formState.value = _formState.value.copy(
                    title = note.title,
                    content = note.content,
                    tag = note.tag
                )
                _initialFormState = _formState.value
                photoManager.loadPhotos()
                loadComments(id)
            }
        }
    }

    fun addPhotos(uris: List<Uri>) = photoManager.addPhotos(uris)

    fun removePhoto(photo: Photo) = photoManager.removePhoto(photo)

    fun updateTitle(title: String) {
        _formState.value = _formState.value.copy(
            title = title,
            titleError = null
        )
    }

    fun updateContent(content: String) {
        _formState.value = _formState.value.copy(
            content = content,
            contentError = null
        )
    }

    fun updateTag(tag: NoteTag) {
        _formState.value = _formState.value.copy(tag = tag)
    }

    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    fun addComment() {
        val text = _commentText.value.trim()
        if (text.isEmpty()) return
        val currentNoteId = noteId ?: return

        viewModelScope.launch {
            val now = clock.now()
            val comment = NoteComment(
                noteId = currentNoteId,
                content = text,
                createdAt = now,
                updatedAt = now
            )
            noteCommentRepository.insertComment(comment)
                .onSuccess {
                    _commentText.value = ""
                    loadComments(currentNoteId)
                }
                .onFailure { error ->
                    Timber.w("Failed to add comment: $error")
                    snackbarController.showMessage(R.string.note_comment_save_failed)
                }
        }
    }

    fun deleteComment(commentId: Long) {
        val currentNoteId = noteId ?: return
        viewModelScope.launch {
            noteCommentRepository.deleteComment(commentId)
                .onSuccess {
                    loadComments(currentNoteId)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete comment: $error")
                    snackbarController.showMessage(R.string.note_comment_delete_failed)
                }
        }
    }

    private fun loadComments(noteId: Long) {
        viewModelScope.launch {
            noteCommentRepository.getCommentsForNote(noteId).collect { comments ->
                _comments.value = comments
            }
        }
    }

    fun saveNote() {
        val current = _formState.value

        val titleError = combineValidations(
            validateRequired(current.title, R.string.notes_title_required),
            validateMaxLength(current.title, AppConfig.Note.TITLE_MAX_LENGTH)
        )
        val contentError = combineValidations(
            validateRequired(current.content, R.string.notes_content_required),
            validateMaxLength(current.content, AppConfig.Note.CONTENT_MAX_LENGTH)
        )

        if (titleError != null || contentError != null) {
            _formState.value = current.copy(
                titleError = titleError,
                contentError = contentError
            )
            return
        }

        _formState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val now = clock.now()
            val original = originalNote
            if (noteId != null && original != null) {
                val updatedNote = original.copy(
                    title = current.title.trim(),
                    content = current.content.trim(),
                    tag = current.tag,
                    updatedAt = now
                )
                noteRepository.updateNote(updatedNote)
                    .onSuccess {
                        Timber.d("Note updated: id=$noteId")
                        analyticsRepository.logEvent(AppConfig.Analytics.EVENT_NOTE_UPDATED)
                        _savedEvent.send(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to update note: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                        snackbarController.showMessage(R.string.notes_save_failed)
                    }
            } else {
                val newNote = Note(
                    title = current.title.trim(),
                    content = current.content.trim(),
                    tag = current.tag,
                    createdAt = now,
                    updatedAt = now
                )
                noteRepository.insertNote(newNote)
                    .onSuccess { id ->
                        Timber.d("Note saved: id=$id")
                        analyticsRepository.logEvent(AppConfig.Analytics.EVENT_NOTE_CREATED)
                        photoManager.updateParentId(id)
                        _savedEvent.send(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to save note: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                        snackbarController.showMessage(R.string.notes_save_failed)
                    }
            }
        }
    }

}
