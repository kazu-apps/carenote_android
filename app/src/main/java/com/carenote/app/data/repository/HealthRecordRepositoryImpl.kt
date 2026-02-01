package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.mapper.HealthRecordMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.repository.HealthRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRecordRepositoryImpl @Inject constructor(
    private val healthRecordDao: HealthRecordDao,
    private val mapper: HealthRecordMapper
) : HealthRecordRepository {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun getAllRecords(): Flow<List<HealthRecord>> {
        return healthRecordDao.getAllRecords().map { entities ->
            mapper.toDomainList(entities)
        }
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
        return healthRecordDao.getRecordsByDateRange(
            start = start.format(dateTimeFormatter),
            end = end.format(dateTimeFormatter)
        ).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun insertRecord(record: HealthRecord): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert health record", it) }
        ) {
            healthRecordDao.insertRecord(mapper.toEntity(record))
        }
    }

    override suspend fun updateRecord(record: HealthRecord): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update health record", it) }
        ) {
            healthRecordDao.updateRecord(mapper.toEntity(record))
        }
    }

    override suspend fun deleteRecord(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete health record", it) }
        ) {
            healthRecordDao.deleteRecord(id)
        }
    }
}
