package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.BillingConnectionState
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.domain.model.ProductInfo
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val premiumStatus: StateFlow<PremiumStatus>
    val connectionState: StateFlow<BillingConnectionState>

    suspend fun queryProducts(): Result<List<ProductInfo>, DomainError>
    suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit, DomainError>
    suspend fun restorePurchases(): Result<PremiumStatus, DomainError>
    fun startConnection()
    fun endConnection()
}
