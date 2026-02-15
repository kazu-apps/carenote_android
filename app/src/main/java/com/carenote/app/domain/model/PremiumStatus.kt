package com.carenote.app.domain.model

import java.time.LocalDateTime

sealed class PremiumStatus {
    data object Inactive : PremiumStatus()
    data class Active(
        val productId: String,
        val purchaseToken: String,
        val expiryTime: LocalDateTime?,
        val autoRenewing: Boolean
    ) : PremiumStatus()
    data object Expired : PremiumStatus()
    data object Pending : PremiumStatus()

    val isActive: Boolean get() = this is Active
}
