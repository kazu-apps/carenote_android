package com.carenote.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.carenote.app.data.local.converter.DateTimeConverters
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.entity.MedicationEntity
import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.data.local.entity.NoteEntity

@Database(
    entities = [
        MedicationEntity::class,
        MedicationLogEntity::class,
        NoteEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class CareNoteDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun medicationLogDao(): MedicationLogDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val DATABASE_NAME = "carenote_database"
    }
}
