package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.MemberDao
import com.carenote.app.data.mapper.MemberMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.MemberRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class MemberRepositoryImpl @Inject constructor(
    private val memberDao: MemberDao,
    private val mapper: MemberMapper,
    private val activeRecipientProvider: ActiveCareRecipientProvider
) : MemberRepository {

    override fun getAllMembers(): Flow<List<Member>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            memberDao.getAllMembers(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getMemberById(id: Long): Flow<Member?> {
        return memberDao.getMemberById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getMemberByUid(uid: String): Flow<Member?> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            memberDao.getMemberByUid(recipientId, uid)
        }.map { entity -> entity?.let { mapper.toDomain(it) } }
    }

    override suspend fun insertMember(member: Member): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert member", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            memberDao.insertMember(
                mapper.toEntity(member).copy(careRecipientId = recipientId)
            )
        }
    }

    override suspend fun updateMember(member: Member): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update member", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            memberDao.updateMember(mapper.toEntity(member).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun deleteMember(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete member", it) }
        ) {
            memberDao.deleteMember(id)
        }
    }
}
