package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.mapper.MedicationLogMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.repository.MedicationLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationLogRepositoryImpl @Inject constructor(
    private val medicationLogDao: MedicationLogDao,
    private val mapper: MedicationLogMapper
) : MedicationLogRepository {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>> {
        return medicationLogDao.getLogsForMedication(medicationId).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getLogsForDate(date: LocalDate): Flow<List<MedicationLog>> {
        val startOfDay = date.atTime(LocalTime.MIN).format(dateTimeFormatter)
        val endOfDay = date.plusDays(1).atTime(LocalTime.MIN).format(dateTimeFormatter)
        return medicationLogDao.getLogsForDateRange(startOfDay, endOfDay).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun insertLog(log: MedicationLog): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert medication log", it) }
        ) {
            medicationLogDao.insertLog(mapper.toEntity(log))
        }
    }

    override suspend fun updateLog(log: MedicationLog): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update medication log", it) }
        ) {
            medicationLogDao.updateLog(mapper.toEntity(log))
        }
    }

    override suspend fun deleteLog(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete medication log", it) }
        ) {
            medicationLogDao.deleteLog(id)
        }
    }

    override suspend fun hasLogForMedicationToday(
        medicationId: Long,
        timing: MedicationTiming?
    ): Boolean {
        val today = LocalDate.now()
        val startOfDay = today.atTime(LocalTime.MIN).format(dateTimeFormatter)
        val endOfDay = today.plusDays(1).atTime(LocalTime.MIN).format(dateTimeFormatter)
        return medicationLogDao.hasTakenLogForDateRange(
            medicationId = medicationId,
            startOfDay = startOfDay,
            endOfDay = endOfDay,
            timing = timing?.name
        )
    }
}
