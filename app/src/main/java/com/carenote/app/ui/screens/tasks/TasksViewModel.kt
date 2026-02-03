package com.carenote.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.repository.TaskRepository
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _filterMode = MutableStateFlow(TaskFilterMode.ALL)
    val filterMode: StateFlow<TaskFilterMode> = _filterMode.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<UiState<List<Task>>> =
        _filterMode
            .flatMapLatest { mode ->
                when (mode) {
                    TaskFilterMode.ALL -> taskRepository.getAllTasks()
                    TaskFilterMode.INCOMPLETE -> taskRepository.getIncompleteTasks()
                    TaskFilterMode.COMPLETED -> taskRepository.getAllTasks()
                        .map { list -> list.filter { it.isCompleted } }
                }
            }
            .map { taskList ->
                @Suppress("USELESS_CAST")
                UiState.Success(taskList) as UiState<List<Task>>
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    fun setFilterMode(mode: TaskFilterMode) {
        _filterMode.value = mode
    }

    fun toggleCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                isCompleted = !task.isCompleted,
                updatedAt = LocalDateTime.now()
            )
            taskRepository.updateTask(updatedTask)
                .onSuccess {
                    Timber.d("Task completion toggled: id=${task.id}, completed=${updatedTask.isCompleted}")
                }
                .onFailure { error ->
                    Timber.w("Failed to toggle task completion: $error")
                    snackbarController.showMessage(R.string.tasks_toggle_failed)
                }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
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

}
