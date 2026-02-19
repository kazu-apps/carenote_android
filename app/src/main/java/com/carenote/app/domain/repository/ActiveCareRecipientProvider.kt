package com.carenote.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface ActiveCareRecipientProvider {
    val activeCareRecipientId: Flow<Long>
    suspend fun getActiveCareRecipientId(): Long
    suspend fun setActiveCareRecipientId(id: Long)
    suspend fun getActiveFirestoreId(): String?
}
