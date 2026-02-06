package com.carenote.app.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.repository.CalendarEventRepository
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
    val titleError: UiText? = null,
    val descriptionError: UiText? = null,
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

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

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
        _formState.value = _formState.value.copy(
            description = description,
            descriptionError = null
        )
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

        val titleError = if (current.title.isBlank()) {
            UiText.Resource(R.string.calendar_event_title_required)
        } else if (current.title.length > AppConfig.Calendar.TITLE_MAX_LENGTH) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Calendar.TITLE_MAX_LENGTH)
            )
        } else {
            null
        }
        val descriptionError = if (
            current.description.length > AppConfig.Calendar.DESCRIPTION_MAX_LENGTH
        ) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Calendar.DESCRIPTION_MAX_LENGTH)
            )
        } else {
            null
        }

        if (titleError != null || descriptionError != null) {
            _formState.value = current.copy(
                titleError = titleError,
                descriptionError = descriptionError
            )
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
                        _savedEvent.send(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to update calendar event: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                        snackbarController.showMessage(R.string.calendar_event_save_failed)
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
                        _savedEvent.send(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to save calendar event: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                        snackbarController.showMessage(R.string.calendar_event_save_failed)
                    }
            }
        }
    }

}
