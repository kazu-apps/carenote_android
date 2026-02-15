package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun getAllMembers(): Flow<List<Member>>
    fun getMemberById(id: Long): Flow<Member?>
    fun getMemberByUid(uid: String): Flow<Member?>
    suspend fun insertMember(member: Member): Result<Long, DomainError>
    suspend fun updateMember(member: Member): Result<Unit, DomainError>
    suspend fun deleteMember(id: Long): Result<Unit, DomainError>
}
