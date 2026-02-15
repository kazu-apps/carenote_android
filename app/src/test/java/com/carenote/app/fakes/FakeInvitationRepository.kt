package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.domain.repository.InvitationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeInvitationRepository : InvitationRepository {

    private val invitations = MutableStateFlow<List<Invitation>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setInvitations(list: List<Invitation>) {
        invitations.value = list
    }

    fun clear() {
        invitations.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getAllInvitations(): Flow<List<Invitation>> = invitations

    override fun getInvitationById(id: Long): Flow<Invitation?> =
        invitations.map { list -> list.find { it.id == id } }

    override fun getInvitationByToken(token: String): Flow<Invitation?> =
        invitations.map { list -> list.find { it.token == token } }

    override fun getInvitationsByEmail(email: String, status: InvitationStatus): Flow<List<Invitation>> =
        invitations.map { list ->
            list.filter { it.inviteeEmail == email && it.status == status }
        }

    override suspend fun insertInvitation(invitation: Invitation): Result<Long, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        val id = nextId++
        invitations.value = invitations.value + invitation.copy(id = id)
        return Result.Success(id)
    }

    override suspend fun updateInvitation(invitation: Invitation): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake update error"))
        invitations.value = invitations.value.map { if (it.id == invitation.id) invitation else it }
        return Result.Success(Unit)
    }

    override suspend fun deleteInvitation(id: Long): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        invitations.value = invitations.value.filter { it.id != id }
        return Result.Success(Unit)
    }
}
