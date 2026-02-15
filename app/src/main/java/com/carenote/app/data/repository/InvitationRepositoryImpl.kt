package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.InvitationDao
import com.carenote.app.data.mapper.InvitationMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.InvitationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class InvitationRepositoryImpl @Inject constructor(
    private val invitationDao: InvitationDao,
    private val mapper: InvitationMapper,
    private val activeRecipientProvider: ActiveCareRecipientProvider
) : InvitationRepository {

    override fun getAllInvitations(): Flow<List<Invitation>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            invitationDao.getAllInvitations(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getInvitationById(id: Long): Flow<Invitation?> {
        return invitationDao.getInvitationById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getInvitationByToken(token: String): Flow<Invitation?> {
        return invitationDao.getInvitationByToken(token).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getInvitationsByEmail(email: String, status: InvitationStatus): Flow<List<Invitation>> {
        return invitationDao.getInvitationsByEmail(email, status.name).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun insertInvitation(invitation: Invitation): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert invitation", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            invitationDao.insertInvitation(
                mapper.toEntity(invitation).copy(careRecipientId = recipientId)
            )
        }
    }

    override suspend fun updateInvitation(invitation: Invitation): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update invitation", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            invitationDao.updateInvitation(mapper.toEntity(invitation).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun deleteInvitation(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete invitation", it) }
        ) {
            invitationDao.deleteInvitation(id)
        }
    }
}
