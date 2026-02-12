package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.repository.MedicationLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeMedicationLogRepository : MedicationLogRepository {

    private val logs = MutableStateFlow<List<MedicationLog>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setLogs(list: List<MedicationLog>) {
        logs.value = list
    }

    fun clear() {
        logs.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>> {
        return logs.map { list -> list.filter { it.medicationId == medicationId } }
    }

    override fun getLogsForDate(date: LocalDate): Flow<List<MedicationLog>> {
        return logs.map { list ->
            list.filter { it.scheduledAt.toLocalDate() == date }
        }
    }

    override suspend fun insertLog(log: MedicationLog): Result<Long, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        val id = nextId++
        val newLog = log.copy(id = id)
        logs.value = logs.value + newLog
        return Result.Success(id)
    }

    override suspend fun updateLog(log: MedicationLog): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake update error"))
        logs.value = logs.value.map {
            if (it.id == log.id) log else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteLog(id: Long): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        logs.value = logs.value.filter { it.id != id }
        return Result.Success(Unit)
    }

    override suspend fun hasLogForMedicationToday(
        medicationId: Long,
        timing: MedicationTiming?
    ): Boolean {
        val today = LocalDate.now()
        return logs.value.any { log ->
            log.medicationId == medicationId &&
                log.status == MedicationLogStatus.TAKEN &&
                log.scheduledAt.toLocalDate() == today &&
                (timing == null || log.timing == timing)
        }
    }

    override fun getAllLogs(): Flow<List<MedicationLog>> {
        return logs
    }
}
