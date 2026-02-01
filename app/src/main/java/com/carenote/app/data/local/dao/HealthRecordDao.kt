package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.HealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {

    @Query("SELECT * FROM health_records ORDER BY recorded_at DESC")
    fun getAllRecords(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE id = :id")
    fun getRecordById(id: Long): Flow<HealthRecordEntity?>

    @Query(
        "SELECT * FROM health_records " +
            "WHERE recorded_at >= :start AND recorded_at <= :end " +
            "ORDER BY recorded_at DESC"
    )
    fun getRecordsByDateRange(start: String, end: String): Flow<List<HealthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HealthRecordEntity): Long

    @Update
    suspend fun updateRecord(record: HealthRecordEntity)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)
}
