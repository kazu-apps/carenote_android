package com.carenote.app.data.repository

import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.CareRecipientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveCareRecipientProviderImpl @Inject constructor(
    private val careRecipientRepository: CareRecipientRepository
) : ActiveCareRecipientProvider {

    override val activeCareRecipientId: Flow<Long> =
        careRecipientRepository.getCareRecipient().map { it?.id ?: 0L }

    override suspend fun getActiveCareRecipientId(): Long {
        return activeCareRecipientId.first()
    }

    override suspend fun getActiveFirestoreId(): String? {
        return careRecipientRepository.getCareRecipient().first()?.firestoreId
    }
}
