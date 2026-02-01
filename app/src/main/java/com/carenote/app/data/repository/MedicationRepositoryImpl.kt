package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val mapper: MedicationMapper
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<Medication>> {
        return medicationDao.getAllMedications().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getMedicationById(id: Long): Flow<Medication?> {
        return medicationDao.getMedicationById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override suspend fun insertMedication(medication: Medication): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert medication", it) }
        ) {
            medicationDao.insertMedication(mapper.toEntity(medication))
        }
    }

    override suspend fun updateMedication(medication: Medication): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update medication", it) }
        ) {
            medicationDao.updateMedication(mapper.toEntity(medication))
        }
    }

    override suspend fun deleteMedication(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete medication", it) }
        ) {
            medicationDao.deleteMedication(id)
        }
    }
}
