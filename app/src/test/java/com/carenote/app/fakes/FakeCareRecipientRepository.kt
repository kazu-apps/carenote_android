package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.domain.repository.CareRecipientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCareRecipientRepository : CareRecipientRepository {

    private val careRecipient = MutableStateFlow<CareRecipient?>(null)
    var shouldFail = false
    var updateFirestoreIdCalled = false

    fun setCareRecipient(value: CareRecipient?) {
        careRecipient.value = value
    }

    fun clear() {
        careRecipient.value = null
        shouldFail = false
        updateFirestoreIdCalled = false
    }

    override fun getCareRecipient(): Flow<CareRecipient?> = careRecipient

    override suspend fun saveCareRecipient(careRecipient: CareRecipient): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake save error"))
        }
        this.careRecipient.value = careRecipient
        return Result.Success(Unit)
    }

    override suspend fun updateFirestoreId(id: Long, firestoreId: String): Result<Unit, DomainError> {
        updateFirestoreIdCalled = true
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake update firestoreId error"))
        }
        val current = careRecipient.value
        if (current != null && current.id == id) {
            careRecipient.value = current.copy(firestoreId = firestoreId)
        }
        return Result.Success(Unit)
    }
}
