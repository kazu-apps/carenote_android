package com.carenote.app.fakes

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

class FakeBillingRepository : BillingRepository {

    private val _premiumStatus = MutableStateFlow<PremiumStatus>(PremiumStatus.Inactive)
    override val premiumStatus: StateFlow<PremiumStatus> = _premiumStatus.asStateFlow()

    private val _connectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    var shouldFail = false
    var launchBillingFlowCallCount = 0
    private var products = emptyList<ProductInfo>()

    fun setPremiumActive(productId: String = "carenote_premium_monthly") {
        _premiumStatus.value = PremiumStatus.Active(
            productId = productId,
            purchaseToken = "fake_token",
            expiryTime = null,
            autoRenewing = true
        )
    }

    fun setPremiumInactive() {
        _premiumStatus.value = PremiumStatus.Inactive
    }

    fun setPremiumExpired() {
        _premiumStatus.value = PremiumStatus.Expired
    }

    fun setPremiumPending() {
        _premiumStatus.value = PremiumStatus.Pending
    }

    fun setProducts(productList: List<ProductInfo>) {
        products = productList
    }

    fun setConnectionState(state: BillingConnectionState) {
        _connectionState.value = state
    }

    override suspend fun queryProducts(): Result<List<ProductInfo>, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.NetworkError("Fake error"))
        return Result.Success(products)
    }

    override suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.NetworkError("Fake error"))
        return Result.Success(Unit)
    }

    override suspend fun restorePurchases(): Result<PremiumStatus, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.NetworkError("Fake error"))
        return Result.Success(_premiumStatus.value)
    }

    override fun startConnection() {
        _connectionState.value = BillingConnectionState.CONNECTED
    }

    override fun endConnection() {
        _connectionState.value = BillingConnectionState.DISCONNECTED
    }

    override suspend fun launchBillingFlow(
        activity: Activity,
        productId: String
    ): Result<Unit, DomainError> {
        launchBillingFlowCallCount++
        if (shouldFail) return Result.Failure(DomainError.NetworkError("Fake error"))
        return Result.Success(Unit)
    }

    fun clear() {
        _premiumStatus.value = PremiumStatus.Inactive
        _connectionState.value = BillingConnectionState.DISCONNECTED
        shouldFail = false
        launchBillingFlowCallCount = 0
        products = emptyList()
    }
}
