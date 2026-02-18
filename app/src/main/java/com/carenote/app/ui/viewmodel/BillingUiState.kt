package com.carenote.app.ui.viewmodel

import com.carenote.app.domain.model.BillingConnectionState
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.domain.model.ProductInfo

data class BillingUiState(
    val premiumStatus: PremiumStatus = PremiumStatus.Inactive,
    val connectionState: BillingConnectionState = BillingConnectionState.DISCONNECTED,
    val products: List<ProductInfo> = emptyList(),
    val isLoading: Boolean = false
)
