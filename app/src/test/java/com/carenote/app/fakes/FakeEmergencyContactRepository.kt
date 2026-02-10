package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeEmergencyContactRepository : EmergencyContactRepository {

    private val contacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setContacts(list: List<EmergencyContact>) {
        contacts.value = list
    }

    fun clear() {
        contacts.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getAllContacts(): Flow<List<EmergencyContact>> = contacts

    override fun getContactById(id: Long): Flow<EmergencyContact?> {
        return contacts.map { list -> list.find { it.id == id } }
    }

    override suspend fun insertContact(contact: EmergencyContact): Result<Long, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        }
        val id = nextId++
        val newContact = contact.copy(id = id)
        contacts.value = contacts.value + newContact
        return Result.Success(id)
    }

    override suspend fun updateContact(contact: EmergencyContact): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake update error"))
        }
        contacts.value = contacts.value.map {
            if (it.id == contact.id) contact else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteContact(id: Long): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        }
        contacts.value = contacts.value.filter { it.id != id }
        return Result.Success(Unit)
    }
}
