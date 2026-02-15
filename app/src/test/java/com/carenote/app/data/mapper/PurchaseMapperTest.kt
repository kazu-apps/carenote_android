package com.carenote.app.data.mapper

import com.android.billingclient.api.Purchase
import com.carenote.app.data.local.entity.PurchaseEntity
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.fakes.FakeClock
import com.carenote.app.testing.TestDataFixtures
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class PurchaseMapperTest {

    private lateinit var mapper: PurchaseMapper

    @Before
    fun setup() {
        mapper = PurchaseMapper()
    }

    // --- toPremiumStatus ---

    @Test
    fun `toPremiumStatus returns Inactive when entity is null`() {
        val result = mapper.toPremiumStatus(null)
        assertEquals(PremiumStatus.Inactive, result)
    }

    @Test
    fun `toPremiumStatus returns Active for PURCHASED state`() {
        val entity = createEntity(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            expiryTime = LocalDateTime.now().plusDays(30).toString()
        )
        val result = mapper.toPremiumStatus(entity)
        assertTrue(result is PremiumStatus.Active)
        val active = result as PremiumStatus.Active
        assertEquals("test_product", active.productId)
        assertEquals("test_token", active.purchaseToken)
        assertTrue(active.autoRenewing)
    }

    @Test
    fun `toPremiumStatus returns Expired when expiryTime is past`() {
        val entity = createEntity(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            expiryTime = LocalDateTime.now().minusDays(1).toString()
        )
        val result = mapper.toPremiumStatus(entity)
        assertEquals(PremiumStatus.Expired, result)
    }

    @Test
    fun `toPremiumStatus returns Active when expiryTime is null`() {
        val entity = createEntity(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            expiryTime = null
        )
        val result = mapper.toPremiumStatus(entity)
        assertTrue(result is PremiumStatus.Active)
    }

    @Test
    fun `toPremiumStatus returns Pending for PENDING state`() {
        val entity = createEntity(purchaseState = Purchase.PurchaseState.PENDING)
        val result = mapper.toPremiumStatus(entity)
        assertEquals(PremiumStatus.Pending, result)
    }

    @Test
    fun `toPremiumStatus returns Inactive for UNSPECIFIED state`() {
        val entity = createEntity(purchaseState = Purchase.PurchaseState.UNSPECIFIED_STATE)
        val result = mapper.toPremiumStatus(entity)
        assertEquals(PremiumStatus.Inactive, result)
    }

    @Test
    fun `toPremiumStatus returns Inactive for unknown state`() {
        val entity = createEntity(purchaseState = 99)
        val result = mapper.toPremiumStatus(entity)
        assertEquals(PremiumStatus.Inactive, result)
    }

    @Test
    fun `toPremiumStatus handles invalid expiryTime format gracefully`() {
        val entity = createEntity(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            expiryTime = "invalid-date"
        )
        val result = mapper.toPremiumStatus(entity)
        assertTrue(result is PremiumStatus.Active)
        assertNull((result as PremiumStatus.Active).expiryTime)
    }

    // --- toEntity ---

    @Test
    fun `toEntity maps Purchase to PurchaseEntity`() {
        val clock = FakeClock()
        val purchase = mockk<Purchase>()
        every { purchase.products } returns listOf("carenote_premium_monthly")
        every { purchase.purchaseToken } returns "token_123"
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.isAcknowledged } returns true
        every { purchase.isAutoRenewing } returns true
        every { purchase.purchaseTime } returns 1700000000000L

        val entity = mapper.toEntity(purchase, clock)

        assertEquals("carenote_premium_monthly", entity.productId)
        assertEquals("token_123", entity.purchaseToken)
        assertEquals(Purchase.PurchaseState.PURCHASED, entity.purchaseState)
        assertTrue(entity.isAcknowledged)
        assertTrue(entity.isAutoRenewing)
    }

    @Test
    fun `toEntity uses empty productId when products list is empty`() {
        val clock = FakeClock()
        val purchase = mockk<Purchase>()
        every { purchase.products } returns emptyList()
        every { purchase.purchaseToken } returns "token"
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.isAcknowledged } returns false
        every { purchase.isAutoRenewing } returns false
        every { purchase.purchaseTime } returns 1700000000000L

        val entity = mapper.toEntity(purchase, clock)

        assertEquals("", entity.productId)
    }

    // --- Helper ---

    private fun createEntity(
        purchaseState: Int = Purchase.PurchaseState.PURCHASED,
        expiryTime: String? = null
    ): PurchaseEntity = PurchaseEntity(
        id = 1,
        productId = "test_product",
        purchaseToken = "test_token",
        purchaseState = purchaseState,
        isAcknowledged = true,
        isAutoRenewing = true,
        purchaseTime = TestDataFixtures.NOW_STRING,
        expiryTime = expiryTime,
        updatedAt = TestDataFixtures.NOW_STRING
    )
}
