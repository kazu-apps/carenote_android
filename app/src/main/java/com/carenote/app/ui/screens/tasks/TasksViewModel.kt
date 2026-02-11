package com.carenote.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.TaskReminderSchedulerInterface
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.repository.TaskRepository
import com.carenote.app.domain.util.Clock
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
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskReminderScheduler: TaskReminderSchedulerInterface,
    private val clock: Clock
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterMode = MutableStateFlow(TaskFilterMode.ALL)
    val filterMode: StateFlow<TaskFilterMode> = _filterMode.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val tasks: Flow<PagingData<Task>> =
        combine(
            _searchQuery.debounce(AppConfig.UI.SEARCH_DEBOUNCE_MS),
            _filterMode
        ) { query, mode -> query to mode }
            .flatMapLatest { (query, mode) ->
                when (mode) {
                    TaskFilterMode.ALL -> taskRepository.getPagedAllTasks(query)
                    TaskFilterMode.INCOMPLETE -> taskRepository.getPagedIncompleteTasks(query)
                    TaskFilterMode.COMPLETED -> taskRepository.getPagedCompletedTasks(query)
                }
            }
            .cachedIn(viewModelScope)

    fun setFilterMode(mode: TaskFilterMode) {
        _filterMode.value = mode
    }

    fun toggleCompletion(task: Task) {
        viewModelScope.launch {
            val completing = !task.isCompleted
            val updatedTask = task.copy(
                isCompleted = completing,
                updatedAt = clock.now()
            )
            taskRepository.updateTask(updatedTask)
                .onSuccess {
                    Timber.d("Task completion toggled: id=${task.id}, completed=$completing")
                    if (completing) {
                        taskReminderScheduler.cancelReminder(task.id)
                        taskReminderScheduler.cancelFollowUp(task.id)
                        generateNextRecurringTask(task)
                    } else {
                        if (task.reminderEnabled && task.reminderTime != null) {
                            taskReminderScheduler.scheduleReminder(
                                task.id,
                                task.title,
                                task.reminderTime
                            )
                        }
                    }
                }
                .onFailure { error ->
                    Timber.w("Failed to toggle task completion: $error")
                    snackbarController.showMessage(R.string.tasks_toggle_failed)
                }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            taskReminderScheduler.cancelReminder(id)
            taskReminderScheduler.cancelFollowUp(id)
            taskRepository.deleteTask(id)
                .onSuccess {
                    Timber.d("Task deleted: id=$id")
                    snackbarController.showMessage(R.string.tasks_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete task: $error")
                    snackbarController.showMessage(R.string.tasks_delete_failed)
                }
        }
    }

    private suspend fun generateNextRecurringTask(task: Task) {
        if (task.recurrenceFrequency == RecurrenceFrequency.NONE) return
        val baseDueDate = task.dueDate ?: return

        val nextDueDate = calculateNextDueDate(
            baseDueDate,
            task.recurrenceFrequency,
            task.recurrenceInterval
        )
        val now = clock.now()
        val nextTask = task.copy(
            id = 0,
            isCompleted = false,
            dueDate = nextDueDate,
            createdAt = now,
            updatedAt = now
        )
        taskRepository.insertTask(nextTask)
            .onSuccess { newId ->
                Timber.d("Next recurring task created: id=$newId, dueDate=$nextDueDate")
                if (nextTask.reminderEnabled && nextTask.reminderTime != null) {
                    taskReminderScheduler.scheduleReminder(
                        newId,
                        nextTask.title,
                        nextTask.reminderTime
                    )
                }
            }
            .onFailure { error ->
                Timber.w("Failed to create next recurring task: $error")
            }
    }

    companion object {
        fun calculateNextDueDate(
            current: LocalDate,
            frequency: RecurrenceFrequency,
            interval: Int
        ): LocalDate = when (frequency) {
            RecurrenceFrequency.NONE -> current
            RecurrenceFrequency.DAILY -> current.plusDays(interval.toLong())
            RecurrenceFrequency.WEEKLY -> current.plusWeeks(interval.toLong())
            RecurrenceFrequency.MONTHLY -> current.plusMonths(interval.toLong())
        }
    }
}
