package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.CareRecipientDao
import com.carenote.app.data.mapper.CareRecipientMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.domain.repository.CareRecipientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareRecipientRepositoryImpl @Inject constructor(
    private val careRecipientDao: CareRecipientDao,
    private val mapper: CareRecipientMapper
) : CareRecipientRepository {

    override fun getCareRecipient(): Flow<CareRecipient?> {
        return careRecipientDao.getCareRecipient().map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getAllCareRecipients(): Flow<List<CareRecipient>> {
        return careRecipientDao.getAllCareRecipients().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun getCareRecipientById(id: Long): CareRecipient? {
        return careRecipientDao.getCareRecipientById(id)?.let { mapper.toDomain(it) }
    }

    override suspend fun saveCareRecipient(careRecipient: CareRecipient): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to save care recipient", it) }
        ) {
            careRecipientDao.insertOrUpdate(mapper.toEntity(careRecipient))
        }
    }

    override suspend fun updateFirestoreId(id: Long, firestoreId: String): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update firestoreId", it) }
        ) {
            careRecipientDao.updateFirestoreId(id, firestoreId)
        }
    }
}
