package com.carenote.app.data.repository

import android.app.Activity
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.BillingConnectionState
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.domain.model.ProductInfo
import com.carenote.app.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoOpBillingRepository : BillingRepository {

    override val premiumStatus: StateFlow<PremiumStatus> =
        MutableStateFlow<PremiumStatus>(PremiumStatus.Inactive).asStateFlow()

    override val connectionState: StateFlow<BillingConnectionState> =
        MutableStateFlow(BillingConnectionState.UNAVAILABLE).asStateFlow()

    override suspend fun queryProducts(): Result<List<ProductInfo>, DomainError> {
        return Result.Failure(
            DomainError.NetworkError("Google Play Billing is not available")
        )
    }

    override suspend fun acknowledgePurchase(
        purchaseToken: String
    ): Result<Unit, DomainError> {
        return Result.Failure(
            DomainError.NetworkError("Google Play Billing is not available")
        )
    }

    override suspend fun restorePurchases(): Result<PremiumStatus, DomainError> {
        return Result.Failure(
            DomainError.NetworkError("Google Play Billing is not available")
        )
    }

    override suspend fun launchBillingFlow(
        activity: Activity,
        productId: String
    ): Result<Unit, DomainError> {
        return Result.Failure(
            DomainError.NetworkError("Google Play Billing is not available")
        )
    }

    override fun startConnection() { /* No-op */ }
    override fun endConnection() { /* No-op */ }
}
