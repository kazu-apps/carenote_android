package com.carenote.app.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.domain.repository.TaskRepository
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
import javax.inject.Inject

data class AddEditTaskFormState(
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val titleError: String? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val taskId: Long? = savedStateHandle.get<Long>("taskId")

    private val _formState = MutableStateFlow(
        AddEditTaskFormState(isEditMode = taskId != null)
    )
    val formState: StateFlow<AddEditTaskFormState> = _formState.asStateFlow()

    private val _savedTask = MutableSharedFlow<Boolean>(replay = 1)
    val savedTask: SharedFlow<Boolean> = _savedTask.asSharedFlow()

    private var originalTask: Task? = null

    init {
        if (taskId != null) {
            loadTask(taskId)
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
                    priority = task.priority
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

    fun updateDueDate(dueDate: LocalDate?) {
        _formState.value = _formState.value.copy(dueDate = dueDate)
    }

    fun updatePriority(priority: TaskPriority) {
        _formState.value = _formState.value.copy(priority = priority)
    }

    fun saveTask() {
        val current = _formState.value

        val titleError = if (current.title.isBlank()) TITLE_REQUIRED_ERROR else null

        if (titleError != null) {
            _formState.value = current.copy(titleError = titleError)
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
                    updatedAt = now
                )
                taskRepository.updateTask(updatedTask)
                    .onSuccess {
                        Timber.d("Task updated: id=$taskId")
                        _savedTask.emit(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to update task: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                    }
            } else {
                val newTask = Task(
                    title = current.title.trim(),
                    description = current.description.trim(),
                    dueDate = current.dueDate,
                    priority = current.priority,
                    createdAt = now,
                    updatedAt = now
                )
                taskRepository.insertTask(newTask)
                    .onSuccess { id ->
                        Timber.d("Task saved: id=$id")
                        _savedTask.emit(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to save task: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                    }
            }
        }
    }

    companion object {
        const val TITLE_REQUIRED_ERROR = "タイトルを入力してください"
    }
}
