package com.carenote.app.ui.screens.notes

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.ImageCompressorInterface
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.util.Clock
import com.carenote.app.domain.model.Photo
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.PhotoRepository
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.FormValidator.combineValidations
import com.carenote.app.ui.util.FormValidator.validateMaxLength
import com.carenote.app.ui.util.FormValidator.validateRequired
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
    private val noteRepository: NoteRepository,
    private val photoRepository: PhotoRepository,
    private val imageCompressor: ImageCompressorInterface,
    private val clock: Clock
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

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private var _initialPhotoCount = 0

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
            return _photos.value.size != _initialPhotoCount
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
                val existingPhotos = photoRepository.getPhotosForParent("note", id).firstOrNull().orEmpty()
                _photos.value = existingPhotos
                _initialPhotoCount = existingPhotos.size
            }
        }
    }

    fun addPhotos(uris: List<Uri>) {
        val remaining = AppConfig.Photo.MAX_PHOTOS_PER_PARENT - _photos.value.size
        if (remaining <= 0) return
        val toAdd = uris.take(remaining)
        viewModelScope.launch {
            for (uri in toAdd) {
                try {
                    val compressed = imageCompressor.compress(uri)
                    val now = clock.now()
                    val photo = Photo(
                        parentType = "note",
                        parentId = noteId ?: 0L,
                        localUri = compressed.toString(),
                        createdAt = now,
                        updatedAt = now
                    )
                    photoRepository.addPhoto(photo)
                        .onSuccess { id ->
                            _photos.value = _photos.value + photo.copy(id = id)
                        }
                        .onFailure { error ->
                            Timber.w("Failed to add photo: $error")
                            snackbarController.showMessage(R.string.photo_compress_failed)
                        }
                } catch (e: Exception) {
                    Timber.w("Failed to compress photo: $e")
                    snackbarController.showMessage(R.string.photo_compress_failed)
                }
            }
        }
    }

    fun removePhoto(photo: Photo) {
        viewModelScope.launch {
            photoRepository.deletePhoto(photo.id)
                .onSuccess {
                    _photos.value = _photos.value.filter { it.id != photo.id }
                }
                .onFailure { error ->
                    Timber.w("Failed to remove photo: $error")
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
                        updatePhotosParentId(id)
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

    private suspend fun updatePhotosParentId(newParentId: Long) {
        val photoIds = _photos.value
            .filter { it.parentId == 0L }
            .map { it.id }
        if (photoIds.isNotEmpty()) {
            photoRepository.updatePhotosParentId(photoIds, newParentId)
        }
    }
}
