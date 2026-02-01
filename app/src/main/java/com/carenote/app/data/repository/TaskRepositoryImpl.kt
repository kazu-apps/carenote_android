package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.mapper.TaskMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val mapper: TaskMapper
) : TaskRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getTaskById(id: Long): Flow<Task?> {
        return taskDao.getTaskById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getIncompleteTasks(): Flow<List<Task>> {
        return taskDao.getIncompleteTasks().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getTasksByDueDate(date: LocalDate): Flow<List<Task>> {
        return taskDao.getTasksByDueDate(
            date = date.format(dateFormatter)
        ).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun insertTask(task: Task): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert task", it) }
        ) {
            taskDao.insertTask(mapper.toEntity(task))
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update task", it) }
        ) {
            taskDao.updateTask(mapper.toEntity(task))
        }
    }

    override suspend fun deleteTask(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete task", it) }
        ) {
            taskDao.deleteTask(id)
        }
    }
}
