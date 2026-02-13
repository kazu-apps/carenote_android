package com.carenote.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.mapper.TaskMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val mapper: TaskMapper,
    private val activeRecipientProvider: ActiveCareRecipientProvider,
    private val authRepository: AuthRepository
) : TaskRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val pagingConfig = PagingConfig(pageSize = AppConfig.Paging.PAGE_SIZE)

    override fun getAllTasks(): Flow<List<Task>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            taskDao.getAllTasks(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getTaskById(id: Long): Flow<Task?> {
        return taskDao.getTaskById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getIncompleteTasks(): Flow<List<Task>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            taskDao.getIncompleteTasks(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getTasksByDueDate(date: LocalDate): Flow<List<Task>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            taskDao.getTasksByDueDate(
                date = date.format(dateFormatter),
                careRecipientId = recipientId
            )
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getPagedAllTasks(query: String): Flow<PagingData<Task>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            Pager(pagingConfig) { taskDao.getPagedAllTasks(query, recipientId) }
                .flow.map { pagingData -> pagingData.map { mapper.toDomain(it) } }
        }
    }

    override fun getPagedIncompleteTasks(query: String): Flow<PagingData<Task>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            Pager(pagingConfig) { taskDao.getPagedIncompleteTasks(query, recipientId) }
                .flow.map { pagingData -> pagingData.map { mapper.toDomain(it) } }
        }
    }

    override fun getPagedCompletedTasks(query: String): Flow<PagingData<Task>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            Pager(pagingConfig) { taskDao.getPagedCompletedTasks(query, recipientId) }
                .flow.map { pagingData -> pagingData.map { mapper.toDomain(it) } }
        }
    }

    override fun getIncompleteTaskCount(): Flow<Int> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            taskDao.getIncompleteTaskCount(recipientId)
        }
    }

    override suspend fun insertTask(task: Task): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert task", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            val createdBy = authRepository.getCurrentUser()?.uid ?: ""
            taskDao.insertTask(mapper.toEntity(task).copy(careRecipientId = recipientId, createdBy = createdBy))
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update task", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            taskDao.updateTask(mapper.toEntity(task).copy(careRecipientId = recipientId))
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
