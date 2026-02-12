package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationTiming
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MedicationLogRepository {

    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>>

    fun getLogsForDate(date: LocalDate): Flow<List<MedicationLog>>

    suspend fun insertLog(log: MedicationLog): Result<Long, DomainError>

    suspend fun updateLog(log: MedicationLog): Result<Unit, DomainError>

    suspend fun deleteLog(id: Long): Result<Unit, DomainError>

    suspend fun hasLogForMedicationToday(
        medicationId: Long,
        timing: MedicationTiming? = null
    ): Boolean

    fun getAllLogs(): Flow<List<MedicationLog>>
}
