package com.carenote.app.data.repository

import com.carenote.app.domain.model.BillingConnectionState
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.testing.assertNetworkError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NoOpBillingRepositoryTest {

    private lateinit var repository: NoOpBillingRepository

    @Before
    fun setup() {
        repository = NoOpBillingRepository()
    }

    @Test
    fun `premiumStatus is always Inactive`() {
        assertEquals(PremiumStatus.Inactive, repository.premiumStatus.value)
    }

    @Test
    fun `connectionState is always UNAVAILABLE`() {
        assertEquals(BillingConnectionState.UNAVAILABLE, repository.connectionState.value)
    }

    @Test
    fun `queryProducts returns NetworkError`() = runTest {
        val result = repository.queryProducts()
        result.assertNetworkError()
    }

    @Test
    fun `acknowledgePurchase returns NetworkError`() = runTest {
        val result = repository.acknowledgePurchase("token")
        result.assertNetworkError()
    }

    @Test
    fun `restorePurchases returns NetworkError`() = runTest {
        val result = repository.restorePurchases()
        result.assertNetworkError()
    }

    @Test
    fun `startConnection does nothing`() {
        repository.startConnection()
        assertEquals(BillingConnectionState.UNAVAILABLE, repository.connectionState.value)
    }

    @Test
    fun `endConnection does nothing`() {
        repository.endConnection()
        assertEquals(BillingConnectionState.UNAVAILABLE, repository.connectionState.value)
    }
}
