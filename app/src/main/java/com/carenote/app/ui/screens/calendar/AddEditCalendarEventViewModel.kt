package com.carenote.app.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.repository.CalendarEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class AddEditCalendarEventFormState(
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isAllDay: Boolean = true,
    val titleError: String? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditCalendarEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val calendarEventRepository: CalendarEventRepository
) : ViewModel() {

    private val eventId: Long? = savedStateHandle.get<Long>("eventId")

    private val _formState = MutableStateFlow(
        AddEditCalendarEventFormState(isEditMode = eventId != null)
    )
    val formState: StateFlow<AddEditCalendarEventFormState> = _formState.asStateFlow()

    private val _savedEvent = MutableSharedFlow<Boolean>(replay = 1)
    val savedEvent: SharedFlow<Boolean> = _savedEvent.asSharedFlow()

    private var originalEvent: CalendarEvent? = null

    init {
        if (eventId != null) {
            loadEvent(eventId)
        }
    }

    private fun loadEvent(id: Long) {
        viewModelScope.launch {
            val event = calendarEventRepository.getEventById(id).firstOrNull()
            if (event != null) {
                originalEvent = event
                _formState.value = _formState.value.copy(
                    title = event.title,
                    description = event.description,
                    date = event.date,
                    startTime = event.startTime,
                    endTime = event.endTime,
                    isAllDay = event.isAllDay
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _formState.value = _formState.value.copy(
            title = title,
            titleError = null
        )
    }

    fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }

    fun updateDate(date: LocalDate) {
        _formState.value = _formState.value.copy(date = date)
    }

    fun updateStartTime(time: LocalTime?) {
        _formState.value = _formState.value.copy(startTime = time)
    }

    fun updateEndTime(time: LocalTime?) {
        _formState.value = _formState.value.copy(endTime = time)
    }

    fun toggleAllDay() {
        _formState.value = _formState.value.copy(
            isAllDay = !_formState.value.isAllDay
        )
    }

    fun saveEvent() {
        val current = _formState.value

        val titleError = if (current.title.isBlank()) TITLE_REQUIRED_ERROR else null

        if (titleError != null) {
            _formState.value = current.copy(titleError = titleError)
            return
        }

        _formState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val now = LocalDateTime.now()
            val original = originalEvent
            if (eventId != null && original != null) {
                val updatedEvent = original.copy(
                    title = current.title.trim(),
                    description = current.description.trim(),
                    date = current.date,
                    startTime = current.startTime,
                    endTime = current.endTime,
                    isAllDay = current.isAllDay,
                    updatedAt = now
                )
                calendarEventRepository.updateEvent(updatedEvent)
                    .onSuccess {
                        Timber.d("Calendar event updated: id=$eventId")
                        _savedEvent.emit(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to update calendar event: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                    }
            } else {
                val newEvent = CalendarEvent(
                    title = current.title.trim(),
                    description = current.description.trim(),
                    date = current.date,
                    startTime = current.startTime,
                    endTime = current.endTime,
                    isAllDay = current.isAllDay,
                    createdAt = now,
                    updatedAt = now
                )
                calendarEventRepository.insertEvent(newEvent)
                    .onSuccess { id ->
                        Timber.d("Calendar event saved: id=$id")
                        _savedEvent.emit(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to save calendar event: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                    }
            }
        }
    }

    companion object {
        const val TITLE_REQUIRED_ERROR = "タイトルを入力してください"
    }
}
