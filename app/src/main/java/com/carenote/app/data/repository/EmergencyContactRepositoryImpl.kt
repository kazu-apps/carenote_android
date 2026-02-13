package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.EmergencyContactDao
import com.carenote.app.data.mapper.EmergencyContactMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class EmergencyContactRepositoryImpl @Inject constructor(
    private val emergencyContactDao: EmergencyContactDao,
    private val mapper: EmergencyContactMapper,
    private val activeRecipientProvider: ActiveCareRecipientProvider
) : EmergencyContactRepository {

    override fun getAllContacts(): Flow<List<EmergencyContact>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            emergencyContactDao.getAll(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getContactById(id: Long): Flow<EmergencyContact?> {
        return emergencyContactDao.getById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override suspend fun insertContact(contact: EmergencyContact): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert emergency contact", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            emergencyContactDao.insert(mapper.toEntity(contact).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun updateContact(contact: EmergencyContact): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update emergency contact", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            emergencyContactDao.update(mapper.toEntity(contact).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun deleteContact(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete emergency contact", it) }
        ) {
            emergencyContactDao.deleteById(id)
        }
    }
}
