package com.carenote.app.data.remote

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirebasePurchaseVerifier(
    private val functionsLazy: dagger.Lazy<FirebaseFunctions>
) : PurchaseVerifier {

    override suspend fun verify(
        purchaseToken: String,
        productId: String
    ): Result<VerifiedPurchase, DomainError> {
        return try {
            val data = hashMapOf(
                "purchaseToken" to purchaseToken,
                "productId" to productId
            )
            val result = functionsLazy.get()
                .getHttpsCallable(AppConfig.Billing.VERIFY_PURCHASE_FUNCTION_NAME)
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val responseData = result.data as? Map<String, Any>
                ?: return Result.Failure(
                    DomainError.NetworkError("Invalid response from verification service")
                )

            val verified = VerifiedPurchase(
                isActive = responseData["isActive"] as? Boolean ?: false,
                productId = responseData["productId"] as? String ?: productId,
                expiryTimeMillis = (responseData["expiryTimeMillis"] as? Number)?.toLong() ?: 0L,
                autoRenewing = responseData["autoRenewing"] as? Boolean ?: false
            )
            Result.Success(verified)
        } catch (e: FirebaseFunctionsException) {
            Timber.w("Purchase verification failed: code=%s", e.code)
            Result.Failure(mapFunctionsError(e))
        } catch (e: Exception) {
            Timber.w(e, "Purchase verification failed unexpectedly")
            Result.Failure(DomainError.NetworkError("Purchase verification unavailable"))
        }
    }

    private fun mapFunctionsError(e: FirebaseFunctionsException): DomainError {
        return when (e.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                DomainError.UnauthorizedError("Authentication required for purchase verification")
            FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                DomainError.ValidationError("Invalid purchase data")
            FirebaseFunctionsException.Code.NOT_FOUND ->
                DomainError.NotFoundError("Purchase not found")
            FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                DomainError.UnauthorizedError("Permission denied for purchase verification")
            else ->
                DomainError.NetworkError("Purchase verification service error")
        }
    }
}
