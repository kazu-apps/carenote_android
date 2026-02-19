package com.carenote.app.data.remote

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result

data class VerifiedPurchase(
    val isActive: Boolean,
    val productId: String,
    val expiryTimeMillis: Long,
    val autoRenewing: Boolean
)

interface PurchaseVerifier {
    suspend fun verify(
        purchaseToken: String,
        productId: String
    ): Result<VerifiedPurchase, DomainError>
}
