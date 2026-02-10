package com.carenote.app.fakes

import androidx.paging.PagingData
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeTaskRepository : TaskRepository {

    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setTasks(list: List<Task>) {
        tasks.value = list
    }

    fun clear() {
        tasks.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    fun currentTasks(): List<Task> = tasks.value

    fun getFilteredTasks(query: String): List<Task> {
        return tasks.value.filter { task ->
            task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)
        }
    }

    override fun getIncompleteTaskCount(): Flow<Int> {
        return tasks.map { list -> list.count { !it.isCompleted } }
    }

    override fun getAllTasks(): Flow<List<Task>> = tasks

    override fun getTaskById(id: Long): Flow<Task?> {
        return tasks.map { list -> list.find { it.id == id } }
    }

    override fun getIncompleteTasks(): Flow<List<Task>> {
        return tasks.map { list -> list.filter { !it.isCompleted } }
    }

    override fun getTasksByDueDate(date: LocalDate): Flow<List<Task>> {
        return tasks.map { list -> list.filter { it.dueDate == date } }
    }

    override fun getPagedAllTasks(query: String): Flow<PagingData<Task>> {
        return tasks.map { list ->
            val filtered = if (query.isBlank()) list
            else list.filter { task ->
                task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            }
            PagingData.from(filtered)
        }
    }

    override fun getPagedIncompleteTasks(query: String): Flow<PagingData<Task>> {
        return tasks.map { list ->
            val filtered = list.filter { !it.isCompleted }.let { incomplete ->
                if (query.isBlank()) incomplete
                else incomplete.filter { task ->
                    task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true)
                }
            }
            PagingData.from(filtered)
        }
    }

    override fun getPagedCompletedTasks(query: String): Flow<PagingData<Task>> {
        return tasks.map { list ->
            val filtered = list.filter { it.isCompleted }.let { completed ->
                if (query.isBlank()) completed
                else completed.filter { task ->
                    task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true)
                }
            }
            PagingData.from(filtered)
        }
    }

    override suspend fun insertTask(task: Task): Result<Long, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        }
        val id = nextId++
        val newTask = task.copy(id = id)
        tasks.value = tasks.value + newTask
        return Result.Success(id)
    }

    override suspend fun updateTask(task: Task): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake update error"))
        }
        tasks.value = tasks.value.map {
            if (it.id == task.id) task else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteTask(id: Long): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        }
        tasks.value = tasks.value.filter { it.id != id }
        return Result.Success(Unit)
    }
}
