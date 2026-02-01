package com.carenote.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<NoteTag?>(null)
    val selectedTag: StateFlow<NoteTag?> = _selectedTag.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val notes: StateFlow<UiState<List<Note>>> =
        combine(
            _searchQuery.debounce(AppConfig.UI.SEARCH_DEBOUNCE_MS),
            _selectedTag
        ) { query, tag -> query to tag }
            .flatMapLatest { (query, tag) ->
                noteRepository.searchNotes(query, tag)
            }
            .map { notes ->
                @Suppress("USELESS_CAST")
                UiState.Success(notes) as UiState<List<Note>>
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTag(tag: NoteTag?) {
        _selectedTag.value = tag
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            noteRepository.deleteNote(id)
                .onSuccess {
                    Timber.d("Note deleted: id=$id")
                    snackbarController.showMessage(SNACKBAR_DELETED)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete note: $error")
                    snackbarController.showMessage(SNACKBAR_DELETE_FAILED)
                }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        const val SNACKBAR_DELETED = "メモを削除しました"
        const val SNACKBAR_DELETE_FAILED = "削除に失敗しました"
    }
}
