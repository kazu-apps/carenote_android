package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [Index(value = ["purchase_token"], unique = true)]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "product_id")
    val productId: String,

    @ColumnInfo(name = "purchase_token")
    val purchaseToken: String,

    @ColumnInfo(name = "purchase_state")
    val purchaseState: Int,

    @ColumnInfo(name = "is_acknowledged")
    val isAcknowledged: Boolean = false,

    @ColumnInfo(name = "is_auto_renewing")
    val isAutoRenewing: Boolean = false,

    @ColumnInfo(name = "purchase_time")
    val purchaseTime: String,

    @ColumnInfo(name = "expiry_time")
    val expiryTime: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
