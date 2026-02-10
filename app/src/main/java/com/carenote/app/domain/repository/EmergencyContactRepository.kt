package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

interface EmergencyContactRepository {

    fun getAllContacts(): Flow<List<EmergencyContact>>

    fun getContactById(id: Long): Flow<EmergencyContact?>

    suspend fun insertContact(contact: EmergencyContact): Result<Long, DomainError>

    suspend fun updateContact(contact: EmergencyContact): Result<Unit, DomainError>

    suspend fun deleteContact(id: Long): Result<Unit, DomainError>
}
