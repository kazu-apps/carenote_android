package com.carenote.app.di

import android.content.Context
import androidx.room.Room
import com.carenote.app.data.local.CareNoteDatabase
import com.carenote.app.data.local.DatabaseEncryptionMigrator
import com.carenote.app.data.local.DatabasePassphraseManager
import com.carenote.app.data.local.DatabaseRecoveryHelper
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.local.migration.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        passphraseManager: DatabasePassphraseManager,
        encryptionMigrator: DatabaseEncryptionMigrator,
        recoveryHelper: DatabaseRecoveryHelper
    ): CareNoteDatabase {
        System.loadLibrary("sqlcipher")

        val passphrase = passphraseManager.getOrCreatePassphrase(context)
        val dbFile = context.getDatabasePath(CareNoteDatabase.DATABASE_NAME)

        encryptionMigrator.migrateIfNeeded(dbFile, passphrase)
        recoveryHelper.recoverIfNeeded(dbFile, passphrase)

        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            CareNoteDatabase::class.java,
            CareNoteDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .addMigrations(*Migrations.all())
            .build()
    }

    @Provides
    @Singleton
    fun provideMedicationDao(database: CareNoteDatabase): MedicationDao {
        return database.medicationDao()
    }

    @Provides
    @Singleton
    fun provideMedicationLogDao(database: CareNoteDatabase): MedicationLogDao {
        return database.medicationLogDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: CareNoteDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideHealthRecordDao(database: CareNoteDatabase): HealthRecordDao {
        return database.healthRecordDao()
    }

    @Provides
    @Singleton
    fun provideCalendarEventDao(database: CareNoteDatabase): CalendarEventDao {
        return database.calendarEventDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: CareNoteDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideSyncMappingDao(database: CareNoteDatabase): SyncMappingDao {
        return database.syncMappingDao()
    }
}
