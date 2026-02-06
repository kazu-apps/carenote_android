package com.carenote.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.carenote.app.data.local.converter.DateTimeConverters
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.local.entity.CalendarEventEntity
import com.carenote.app.data.local.entity.HealthRecordEntity
import com.carenote.app.data.local.entity.MedicationEntity
import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.data.local.entity.NoteEntity
import com.carenote.app.data.local.entity.SyncMappingEntity
import com.carenote.app.data.local.entity.TaskEntity

@Database(
    entities = [
        MedicationEntity::class,
        MedicationLogEntity::class,
        NoteEntity::class,
        HealthRecordEntity::class,
        CalendarEventEntity::class,
        TaskEntity::class,
        SyncMappingEntity::class
    ],
    version = 10,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class CareNoteDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun medicationLogDao(): MedicationLogDao
    abstract fun noteDao(): NoteDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun taskDao(): TaskDao
    abstract fun syncMappingDao(): SyncMappingDao

    companion object {
        const val DATABASE_NAME = "carenote_database"
    }
}
