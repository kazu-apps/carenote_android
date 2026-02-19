package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CareRecipient
import kotlinx.coroutines.flow.Flow

interface CareRecipientRepository {

    fun getCareRecipient(): Flow<CareRecipient?>

    fun getAllCareRecipients(): Flow<List<CareRecipient>>

    suspend fun getCareRecipientById(id: Long): CareRecipient?

    suspend fun saveCareRecipient(careRecipient: CareRecipient): Result<Unit, DomainError>

    suspend fun updateFirestoreId(id: Long, firestoreId: String): Result<Unit, DomainError>
}
