package com.carenote.app.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.TaskReminderSchedulerInterface
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.Task
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.domain.repository.TaskRepository
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

data class AddEditTaskFormState(
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val recurrenceInterval: Int = AppConfig.Task.DEFAULT_RECURRENCE_INTERVAL,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime? = null,
    val titleError: UiText? = null,
    val descriptionError: UiText? = null,
    val recurrenceIntervalError: UiText? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val taskReminderScheduler: TaskReminderSchedulerInterface
) : ViewModel() {

    private val taskId: Long? = savedStateHandle.get<Long>("taskId")

    private val _formState = MutableStateFlow(
        AddEditTaskFormState(isEditMode = taskId != null)
    )
    val formState: StateFlow<AddEditTaskFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private var originalTask: Task? = null
    private var _initialFormState: AddEditTaskFormState? = null

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
        if (taskId != null) {
            loadTask(taskId)
        } else {
            _initialFormState = _formState.value
        }
    }

    private fun loadTask(id: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(id).firstOrNull()
            if (task != null) {
                originalTask = task
                _formState.value = _formState.value.copy(
                    title = task.title,
                    description = task.description,
                    dueDate = task.dueDate,
                    priority = task.priority,
                    recurrenceFrequency = task.recurrenceFrequency,
                    recurrenceInterval = task.recurrenceInterval,
                    reminderEnabled = task.reminderEnabled,
                    reminderTime = task.reminderTime
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

    fun updateDueDate(dueDate: LocalDate?) {
        _formState.value = _formState.value.copy(dueDate = dueDate)
    }

    fun updatePriority(priority: TaskPriority) {
        _formState.value = _formState.value.copy(priority = priority)
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

    fun toggleReminder() {
        _formState.value = _formState.value.copy(
            reminderEnabled = !_formState.value.reminderEnabled
        )
    }

    fun updateReminderTime(time: LocalTime?) {
        _formState.value = _formState.value.copy(reminderTime = time)
    }

    fun saveTask() {
        val current = _formState.value

        val titleError = if (current.title.isBlank()) {
            UiText.Resource(R.string.tasks_task_title_required)
        } else if (current.title.length > AppConfig.Task.TITLE_MAX_LENGTH) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Task.TITLE_MAX_LENGTH)
            )
        } else {
            null
        }
        val descriptionError = if (
            current.description.length > AppConfig.Task.DESCRIPTION_MAX_LENGTH
        ) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Task.DESCRIPTION_MAX_LENGTH)
            )
        } else {
            null
        }
        val recurrenceIntervalError = if (
            current.recurrenceFrequency != RecurrenceFrequency.NONE &&
            (current.recurrenceInterval < 1 ||
                current.recurrenceInterval > AppConfig.Task.MAX_RECURRENCE_INTERVAL)
        ) {
            UiText.Resource(R.string.tasks_recurrence_interval_error)
        } else {
            null
        }

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
            val now = LocalDateTime.now()
            val original = originalTask
            if (taskId != null && original != null) {
                val updatedTask = original.copy(
                    title = current.title.trim(),
                    description = current.description.trim(),
                    dueDate = current.dueDate,
                    priority = current.priority,
                    recurrenceFrequency = current.recurrenceFrequency,
                    recurrenceInterval = current.recurrenceInterval,
                    reminderEnabled = current.reminderEnabled,
                    reminderTime = current.reminderTime,
                    updatedAt = now
                )
                taskRepository.updateTask(updatedTask)
                    .onSuccess {
                        Timber.d("Task updated: id=$taskId")
                        scheduleOrCancelReminder(
                            taskId,
                            current.title.trim(),
                            current.reminderEnabled,
                            current.reminderTime
                        )
                        _savedEvent.send(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to update task: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                        snackbarController.showMessage(R.string.tasks_save_failed)
                    }
            } else {
                val newTask = Task(
                    title = current.title.trim(),
                    description = current.description.trim(),
                    dueDate = current.dueDate,
                    priority = current.priority,
                    recurrenceFrequency = current.recurrenceFrequency,
                    recurrenceInterval = current.recurrenceInterval,
                    reminderEnabled = current.reminderEnabled,
                    reminderTime = current.reminderTime,
                    createdAt = now,
                    updatedAt = now
                )
                taskRepository.insertTask(newTask)
                    .onSuccess { id ->
                        Timber.d("Task saved: id=$id")
                        scheduleOrCancelReminder(
                            id,
                            current.title.trim(),
                            current.reminderEnabled,
                            current.reminderTime
                        )
                        _savedEvent.send(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to save task: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                        snackbarController.showMessage(R.string.tasks_save_failed)
                    }
            }
        }
    }

    private fun scheduleOrCancelReminder(
        id: Long,
        title: String,
        enabled: Boolean,
        time: LocalTime?
    ) {
        taskReminderScheduler.cancelReminder(id)
        if (enabled && time != null) {
            taskReminderScheduler.scheduleReminder(id, title, time)
        }
    }
}
