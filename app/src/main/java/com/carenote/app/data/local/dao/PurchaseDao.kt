package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.carenote.app.data.local.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY purchase_time DESC LIMIT 1")
    fun getLatestPurchase(): Flow<PurchaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(purchase: PurchaseEntity): Long

    @Query("DELETE FROM purchases")
    suspend fun deleteAll()
}
