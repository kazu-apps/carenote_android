package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMedicationRepository : MedicationRepository {

    private val medications = MutableStateFlow<List<Medication>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setMedications(list: List<Medication>) {
        medications.value = list
    }

    fun clear() {
        medications.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getAllMedications(): Flow<List<Medication>> = medications

    override fun getMedicationById(id: Long): Flow<Medication?> {
        return medications.map { list -> list.find { it.id == id } }
    }

    override suspend fun insertMedication(medication: Medication): Result<Long, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        val id = nextId++
        val newMedication = medication.copy(id = id)
        medications.value = medications.value + newMedication
        return Result.Success(id)
    }

    override suspend fun updateMedication(medication: Medication): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake update error"))
        medications.value = medications.value.map {
            if (it.id == medication.id) medication else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteMedication(id: Long): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        medications.value = medications.value.filter { it.id != id }
        return Result.Success(Unit)
    }
}
