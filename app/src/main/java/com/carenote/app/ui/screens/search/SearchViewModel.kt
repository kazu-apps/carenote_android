package com.carenote.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.model.SearchResult
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.SearchRepository
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<List<SearchResult>>> =
        _searchQuery
            .debounce(AppConfig.UI.SEARCH_DEBOUNCE_MS)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(UiState.Success(emptyList()))
                } else {
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_SEARCH_PERFORMED)
                    searchRepository.searchAll(query)
                        .map { results ->
                            UiState.Success(results) as UiState<List<SearchResult>>
                        }
                }
            }
            .catch { e ->
                Timber.w("Search failed: $e")
                emit(UiState.Error(DomainError.DatabaseError(e.message ?: "Unknown error")))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = UiState.Success(emptyList())
            )
}
