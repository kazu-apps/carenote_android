package com.carenote.app.domain.model

data class ProductInfo(
    val productId: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
    val priceMicros: Long,
    val billingPeriod: String
)
