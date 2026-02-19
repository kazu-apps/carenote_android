package com.carenote.app.data.repository

import com.carenote.app.data.local.ActiveCareRecipientPreferences
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.CareRecipientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveCareRecipientProviderImpl @Inject constructor(
    private val careRecipientRepository: CareRecipientRepository,
    private val preferences: ActiveCareRecipientPreferences
) : ActiveCareRecipientProvider {

    override val activeCareRecipientId: Flow<Long> = combine(
        preferences.activeCareRecipientId,
        careRecipientRepository.getAllCareRecipients()
    ) { savedId, recipients ->
        val ids = recipients.map { it.id }
        if (savedId != 0L && savedId in ids) savedId
        else recipients.firstOrNull()?.id ?: 0L
    }

    override suspend fun getActiveCareRecipientId(): Long {
        return activeCareRecipientId.first()
    }

    override suspend fun setActiveCareRecipientId(id: Long) {
        preferences.setActiveCareRecipientId(id)
    }

    override suspend fun getActiveFirestoreId(): String? {
        val activeId = getActiveCareRecipientId()
        return careRecipientRepository.getCareRecipientById(activeId)?.firestoreId
    }
}
