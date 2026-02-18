package com.carenote.app.di

import android.content.Context
import androidx.room.Room
import com.carenote.app.data.local.CareNoteDatabase
import com.carenote.app.data.local.DatabaseEncryptionMigrator
import com.carenote.app.data.local.DatabasePassphraseManager
import com.carenote.app.data.local.DatabaseRecoveryHelper
import com.carenote.app.data.local.dao.CareRecipientDao
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.EmergencyContactDao
import com.carenote.app.data.local.dao.PhotoDao
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.InvitationDao
import com.carenote.app.data.local.dao.MemberDao
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.NoteCommentDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.PurchaseDao
import com.carenote.app.data.local.dao.SyncMappingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import timber.log.Timber
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

        try {
            encryptionMigrator.migrateIfNeeded(dbFile, passphrase)
            recoveryHelper.recoverIfNeeded(dbFile, passphrase)
        } catch (e: Exception) {
            Timber.e("Database pre-initialization failed: $e")
            recoveryHelper.deleteDatabaseFiles(dbFile)
        }

        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            CareNoteDatabase::class.java,
            CareNoteDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
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
    fun provideSyncMappingDao(database: CareNoteDatabase): SyncMappingDao {
        return database.syncMappingDao()
    }

    @Provides
    @Singleton
    fun provideCareRecipientDao(database: CareNoteDatabase): CareRecipientDao {
        return database.careRecipientDao()
    }

    @Provides
    @Singleton
    fun providePhotoDao(database: CareNoteDatabase): PhotoDao {
        return database.photoDao()
    }

    @Provides
    @Singleton
    fun provideEmergencyContactDao(database: CareNoteDatabase): EmergencyContactDao {
        return database.emergencyContactDao()
    }

    @Provides
    @Singleton
    fun provideNoteCommentDao(database: CareNoteDatabase): NoteCommentDao {
        return database.noteCommentDao()
    }

    @Provides
    @Singleton
    fun providePurchaseDao(database: CareNoteDatabase): PurchaseDao {
        return database.purchaseDao()
    }

    @Provides
    @Singleton
    fun provideMemberDao(database: CareNoteDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    @Singleton
    fun provideInvitationDao(database: CareNoteDatabase): InvitationDao {
        return database.invitationDao()
    }
}
