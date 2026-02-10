package com.carenote.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.repository.CalendarEventRepository
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarEventRepository: CalendarEventRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsForSelectedDate: StateFlow<UiState<List<CalendarEvent>>> =
        combine(_selectedDate, _refreshTrigger) { date, _ -> date }
            .flatMapLatest { date ->
                calendarEventRepository.getEventsByDate(date)
            }
            .map { events ->
                // Required: stateIn needs explicit UiState<List<T>> type
                @Suppress("USELESS_CAST")
                UiState.Success(events) as UiState<List<CalendarEvent>>
            }
            .catch { e ->
                Timber.w("Failed to observe events for selected date: $e")
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsForMonth: StateFlow<Map<LocalDate, List<CalendarEvent>>> =
        _currentMonth
            .flatMapLatest { yearMonth ->
                val startDate = yearMonth.atDay(1)
                val endDate = yearMonth.atEndOfMonth()
                calendarEventRepository.getEventsByDateRange(startDate, endDate)
            }
            .map { events ->
                events.groupBy { it.date }
            }
            .distinctUntilChanged()
            .catch { e ->
                Timber.w("Failed to observe events for month: $e")
                emit(emptyMap())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = emptyMap()
            )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun changeMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            calendarEventRepository.deleteEvent(id)
                .onSuccess {
                    Timber.d("Calendar event deleted: id=$id")
                    snackbarController.showMessage(R.string.calendar_event_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete calendar event: $error")
                    snackbarController.showMessage(R.string.calendar_event_delete_failed)
                }
        }
    }

}
