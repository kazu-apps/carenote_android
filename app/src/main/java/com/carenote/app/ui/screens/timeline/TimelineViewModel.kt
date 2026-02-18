package com.carenote.app.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.model.TimelineFilterType
import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.TimelineRepository
import com.carenote.app.domain.util.Clock
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(clock.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _filterType = MutableStateFlow(TimelineFilterType.ALL)
    val filterType: StateFlow<TimelineFilterType> = _filterType.asStateFlow()

    val snackbarController = SnackbarController()

    @OptIn(ExperimentalCoroutinesApi::class)
    val timelineItems: StateFlow<UiState<List<TimelineItem>>> =
        combine(_selectedDate, _refreshTrigger, _filterType) { date, _, filter -> date to filter }
            .flatMapLatest { (date, filter) ->
                timelineRepository.getTimelineItemsForDate(date)
                    .map { items -> filter to items }
            }
            .map { (filter, items) ->
                val filtered = when (filter) {
                    TimelineFilterType.ALL -> items
                    TimelineFilterType.TASK -> items.filter { it is TimelineItem.CalendarEventItem && it.event.isTask }
                    TimelineFilterType.EVENT -> items.filter { it is TimelineItem.CalendarEventItem && !it.event.isTask }
                    TimelineFilterType.MEDICATION -> items.filterIsInstance<TimelineItem.MedicationLogItem>()
                    TimelineFilterType.HEALTH_RECORD -> items.filterIsInstance<TimelineItem.HealthRecordItem>()
                    TimelineFilterType.NOTE -> items.filterIsInstance<TimelineItem.NoteItem>()
                }
                @Suppress("USELESS_CAST")
                UiState.Success(filtered) as UiState<List<TimelineItem>>
            }
            .catch { e ->
                Timber.w("Failed to observe timeline items: $e")
                emit(UiState.Error(DomainError.DatabaseError(e.message ?: "Unknown error")))
            }
            .onEach { _isRefreshing.value = false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun goToToday() {
        _selectedDate.value = clock.today()
    }

    fun refresh() {
        _isRefreshing.value = true
        _refreshTrigger.value = System.nanoTime()
    }

    fun setFilter(type: TimelineFilterType) {
        _filterType.value = type
    }

    fun toggleCompleted(eventId: Long, newCompleted: Boolean) {
        viewModelScope.launch {
            val event = calendarEventRepository.getEventById(eventId).firstOrNull()
            if (event == null) {
                Timber.w("Failed to toggle task completion: event not found id=$eventId")
                return@launch
            }
            val updatedEvent = event.copy(
                completed = newCompleted,
                updatedAt = clock.now()
            )
            calendarEventRepository.updateEvent(updatedEvent)
                .onSuccess {
                    Timber.d("Task completion toggled: id=$eventId")
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_CALENDAR_EVENT_COMPLETED)
                }
                .onFailure { error ->
                    Timber.w("Failed to toggle task completion: $error")
                    snackbarController.showMessage(R.string.timeline_toggle_failed)
                }
        }
    }
}
