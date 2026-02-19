package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.carenote.app.data.local.entity.CareRecipientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareRecipientDao {

    @Query("SELECT * FROM care_recipients LIMIT 1")
    fun getCareRecipient(): Flow<CareRecipientEntity?>

    @Query("SELECT * FROM care_recipients ORDER BY id ASC")
    fun getAllCareRecipients(): Flow<List<CareRecipientEntity>>

    @Query("SELECT * FROM care_recipients WHERE id = :id")
    suspend fun getCareRecipientById(id: Long): CareRecipientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: CareRecipientEntity): Long

    @Query("UPDATE care_recipients SET firestore_id = :firestoreId WHERE id = :id")
    suspend fun updateFirestoreId(id: Long, firestoreId: String)
}
