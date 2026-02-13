package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.MedicationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val mapper: MedicationMapper,
    private val activeRecipientProvider: ActiveCareRecipientProvider
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<Medication>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            medicationDao.getAllMedications(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getMedicationById(id: Long): Flow<Medication?> {
        return medicationDao.getMedicationById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun searchMedications(query: String): Flow<List<Medication>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            medicationDao.searchMedications(query, recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override suspend fun insertMedication(medication: Medication): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert medication", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            medicationDao.insertMedication(mapper.toEntity(medication).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun updateMedication(medication: Medication): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update medication", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            medicationDao.updateMedication(mapper.toEntity(medication).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun deleteMedication(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete medication", it) }
        ) {
            medicationDao.deleteMedication(id)
        }
    }

    override suspend fun decrementStock(medicationId: Long, amount: Int): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to decrement stock", it) }
        ) {
            val updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            medicationDao.decrementStock(medicationId, amount, updatedAt)
        }
    }
}
