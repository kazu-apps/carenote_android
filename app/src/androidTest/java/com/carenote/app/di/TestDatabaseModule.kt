package com.carenote.app.di

import android.content.Context
import androidx.room.Room
import com.carenote.app.data.local.CareNoteDatabase
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.CareRecipientDao
import com.carenote.app.data.local.dao.EmergencyContactDao
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.PhotoDao
import com.carenote.app.data.local.dao.MemberDao
import com.carenote.app.data.local.dao.InvitationDao
import com.carenote.app.data.local.dao.NoteCommentDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.dao.PurchaseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Test database module that replaces the production DatabaseModule.
 * Provides an in-memory Room database without SQLCipher encryption
 * for faster and isolated E2E tests.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideTestDatabase(@ApplicationContext context: Context): CareNoteDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            CareNoteDatabase::class.java
        )
            .allowMainThreadQueries()
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
    fun provideMemberDao(database: CareNoteDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    @Singleton
    fun provideInvitationDao(database: CareNoteDatabase): InvitationDao {
        return database.invitationDao()
    }

    @Provides
    @Singleton
    fun provideNoteCommentDao(database: CareNoteDatabase): NoteCommentDao {
        return database.noteCommentDao()
    }

    @Provides
    @Singleton
    fun provideSyncMappingDao(database: CareNoteDatabase): SyncMappingDao {
        return database.syncMappingDao()
    }

    @Provides
    @Singleton
    fun providePurchaseDao(database: CareNoteDatabase): PurchaseDao {
        return database.purchaseDao()
    }
}
