package com.carenote.app.data.repository

import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import android.app.Activity
import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.dao.PurchaseDao
import com.carenote.app.data.mapper.PurchaseMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.BillingConnectionState
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.domain.model.ProductInfo
import com.carenote.app.domain.repository.BillingRepository
import com.carenote.app.domain.util.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BillingRepositoryImpl(
    context: Context,
    private val purchaseDao: PurchaseDao,
    private val purchaseMapper: PurchaseMapper,
    private val clock: Clock
) : BillingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _premiumStatus = MutableStateFlow<PremiumStatus>(PremiumStatus.Inactive)
    override val premiumStatus: StateFlow<PremiumStatus> = _premiumStatus.asStateFlow()

    private val _connectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<BillingConnectionState> =
        _connectionState.asStateFlow()

    private var cachedProductDetails: List<ProductDetails> = emptyList()
    private var retryCount = 0

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            scope.launch {
                handlePurchasesUpdated(billingResult, purchases)
            }
        }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    init {
        observePurchaseDao()
    }

    private fun observePurchaseDao() {
        scope.launch {
            purchaseDao.getLatestPurchase().collect { entity ->
                _premiumStatus.value = purchaseMapper.toPremiumStatus(entity)
            }
        }
    }

    override fun startConnection() {
        if (_connectionState.value == BillingConnectionState.CONNECTED ||
            _connectionState.value == BillingConnectionState.CONNECTING
        ) return

        _connectionState.value = BillingConnectionState.CONNECTING
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing client connected")
                    _connectionState.value = BillingConnectionState.CONNECTED
                    retryCount = 0
                } else {
                    Timber.w("Billing setup failed: ${billingResult.debugMessage}")
                    _connectionState.value = BillingConnectionState.UNAVAILABLE
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing service disconnected")
                _connectionState.value = BillingConnectionState.DISCONNECTED
                retryConnection()
            }
        })
    }

    override fun endConnection() {
        billingClient.endConnection()
        _connectionState.value = BillingConnectionState.DISCONNECTED
        retryCount = 0
    }

    private fun retryConnection() {
        if (retryCount < AppConfig.Billing.MAX_CONNECTION_RETRIES) {
            retryCount++
            scope.launch {
                delay(AppConfig.Billing.CONNECTION_RETRY_DELAY_MS)
                startConnection()
            }
        }
    }

    override suspend fun queryProducts(): Result<List<ProductInfo>, DomainError> {
        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            return Result.Failure(
                DomainError.NetworkError("Billing client not connected")
            )
        }

        val productList = listOf(
            AppConfig.Billing.MONTHLY_PRODUCT_ID,
            AppConfig.Billing.YEARLY_PRODUCT_ID
        ).map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result: ProductDetailsResult = billingClient.queryProductDetails(params)

        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            cachedProductDetails = result.productDetailsList ?: emptyList()
        }

        return mapBillingResult(result.billingResult) {
            result.productDetailsList?.map { details ->
                val subscriptionOffer =
                    details.subscriptionOfferDetails?.firstOrNull()
                val pricingPhase = subscriptionOffer?.pricingPhases
                    ?.pricingPhaseList?.firstOrNull()

                ProductInfo(
                    productId = details.productId,
                    name = details.name,
                    description = details.description,
                    formattedPrice = pricingPhase?.formattedPrice ?: "",
                    priceMicros = pricingPhase?.priceAmountMicros ?: 0L,
                    billingPeriod = pricingPhase?.billingPeriod ?: ""
                )
            } ?: emptyList()
        }
    }

    override suspend fun acknowledgePurchase(
        purchaseToken: String
    ): Result<Unit, DomainError> {
        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            return Result.Failure(
                DomainError.NetworkError("Billing client not connected")
            )
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        val billingResult = billingClient.acknowledgePurchase(params)
        return mapBillingResult(billingResult) { }
    }

    override suspend fun restorePurchases(): Result<PremiumStatus, DomainError> {
        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            return Result.Failure(
                DomainError.NetworkError("Billing client not connected")
            )
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val result = billingClient.queryPurchasesAsync(params)

        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return mapBillingResult(result.billingResult) { PremiumStatus.Inactive }
        }

        val purchases = result.purchasesList
        if (purchases.isEmpty()) {
            return Result.Success(PremiumStatus.Inactive)
        }

        val latestPurchase = purchases.maxByOrNull { it.purchaseTime }
        if (latestPurchase != null) {
            processPurchase(latestPurchase)
        }
        return Result.Success(_premiumStatus.value)
    }

    override suspend fun launchBillingFlow(
        activity: Activity,
        productId: String
    ): Result<Unit, DomainError> {
        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            return Result.Failure(
                DomainError.NetworkError("Billing client not connected")
            )
        }

        val productDetails = cachedProductDetails.find { it.productId == productId }
            ?: return Result.Failure(
                DomainError.NotFoundError("Product not found: $productId")
            )

        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()?.offerToken
            ?: return Result.Failure(
                DomainError.NotFoundError(
                    "No offer available for product: $productId"
                )
            )

        val productDetailsParams =
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val billingResult = billingClient.launchBillingFlow(
            activity, billingFlowParams
        )
        return mapBillingResult(billingResult) { }
    }

    private suspend fun handlePurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase -> processPurchase(purchase) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.d("Purchase cancelled by user")
            }
            else -> {
                Timber.w(
                    "Purchase update failed: ${billingResult.debugMessage}"
                )
            }
        }
    }

    private suspend fun processPurchase(purchase: Purchase) {
        val entity = purchaseMapper.toEntity(purchase, clock)
        purchaseDao.upsert(entity)

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
            !purchase.isAcknowledged
        ) {
            acknowledgePurchase(purchase.purchaseToken)
        }
    }

    private fun <T> mapBillingResult(
        billingResult: BillingResult,
        onSuccess: () -> T
    ): Result<T, DomainError> {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                Result.Success(onSuccess())
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                Result.Failure(
                    DomainError.NetworkError("Billing service unavailable")
                )
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                Result.Failure(
                    DomainError.NetworkError("Billing unavailable")
                )
            BillingClient.BillingResponseCode.USER_CANCELED ->
                Result.Failure(
                    DomainError.ValidationError("Purchase cancelled")
                )
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                Result.Failure(
                    DomainError.ValidationError("Item already owned")
                )
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Timber.d("Billing developer error: %s", billingResult.debugMessage)
                Result.Failure(
                    DomainError.UnknownError("Billing configuration error")
                )
            }
            else -> {
                Timber.d("Billing error (%d): %s", billingResult.responseCode, billingResult.debugMessage)
                Result.Failure(
                    DomainError.UnknownError("Billing error (${billingResult.responseCode})")
                )
            }
        }
    }
}
