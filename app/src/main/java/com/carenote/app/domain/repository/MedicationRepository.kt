package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {

    fun getAllMedications(): Flow<List<Medication>>

    fun getMedicationById(id: Long): Flow<Medication?>

    suspend fun insertMedication(medication: Medication): Result<Long, DomainError>

    suspend fun updateMedication(medication: Medication): Result<Unit, DomainError>

    suspend fun deleteMedication(id: Long): Result<Unit, DomainError>
}
