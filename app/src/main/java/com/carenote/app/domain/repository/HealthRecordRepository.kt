package com.carenote.app.domain.repository

import androidx.paging.PagingData
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.HealthRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface HealthRecordRepository {

    fun getAllRecords(): Flow<List<HealthRecord>>

    fun getRecordById(id: Long): Flow<HealthRecord?>

    fun getRecordsByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<HealthRecord>>

    fun getPagedRecords(query: String): Flow<PagingData<HealthRecord>>

    suspend fun insertRecord(record: HealthRecord): Result<Long, DomainError>

    suspend fun updateRecord(record: HealthRecord): Result<Unit, DomainError>

    suspend fun deleteRecord(id: Long): Result<Unit, DomainError>
}
