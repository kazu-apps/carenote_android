package com.carenote.app.ui.screens.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.ProductInfo
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.BillingRepository
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.BillingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    private val _isLoading = MutableStateFlow(false)

    val billingUiState: StateFlow<BillingUiState> = combine(
        billingRepository.premiumStatus,
        billingRepository.connectionState,
        _products,
        _isLoading
    ) { status, connection, products, loading ->
        BillingUiState(
            premiumStatus = status,
            connectionState = connection,
            products = products,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(
            AppConfig.UI.FLOW_STOP_TIMEOUT_MS
        ),
        initialValue = BillingUiState()
    )

    fun connectBilling() {
        billingRepository.startConnection()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = billingRepository.queryProducts()) {
                is Result.Success -> _products.value = result.value
                is Result.Failure -> {
                    Timber.w("Failed to load products: ${result.error}")
                    snackbarController.showMessage(
                        R.string.billing_error_load_products
                    )
                }
            }
            _isLoading.value = false
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        analyticsRepository.logEvent(
            AppConfig.Analytics.EVENT_PURCHASE_STARTED,
            mapOf(AppConfig.Analytics.PARAM_PRODUCT_ID to productId)
        )
        viewModelScope.launch {
            when (val result = billingRepository.launchBillingFlow(
                activity, productId
            )) {
                is Result.Success -> Unit
                is Result.Failure -> {
                    Timber.w("Failed to launch purchase: ${result.error}")
                    snackbarController.showMessage(
                        R.string.billing_error_purchase
                    )
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            when (val result = billingRepository.restorePurchases()) {
                is Result.Success -> {
                    analyticsRepository.logEvent(
                        AppConfig.Analytics.EVENT_PURCHASE_RESTORED
                    )
                    if (result.value.isActive) {
                        snackbarController.showMessage(
                            R.string.billing_restore_success
                        )
                    } else {
                        snackbarController.showMessage(
                            R.string.billing_restore_no_purchases
                        )
                    }
                }
                is Result.Failure -> {
                    Timber.w(
                        "Failed to restore purchases: ${result.error}"
                    )
                    snackbarController.showMessage(
                        R.string.billing_error_restore
                    )
                }
            }
        }
    }

    fun logManageSubscription() {
        analyticsRepository.logEvent(
            AppConfig.Analytics.EVENT_MANAGE_SUBSCRIPTION
        )
    }
}
