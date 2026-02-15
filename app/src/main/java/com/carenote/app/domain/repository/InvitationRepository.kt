package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import kotlinx.coroutines.flow.Flow

interface InvitationRepository {
    fun getAllInvitations(): Flow<List<Invitation>>
    fun getInvitationById(id: Long): Flow<Invitation?>
    fun getInvitationByToken(token: String): Flow<Invitation?>
    fun getInvitationsByEmail(email: String, status: InvitationStatus): Flow<List<Invitation>>
    suspend fun insertInvitation(invitation: Invitation): Result<Long, DomainError>
    suspend fun updateInvitation(invitation: Invitation): Result<Unit, DomainError>
    suspend fun deleteInvitation(id: Long): Result<Unit, DomainError>
}
