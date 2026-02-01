package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Query("SELECT * FROM calendar_events ORDER BY date ASC, start_time ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    fun getEventById(id: Long): Flow<CalendarEventEntity?>

    @Query("SELECT * FROM calendar_events WHERE date = :date ORDER BY start_time ASC")
    fun getEventsByDate(date: String): Flow<List<CalendarEventEntity>>

    @Query(
        "SELECT * FROM calendar_events " +
            "WHERE date >= :startDate AND date <= :endDate " +
            "ORDER BY date ASC, start_time ASC"
    )
    fun getEventsByDateRange(
        startDate: String,
        endDate: String
    ): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)
}
