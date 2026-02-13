package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.fakes.FakeCareRecipientRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ActiveCareRecipientProviderImplTest {

    private lateinit var careRecipientRepository: FakeCareRecipientRepository
    private lateinit var provider: ActiveCareRecipientProviderImpl

    @Before
    fun setUp() {
        careRecipientRepository = FakeCareRecipientRepository()
        provider = ActiveCareRecipientProviderImpl(careRecipientRepository)
    }

    @Test
    fun `activeCareRecipientId emits 0L when no care recipient`() = runTest {
        provider.activeCareRecipientId.test {
            assertEquals(0L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `activeCareRecipientId emits correct id when care recipient exists`() = runTest {
        careRecipientRepository.setCareRecipient(
            CareRecipient(id = 42L, name = "テスト太郎")
        )

        provider.activeCareRecipientId.test {
            assertEquals(42L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `activeCareRecipientId updates when care recipient changes`() = runTest {
        provider.activeCareRecipientId.test {
            assertEquals(0L, awaitItem())
            careRecipientRepository.setCareRecipient(
                CareRecipient(id = 10L, name = "テスト太郎")
            )
            assertEquals(10L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActiveCareRecipientId returns first value`() = runTest {
        careRecipientRepository.setCareRecipient(
            CareRecipient(id = 99L, name = "テスト花子")
        )

        val result = provider.getActiveCareRecipientId()
        assertEquals(99L, result)
    }
}
