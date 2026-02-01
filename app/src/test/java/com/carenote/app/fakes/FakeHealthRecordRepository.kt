package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.repository.HealthRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class FakeHealthRecordRepository : HealthRecordRepository {

    private val records = MutableStateFlow<List<HealthRecord>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setRecords(list: List<HealthRecord>) {
        records.value = list
    }

    fun clear() {
        records.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getAllRecords(): Flow<List<HealthRecord>> = records

    override fun getRecordById(id: Long): Flow<HealthRecord?> {
        return records.map { list -> list.find { it.id == id } }
    }

    override fun getRecordsByDateRange(
        start: LocalDateTime,
        end: LocalDateTime
    ): Flow<List<HealthRecord>> {
        return records.map { list ->
            list.filter { record ->
                !record.recordedAt.isBefore(start) && !record.recordedAt.isAfter(end)
            }
        }
    }

    override suspend fun insertRecord(record: HealthRecord): Result<Long, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        }
        val id = nextId++
        val newRecord = record.copy(id = id)
        records.value = records.value + newRecord
        return Result.Success(id)
    }

    override suspend fun updateRecord(record: HealthRecord): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake update error"))
        }
        records.value = records.value.map {
            if (it.id == record.id) record else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteRecord(id: Long): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        }
        records.value = records.value.filter { it.id != id }
        return Result.Success(Unit)
    }
}
