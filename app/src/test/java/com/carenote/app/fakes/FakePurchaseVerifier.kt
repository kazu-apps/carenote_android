package com.carenote.app.fakes

import com.carenote.app.data.remote.PurchaseVerifier
import com.carenote.app.data.remote.VerifiedPurchase
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result

class FakePurchaseVerifier : PurchaseVerifier {

    var shouldFail = false
    var failureError: DomainError = DomainError.NetworkError("Test verification error")
    var defaultResult: VerifiedPurchase = VerifiedPurchase(
        isActive = true,
        productId = "carenote_premium_monthly",
        expiryTimeMillis = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
        autoRenewing = true
    )
    var verifyCallCount = 0
        private set
    var lastPurchaseToken: String? = null
        private set
    var lastProductId: String? = null
        private set

    fun clear() {
        shouldFail = false
        failureError = DomainError.NetworkError("Test verification error")
        defaultResult = VerifiedPurchase(
            isActive = true,
            productId = "carenote_premium_monthly",
            expiryTimeMillis = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            autoRenewing = true
        )
        verifyCallCount = 0
        lastPurchaseToken = null
        lastProductId = null
    }

    override suspend fun verify(
        purchaseToken: String,
        productId: String
    ): Result<VerifiedPurchase, DomainError> {
        verifyCallCount++
        lastPurchaseToken = purchaseToken
        lastProductId = productId
        if (shouldFail) return Result.Failure(failureError)
        return Result.Success(defaultResult)
    }
}
