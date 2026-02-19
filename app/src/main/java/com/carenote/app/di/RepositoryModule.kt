package com.carenote.app.di

import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.data.local.dao.CareRecipientDao
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.EmergencyContactDao
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.InvitationDao
import com.carenote.app.data.local.dao.MemberDao
import com.carenote.app.data.local.dao.NoteCommentDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.PhotoDao
import com.carenote.app.data.mapper.CareRecipientMapper
import com.carenote.app.data.mapper.CalendarEventMapper
import com.carenote.app.data.mapper.EmergencyContactMapper
import com.carenote.app.data.mapper.HealthRecordMapper
import com.carenote.app.data.mapper.MedicationLogMapper
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.data.mapper.InvitationMapper
import com.carenote.app.data.mapper.MemberMapper
import com.carenote.app.data.mapper.NoteCommentMapper
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.data.mapper.PhotoMapper
import com.carenote.app.data.repository.SettingsRepositoryImpl
import com.carenote.app.data.repository.CareRecipientRepositoryImpl
import com.carenote.app.data.repository.CalendarEventRepositoryImpl
import com.carenote.app.data.repository.EmergencyContactRepositoryImpl
import com.carenote.app.data.repository.HealthRecordRepositoryImpl
import com.carenote.app.data.repository.MedicationLogRepositoryImpl
import com.carenote.app.data.repository.MedicationRepositoryImpl
import com.carenote.app.data.repository.InvitationRepositoryImpl
import com.carenote.app.data.repository.MemberRepositoryImpl
import com.carenote.app.data.repository.NoteCommentRepositoryImpl
import com.carenote.app.data.repository.NoteRepositoryImpl
import com.carenote.app.data.repository.PhotoRepositoryImpl
import com.carenote.app.data.local.ActiveCareRecipientPreferences
import com.carenote.app.data.repository.ActiveCareRecipientProviderImpl
import com.carenote.app.data.repository.SearchRepositoryImpl
import com.carenote.app.data.repository.TimelineRepositoryImpl
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.domain.repository.CareRecipientRepository
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.EmergencyContactRepository
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.InvitationRepository
import com.carenote.app.domain.repository.MemberRepository
import com.carenote.app.domain.repository.NoteCommentRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.SearchRepository
import com.carenote.app.domain.repository.PhotoRepository
import com.carenote.app.domain.repository.TimelineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideActiveCareRecipientProvider(
        careRecipientRepository: CareRecipientRepository,
        preferences: ActiveCareRecipientPreferences
    ): ActiveCareRecipientProvider {
        return ActiveCareRecipientProviderImpl(careRecipientRepository, preferences)
    }

    @Provides
    @Singleton
    fun provideMedicationRepository(
        medicationDao: MedicationDao,
        mapper: MedicationMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): MedicationRepository {
        return MedicationRepositoryImpl(
            medicationDao, mapper, activeRecipientProvider
        )
    }

    @Provides
    @Singleton
    fun provideMedicationLogRepository(
        medicationLogDao: MedicationLogDao,
        mapper: MedicationLogMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): MedicationLogRepository {
        return MedicationLogRepositoryImpl(
            medicationLogDao, mapper, activeRecipientProvider
        )
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao,
        mapper: NoteMapper,
        photoRepository: PhotoRepository,
        activeRecipientProvider: ActiveCareRecipientProvider,
        authRepository: AuthRepository
    ): NoteRepository {
        return NoteRepositoryImpl(
            noteDao, mapper, photoRepository,
            activeRecipientProvider, authRepository
        )
    }

    @Provides
    @Singleton
    fun provideNoteCommentRepository(
        noteCommentDao: NoteCommentDao,
        mapper: NoteCommentMapper,
        activeRecipientProvider: ActiveCareRecipientProvider,
        authRepository: AuthRepository
    ): NoteCommentRepository {
        return NoteCommentRepositoryImpl(
            noteCommentDao, mapper,
            activeRecipientProvider, authRepository
        )
    }

    @Provides
    @Singleton
    fun providePhotoRepository(
        photoDao: PhotoDao,
        mapper: PhotoMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): PhotoRepository {
        return PhotoRepositoryImpl(
            photoDao, mapper, activeRecipientProvider
        )
    }

    @Provides
    @Singleton
    fun provideHealthRecordRepository(
        healthRecordDao: HealthRecordDao,
        mapper: HealthRecordMapper,
        photoRepository: PhotoRepository,
        activeRecipientProvider: ActiveCareRecipientProvider,
        authRepository: AuthRepository
    ): HealthRecordRepository {
        return HealthRecordRepositoryImpl(
            healthRecordDao, mapper, photoRepository,
            activeRecipientProvider, authRepository
        )
    }

    @Provides
    @Singleton
    fun provideCalendarEventRepository(
        calendarEventDao: CalendarEventDao,
        mapper: CalendarEventMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): CalendarEventRepository {
        return CalendarEventRepositoryImpl(
            calendarEventDao, mapper, activeRecipientProvider
        )
    }

    @Provides
    @Singleton
    fun provideCareRecipientRepository(
        careRecipientDao: CareRecipientDao,
        mapper: CareRecipientMapper
    ): CareRecipientRepository {
        return CareRecipientRepositoryImpl(careRecipientDao, mapper)
    }

    @Provides
    @Singleton
    fun provideEmergencyContactRepository(
        emergencyContactDao: EmergencyContactDao,
        mapper: EmergencyContactMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): EmergencyContactRepository {
        return EmergencyContactRepositoryImpl(
            emergencyContactDao, mapper, activeRecipientProvider
        )
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataSource: SettingsDataSource
    ): SettingsRepository {
        return SettingsRepositoryImpl(dataSource)
    }

    @Provides
    @Singleton
    fun provideTimelineRepository(
        timelineRepositoryImpl: TimelineRepositoryImpl
    ): TimelineRepository {
        return timelineRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideMemberRepository(
        dao: MemberDao,
        mapper: MemberMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): MemberRepository {
        return MemberRepositoryImpl(
            dao, mapper, activeRecipientProvider
        )
    }

    @Provides
    @Singleton
    fun provideInvitationRepository(
        dao: InvitationDao,
        mapper: InvitationMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): InvitationRepository {
        return InvitationRepositoryImpl(
            dao, mapper, activeRecipientProvider
        )
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository {
        return searchRepositoryImpl
    }
}
