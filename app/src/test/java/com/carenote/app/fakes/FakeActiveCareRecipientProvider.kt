package com.carenote.app.fakes

import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class FakeActiveCareRecipientProvider : ActiveCareRecipientProvider {
    private val _activeCareRecipientId = MutableStateFlow(1L)
    override val activeCareRecipientId: Flow<Long> = _activeCareRecipientId

    override suspend fun getActiveCareRecipientId(): Long = _activeCareRecipientId.first()

    fun setActiveCareRecipientId(id: Long) {
        _activeCareRecipientId.value = id
    }
}
