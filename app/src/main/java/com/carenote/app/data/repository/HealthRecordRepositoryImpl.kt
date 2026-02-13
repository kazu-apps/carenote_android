package com.carenote.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.mapper.HealthRecordMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.domain.repository.PhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import timber.log.Timber
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class HealthRecordRepositoryImpl @Inject constructor(
    private val healthRecordDao: HealthRecordDao,
    private val mapper: HealthRecordMapper,
    private val photoRepository: PhotoRepository,
    private val activeRecipientProvider: ActiveCareRecipientProvider,
    private val authRepository: AuthRepository
) : HealthRecordRepository {

    private val pagingConfig = PagingConfig(pageSize = AppConfig.Paging.PAGE_SIZE)
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun getAllRecords(): Flow<List<HealthRecord>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            healthRecordDao.getAllRecords(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getRecordById(id: Long): Flow<HealthRecord?> {
        return healthRecordDao.getRecordById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getRecordsByDateRange(
        start: LocalDateTime,
        end: LocalDateTime
    ): Flow<List<HealthRecord>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            healthRecordDao.getRecordsByDateRange(
                start = start.format(dateTimeFormatter),
                end = end.format(dateTimeFormatter),
                careRecipientId = recipientId
            )
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getPagedRecords(query: String): Flow<PagingData<HealthRecord>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            Pager(pagingConfig) { healthRecordDao.getPagedRecords(query, recipientId) }
                .flow.map { pagingData -> pagingData.map { mapper.toDomain(it) } }
        }
    }

    override suspend fun insertRecord(record: HealthRecord): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert health record", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            val createdBy = authRepository.getCurrentUser()?.uid ?: ""
            healthRecordDao.insertRecord(mapper.toEntity(record).copy(careRecipientId = recipientId, createdBy = createdBy))
        }
    }

    override suspend fun updateRecord(record: HealthRecord): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update health record", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            healthRecordDao.updateRecord(mapper.toEntity(record).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun deleteRecord(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete health record", it) }
        ) {
            photoRepository.deletePhotosForParent("health_record", id)
                .onFailure { error -> Timber.w("Failed to delete photos for health record $id: $error") }
            healthRecordDao.deleteRecord(id)
        }
    }
}
