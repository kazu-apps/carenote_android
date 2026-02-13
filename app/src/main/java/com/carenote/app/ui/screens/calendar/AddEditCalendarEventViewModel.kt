package com.carenote.app.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.util.Clock
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.repository.CalendarEventRepository
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class AddEditCalendarEventFormState(
    val title: String = "",
    val description: String = "",
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isAllDay: Boolean = true,
    val type: CalendarEventType = CalendarEventType.OTHER,
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val recurrenceInterval: Int = 1,
    val titleError: UiText? = null,
    val descriptionError: UiText? = null,
    val recurrenceIntervalError: UiText? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditCalendarEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val calendarEventRepository: CalendarEventRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock
) : ViewModel() {

    private val eventId: Long? = savedStateHandle.get<Long>("eventId")

    private val _formState = MutableStateFlow(
        AddEditCalendarEventFormState(date = clock.today(), isEditMode = eventId != null)
    )
    val formState: StateFlow<AddEditCalendarEventFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private var originalEvent: CalendarEvent? = null
    private var _initialFormState: AddEditCalendarEventFormState? = null

    val isDirty: Boolean
        get() {
            val initial = _initialFormState ?: return false
            val current = _formState.value.copy(
                titleError = null,
                descriptionError = null,
                recurrenceIntervalError = null,
                isSaving = false,
                isEditMode = false
            )
            val baseline = initial.copy(
                titleError = null,
                descriptionError = null,
                recurrenceIntervalError = null,
                isSaving = false,
                isEditMode = false
            )
            return current != baseline
        }

    init {
        if (eventId != null) {
            loadEvent(eventId)
        } else {
            _initialFormState = _formState.value
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
                    isAllDay = event.isAllDay,
                    type = event.type,
                    recurrenceFrequency = event.recurrenceFrequency,
                    recurrenceInterval = event.recurrenceInterval
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

    fun updateType(type: CalendarEventType) {
        _formState.value = _formState.value.copy(type = type)
    }

    fun updateRecurrenceFrequency(frequency: RecurrenceFrequency) {
        _formState.value = _formState.value.copy(
            recurrenceFrequency = frequency,
            recurrenceIntervalError = null
        )
    }

    fun updateRecurrenceInterval(interval: Int) {
        _formState.value = _formState.value.copy(
            recurrenceInterval = interval,
            recurrenceIntervalError = null
        )
    }

    fun saveEvent() {
        val current = _formState.value

        val titleError = combineValidations(
            validateRequired(current.title, R.string.calendar_event_title_required),
            validateMaxLength(current.title, AppConfig.Calendar.TITLE_MAX_LENGTH)
        )
        val descriptionError = validateMaxLength(
            current.description, AppConfig.Calendar.DESCRIPTION_MAX_LENGTH
        )
        val recurrenceIntervalError = if (current.recurrenceFrequency != RecurrenceFrequency.NONE) {
            if (current.recurrenceInterval < 1 || current.recurrenceInterval > AppConfig.Task.MAX_RECURRENCE_INTERVAL) {
                UiText.Resource(R.string.tasks_recurrence_interval_error)
            } else null
        } else null

        if (titleError != null || descriptionError != null || recurrenceIntervalError != null) {
            _formState.value = current.copy(
                titleError = titleError,
                descriptionError = descriptionError,
                recurrenceIntervalError = recurrenceIntervalError
            )
            return
        }

        _formState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val now = clock.now()
            val original = originalEvent
            if (eventId != null && original != null) {
                val updatedEvent = original.copy(
                    title = current.title.trim(),
                    description = current.description.trim(),
                    date = current.date,
                    startTime = current.startTime,
                    endTime = current.endTime,
                    isAllDay = current.isAllDay,
                    type = current.type,
                    recurrenceFrequency = current.recurrenceFrequency,
                    recurrenceInterval = current.recurrenceInterval,
                    updatedAt = now
                )
                calendarEventRepository.updateEvent(updatedEvent)
                    .onSuccess {
                        Timber.d("Calendar event updated: id=$eventId")
                        analyticsRepository.logEvent(AppConfig.Analytics.EVENT_CALENDAR_EVENT_UPDATED)
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
                    type = current.type,
                    recurrenceFrequency = current.recurrenceFrequency,
                    recurrenceInterval = current.recurrenceInterval,
                    createdAt = now,
                    updatedAt = now
                )
                calendarEventRepository.insertEvent(newEvent)
                    .onSuccess { id ->
                        Timber.d("Calendar event saved: id=$id")
                        analyticsRepository.logEvent(AppConfig.Analytics.EVENT_CALENDAR_EVENT_CREATED)
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
