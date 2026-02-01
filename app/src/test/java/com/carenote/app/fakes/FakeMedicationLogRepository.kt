package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.repository.MedicationLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeMedicationLogRepository : MedicationLogRepository {

    private val logs = MutableStateFlow<List<MedicationLog>>(emptyList())
    private var nextId = 1L

    fun setLogs(list: List<MedicationLog>) {
        logs.value = list
    }

    fun clear() {
        logs.value = emptyList()
        nextId = 1L
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
        val id = nextId++
        val newLog = log.copy(id = id)
        logs.value = logs.value + newLog
        return Result.Success(id)
    }

    override suspend fun updateLog(log: MedicationLog): Result<Unit, DomainError> {
        logs.value = logs.value.map {
            if (it.id == log.id) log else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteLog(id: Long): Result<Unit, DomainError> {
        logs.value = logs.value.filter { it.id != id }
        return Result.Success(Unit)
    }
}
