package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications WHERE care_recipient_id = :careRecipientId ORDER BY name ASC")
    fun getAllMedications(careRecipientId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: Long): Flow<MedicationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: Long)

    @Query("SELECT * FROM medications WHERE care_recipient_id = :careRecipientId AND (name LIKE '%' || :query || '%' OR dosage LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchMedications(query: String, careRecipientId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE updated_at > :lastSyncTime")
    suspend fun getModifiedSince(lastSyncTime: String): List<MedicationEntity>

    @Query(
        """
        UPDATE medications
        SET current_stock = CASE
            WHEN current_stock >= :amount THEN current_stock - :amount
            ELSE 0
        END,
        updated_at = :updatedAt
        WHERE id = :id AND current_stock IS NOT NULL
        """
    )
    suspend fun decrementStock(id: Long, amount: Int, updatedAt: String)
}
