package com.carenote.app.data.remote

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result

class NoOpPurchaseVerifier : PurchaseVerifier {
    override suspend fun verify(
        purchaseToken: String,
        productId: String
    ): Result<VerifiedPurchase, DomainError> {
        return Result.Failure(
            DomainError.NetworkError("Purchase verification unavailable (Firebase not configured)")
        )
    }
}
