package com.carenote.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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

    private val _refreshTrigger = MutableStateFlow(0L)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val notes: StateFlow<UiState<List<Note>>> =
        combine(
            _searchQuery.debounce(AppConfig.UI.SEARCH_DEBOUNCE_MS),
            _selectedTag,
            _refreshTrigger
        ) { query, tag, _ -> query to tag }
            .flatMapLatest { (query, tag) ->
                noteRepository.searchNotes(query, tag)
            }
            .map { notes ->
                @Suppress("USELESS_CAST")
                UiState.Success(notes) as UiState<List<Note>>
            }
            .catch { e ->
                Timber.w("Failed to observe notes: $e")
                emit(UiState.Error(DomainError.DatabaseError(e.message ?: "Unknown error")))
            }
            .onEach { _isRefreshing.value = false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    fun refresh() {
        _isRefreshing.value = true
        _refreshTrigger.value = System.nanoTime()
    }

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
                    snackbarController.showMessage(R.string.notes_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete note: $error")
                    snackbarController.showMessage(R.string.notes_delete_failed)
                }
        }
    }

}
