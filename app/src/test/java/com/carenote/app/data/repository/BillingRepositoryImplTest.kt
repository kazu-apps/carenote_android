package com.carenote.app.data.repository

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.carenote.app.data.local.dao.PurchaseDao
import com.carenote.app.data.mapper.PurchaseMapper
import com.carenote.app.domain.model.BillingConnectionState
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakePurchaseVerifier
import com.carenote.app.testing.assertNetworkError
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BillingRepositoryImplTest {

    private val context = mockk<Context>(relaxed = true)
    private val purchaseDao = mockk<PurchaseDao>(relaxed = true)
    private val purchaseMapper = PurchaseMapper()
    private val clock = FakeClock()
    private val purchaseVerifier = FakePurchaseVerifier()
    private val mockBillingClient = mockk<BillingClient>(relaxed = true)
    private val listenerSlot = slot<PurchasesUpdatedListener>()
    private val builderMock = mockk<BillingClient.Builder>(relaxed = true)
    private val pendingPurchasesBuilderMock = mockk<PendingPurchasesParams.Builder>(relaxed = true)
    private val pendingPurchasesParams = mockk<PendingPurchasesParams>(relaxed = true)

    private lateinit var repository: BillingRepositoryImpl

    @Before
    fun setup() {
        every { purchaseDao.getLatestPurchase() } returns flowOf(null)

        mockkStatic(BillingClient::class)
        mockkStatic(PendingPurchasesParams::class)

        every { PendingPurchasesParams.newBuilder() } returns pendingPurchasesBuilderMock
        every { pendingPurchasesBuilderMock.enableOneTimeProducts() } returns pendingPurchasesBuilderMock
        every { pendingPurchasesBuilderMock.enablePrepaidPlans() } returns pendingPurchasesBuilderMock
        every { pendingPurchasesBuilderMock.build() } returns pendingPurchasesParams

        every { BillingClient.newBuilder(any()) } returns builderMock
        every { builderMock.setListener(capture(listenerSlot)) } returns builderMock
        every { builderMock.enablePendingPurchases(any()) } returns builderMock
        every { builderMock.build() } returns mockBillingClient

        repository = BillingRepositoryImpl(context, purchaseDao, purchaseMapper, clock, purchaseVerifier)
    }

    @After
    fun tearDown() {
        unmockkStatic(BillingClient::class)
        unmockkStatic(PendingPurchasesParams::class)
    }

    // --- Connection ---

    @Test
    fun `initial connectionState is DISCONNECTED`() {
        assertEquals(BillingConnectionState.DISCONNECTED, repository.connectionState.value)
    }

    @Test
    fun `initial premiumStatus is Inactive`() {
        assertEquals(PremiumStatus.Inactive, repository.premiumStatus.value)
    }

    @Test
    fun `startConnection sets state to CONNECTING`() {
        val stateListenerSlot = slot<BillingClientStateListener>()
        every { mockBillingClient.startConnection(capture(stateListenerSlot)) } answers {
            // Don't call onBillingSetupFinished yet
        }

        repository.startConnection()

        assertEquals(BillingConnectionState.CONNECTING, repository.connectionState.value)
    }

    @Test
    fun `startConnection sets CONNECTED on successful setup`() {
        val stateListenerSlot = slot<BillingClientStateListener>()
        every { mockBillingClient.startConnection(capture(stateListenerSlot)) } answers {
            val okResult = mockk<BillingResult>()
            every { okResult.responseCode } returns BillingClient.BillingResponseCode.OK
            stateListenerSlot.captured.onBillingSetupFinished(okResult)
        }

        repository.startConnection()

        assertEquals(BillingConnectionState.CONNECTED, repository.connectionState.value)
    }

    @Test
    fun `startConnection sets UNAVAILABLE on failed setup`() {
        val stateListenerSlot = slot<BillingClientStateListener>()
        every { mockBillingClient.startConnection(capture(stateListenerSlot)) } answers {
            val failResult = mockk<BillingResult>()
            every { failResult.responseCode } returns BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
            every { failResult.debugMessage } returns "unavailable"
            stateListenerSlot.captured.onBillingSetupFinished(failResult)
        }

        repository.startConnection()

        assertEquals(BillingConnectionState.UNAVAILABLE, repository.connectionState.value)
    }

    @Test
    fun `endConnection sets state to DISCONNECTED`() {
        repository.endConnection()

        assertEquals(BillingConnectionState.DISCONNECTED, repository.connectionState.value)
    }

    @Test
    fun `startConnection does nothing when already CONNECTED`() {
        val stateListenerSlot = slot<BillingClientStateListener>()
        every { mockBillingClient.startConnection(capture(stateListenerSlot)) } answers {
            val okResult = mockk<BillingResult>()
            every { okResult.responseCode } returns BillingClient.BillingResponseCode.OK
            stateListenerSlot.captured.onBillingSetupFinished(okResult)
        }
        repository.startConnection()
        assertEquals(BillingConnectionState.CONNECTED, repository.connectionState.value)

        // Try to connect again - should be no-op
        repository.startConnection()
        assertEquals(BillingConnectionState.CONNECTED, repository.connectionState.value)
    }

    // --- queryProducts when not connected ---

    @Test
    fun `queryProducts returns NetworkError when not connected`() = runTest {
        val result = repository.queryProducts()
        result.assertNetworkError()
    }

    // --- acknowledgePurchase when not connected ---

    @Test
    fun `acknowledgePurchase returns NetworkError when not connected`() = runTest {
        val result = repository.acknowledgePurchase("token")
        result.assertNetworkError()
    }

    // --- restorePurchases when not connected ---

    @Test
    fun `restorePurchases returns NetworkError when not connected`() = runTest {
        val result = repository.restorePurchases()
        result.assertNetworkError()
    }

}
