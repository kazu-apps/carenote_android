package com.carenote.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<NoteTag?>(null)
    val selectedTag: StateFlow<NoteTag?> = _selectedTag.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val notes: Flow<PagingData<Note>> =
        combine(
            _searchQuery.debounce(AppConfig.UI.SEARCH_DEBOUNCE_MS),
            _selectedTag
        ) { query, tag -> query to tag }
            .flatMapLatest { (query, tag) ->
                noteRepository.searchPagedNotes(query, tag)
            }
            .cachedIn(viewModelScope)

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
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_NOTE_DELETED)
                    snackbarController.showMessage(R.string.notes_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete note: $error")
                    snackbarController.showMessage(R.string.notes_delete_failed)
                }
        }
    }

}
