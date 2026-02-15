package com.carenote.app.data.mapper

import com.android.billingclient.api.Purchase
import com.carenote.app.data.local.entity.PurchaseEntity
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.domain.util.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

class PurchaseMapper @Inject constructor() {

    fun toPremiumStatus(entity: PurchaseEntity?): PremiumStatus {
        if (entity == null) return PremiumStatus.Inactive

        return when (entity.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                val expiryTime = entity.expiryTime?.let { parseDateTime(it) }
                if (expiryTime != null && expiryTime.isBefore(LocalDateTime.now())) {
                    PremiumStatus.Expired
                } else {
                    PremiumStatus.Active(
                        productId = entity.productId,
                        purchaseToken = entity.purchaseToken,
                        expiryTime = expiryTime,
                        autoRenewing = entity.isAutoRenewing
                    )
                }
            }
            Purchase.PurchaseState.PENDING -> PremiumStatus.Pending
            else -> PremiumStatus.Inactive
        }
    }

    fun toEntity(purchase: Purchase, clock: Clock): PurchaseEntity {
        val productId = purchase.products.firstOrNull() ?: ""
        return PurchaseEntity(
            productId = productId,
            purchaseToken = purchase.purchaseToken,
            purchaseState = purchase.purchaseState,
            isAcknowledged = purchase.isAcknowledged,
            isAutoRenewing = purchase.isAutoRenewing,
            purchaseTime = formatDateTime(
                LocalDateTime.ofEpochSecond(
                    purchase.purchaseTime / 1000,
                    0,
                    ZoneOffset.UTC
                )
            ),
            updatedAt = formatDateTime(clock.now())
        )
    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    private fun parseDateTime(value: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
