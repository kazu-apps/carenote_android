package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMemberRepository : MemberRepository {

    private val members = MutableStateFlow<List<Member>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setMembers(list: List<Member>) {
        members.value = list
    }

    fun clear() {
        members.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getAllMembers(): Flow<List<Member>> = members

    override fun getMemberById(id: Long): Flow<Member?> =
        members.map { list -> list.find { it.id == id } }

    override fun getMemberByUid(uid: String): Flow<Member?> =
        members.map { list -> list.find { it.uid == uid } }

    override suspend fun insertMember(member: Member): Result<Long, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        val id = nextId++
        members.value = members.value + member.copy(id = id)
        return Result.Success(id)
    }

    override suspend fun updateMember(member: Member): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake update error"))
        members.value = members.value.map { if (it.id == member.id) member else it }
        return Result.Success(Unit)
    }

    override suspend fun deleteMember(id: Long): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        members.value = members.value.filter { it.id != id }
        return Result.Success(Unit)
    }
}
