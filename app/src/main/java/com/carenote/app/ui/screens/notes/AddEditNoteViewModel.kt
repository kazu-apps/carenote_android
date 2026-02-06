package com.carenote.app.ui.screens.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Note
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.ui.common.UiText
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
import java.time.LocalDateTime
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
    private val noteRepository: NoteRepository
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
            return current != baseline
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
            }
        }
    }

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

    fun saveNote() {
        val current = _formState.value

        val titleError = if (current.title.isBlank()) {
            UiText.Resource(R.string.notes_title_required)
        } else if (current.title.length > AppConfig.Note.TITLE_MAX_LENGTH) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Note.TITLE_MAX_LENGTH)
            )
        } else {
            null
        }
        val contentError = if (current.content.isBlank()) {
            UiText.Resource(R.string.notes_content_required)
        } else if (current.content.length > AppConfig.Note.CONTENT_MAX_LENGTH) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Note.CONTENT_MAX_LENGTH)
            )
        } else {
            null
        }

        if (titleError != null || contentError != null) {
            _formState.value = current.copy(
                titleError = titleError,
                contentError = contentError
            )
            return
        }

        _formState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val now = LocalDateTime.now()
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
