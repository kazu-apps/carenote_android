package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationLogDao {

    @Query("SELECT * FROM medication_logs WHERE medication_id = :medicationId ORDER BY scheduled_at DESC")
    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLogEntity>>

    @Query(
        "SELECT * FROM medication_logs WHERE care_recipient_id = :careRecipientId AND scheduled_at >= :startOfDay AND scheduled_at < :endOfDay " +
            "ORDER BY scheduled_at ASC"
    )
    fun getLogsForDateRange(startOfDay: String, endOfDay: String, careRecipientId: Long): Flow<List<MedicationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLogEntity): Long

    @Update
    suspend fun updateLog(log: MedicationLogEntity)

    @Query("DELETE FROM medication_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)

    @Query(
        "SELECT EXISTS(" +
            "SELECT 1 FROM medication_logs " +
            "WHERE medication_id = :medicationId " +
            "AND status = 'TAKEN' " +
            "AND scheduled_at >= :startOfDay " +
            "AND scheduled_at < :endOfDay " +
            "AND (:timing IS NULL OR timing = :timing)" +
            ")"
    )
    suspend fun hasTakenLogForDateRange(
        medicationId: Long,
        startOfDay: String,
        endOfDay: String,
        timing: String?
    ): Boolean

    @Query("SELECT * FROM medication_logs WHERE care_recipient_id = :careRecipientId ORDER BY scheduled_at DESC")
    fun getAllLogs(careRecipientId: Long): Flow<List<MedicationLogEntity>>
}
