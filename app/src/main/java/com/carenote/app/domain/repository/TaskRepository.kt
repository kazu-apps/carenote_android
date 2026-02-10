package com.carenote.app.domain.repository

import androidx.paging.PagingData
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {

    fun getAllTasks(): Flow<List<Task>>

    fun getTaskById(id: Long): Flow<Task?>

    fun getIncompleteTasks(): Flow<List<Task>>

    fun getTasksByDueDate(date: LocalDate): Flow<List<Task>>

    fun getPagedAllTasks(query: String): Flow<PagingData<Task>>

    fun getPagedIncompleteTasks(query: String): Flow<PagingData<Task>>

    fun getPagedCompletedTasks(query: String): Flow<PagingData<Task>>

    fun getIncompleteTaskCount(): Flow<Int>

    suspend fun insertTask(task: Task): Result<Long, DomainError>

    suspend fun updateTask(task: Task): Result<Unit, DomainError>

    suspend fun deleteTask(id: Long): Result<Unit, DomainError>
}
