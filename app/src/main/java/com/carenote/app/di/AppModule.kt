package com.carenote.app.di

import com.carenote.app.data.export.HealthRecordCsvExporter
import com.carenote.app.data.export.HealthRecordPdfExporter
import com.carenote.app.data.export.MedicationLogCsvExporter
import com.carenote.app.data.export.MedicationLogPdfExporter
import com.carenote.app.data.export.NoteCsvExporter
import com.carenote.app.data.export.NotePdfExporter
import com.carenote.app.data.export.TaskCsvExporter
import com.carenote.app.data.export.TaskPdfExporter
import com.carenote.app.data.local.ImageCompressor
import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.data.local.dao.CareRecipientDao
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.EmergencyContactDao
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.NoteCommentDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.PhotoDao
import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.mapper.CareRecipientMapper
import com.carenote.app.data.mapper.CalendarEventMapper
import com.carenote.app.data.mapper.EmergencyContactMapper
import com.carenote.app.data.mapper.HealthRecordMapper
import com.carenote.app.data.mapper.MedicationLogMapper
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.data.mapper.NoteCommentMapper
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.data.mapper.PhotoMapper
import com.carenote.app.data.mapper.TaskMapper
import com.carenote.app.data.repository.SettingsRepositoryImpl
import com.carenote.app.data.repository.CareRecipientRepositoryImpl
import com.carenote.app.data.repository.CalendarEventRepositoryImpl
import com.carenote.app.data.repository.EmergencyContactRepositoryImpl
import com.carenote.app.data.repository.HealthRecordRepositoryImpl
import com.carenote.app.data.repository.MedicationLogRepositoryImpl
import com.carenote.app.data.repository.MedicationRepositoryImpl
import com.carenote.app.data.repository.NoteCommentRepositoryImpl
import com.carenote.app.data.repository.NoteRepositoryImpl
import com.carenote.app.data.repository.PhotoRepositoryImpl
import com.carenote.app.data.repository.TaskRepositoryImpl
import com.carenote.app.data.repository.ActiveCareRecipientProviderImpl
import com.carenote.app.data.repository.SearchRepositoryImpl
import com.carenote.app.data.repository.TimelineRepositoryImpl
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.HealthRecordCsvExporterInterface
import com.carenote.app.domain.repository.HealthRecordPdfExporterInterface
import com.carenote.app.domain.repository.MedicationLogCsvExporterInterface
import com.carenote.app.domain.repository.MedicationLogPdfExporterInterface
import com.carenote.app.domain.repository.NoteCsvExporterInterface
import com.carenote.app.domain.repository.NotePdfExporterInterface
import com.carenote.app.domain.repository.TaskCsvExporterInterface
import com.carenote.app.domain.repository.TaskPdfExporterInterface
import com.carenote.app.domain.repository.ImageCompressorInterface
import com.carenote.app.domain.util.Clock
import com.carenote.app.domain.util.SystemClock
import com.carenote.app.data.local.NotificationCountDataSource
import com.carenote.app.data.repository.PremiumFeatureGuardImpl
import com.carenote.app.domain.repository.BillingRepository
import com.carenote.app.domain.repository.PremiumFeatureGuard
import com.carenote.app.ui.util.RootDetectionChecker
import com.carenote.app.ui.util.RootDetector
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.domain.repository.CareRecipientRepository
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.EmergencyContactRepository
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.NoteCommentRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.SearchRepository
import com.carenote.app.domain.repository.PhotoRepository
import com.carenote.app.domain.repository.TaskRepository
import com.carenote.app.domain.repository.TimelineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideActiveCareRecipientProvider(
        careRecipientRepository: CareRecipientRepository
    ): ActiveCareRecipientProvider {
        return ActiveCareRecipientProviderImpl(careRecipientRepository)
    }

    @Provides
    @Singleton
    fun provideMedicationRepository(
        medicationDao: MedicationDao,
        mapper: MedicationMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): MedicationRepository {
        return MedicationRepositoryImpl(medicationDao, mapper, activeRecipientProvider)
    }

    @Provides
    @Singleton
    fun provideMedicationLogRepository(
        medicationLogDao: MedicationLogDao,
        mapper: MedicationLogMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): MedicationLogRepository {
        return MedicationLogRepositoryImpl(medicationLogDao, mapper, activeRecipientProvider)
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
        return NoteRepositoryImpl(noteDao, mapper, photoRepository, activeRecipientProvider, authRepository)
    }

    @Provides
    @Singleton
    fun provideNoteCommentRepository(
        noteCommentDao: NoteCommentDao,
        mapper: NoteCommentMapper,
        activeRecipientProvider: ActiveCareRecipientProvider,
        authRepository: AuthRepository
    ): NoteCommentRepository {
        return NoteCommentRepositoryImpl(noteCommentDao, mapper, activeRecipientProvider, authRepository)
    }

    @Provides
    @Singleton
    fun providePhotoRepository(
        photoDao: PhotoDao,
        mapper: PhotoMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): PhotoRepository {
        return PhotoRepositoryImpl(photoDao, mapper, activeRecipientProvider)
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
        return HealthRecordRepositoryImpl(healthRecordDao, mapper, photoRepository, activeRecipientProvider, authRepository)
    }

    @Provides
    @Singleton
    fun provideCalendarEventRepository(
        calendarEventDao: CalendarEventDao,
        mapper: CalendarEventMapper,
        activeRecipientProvider: ActiveCareRecipientProvider
    ): CalendarEventRepository {
        return CalendarEventRepositoryImpl(calendarEventDao, mapper, activeRecipientProvider)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        mapper: TaskMapper,
        activeRecipientProvider: ActiveCareRecipientProvider,
        authRepository: AuthRepository
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao, mapper, activeRecipientProvider, authRepository)
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
        return EmergencyContactRepositoryImpl(emergencyContactDao, mapper, activeRecipientProvider)
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
    fun provideImageCompressor(
        imageCompressor: ImageCompressor
    ): ImageCompressorInterface {
        return imageCompressor
    }

    @Provides
    @Singleton
    fun provideHealthRecordCsvExporter(
        csvExporter: HealthRecordCsvExporter
    ): HealthRecordCsvExporterInterface {
        return csvExporter
    }

    @Provides
    @Singleton
    fun provideHealthRecordPdfExporter(
        pdfExporter: HealthRecordPdfExporter
    ): HealthRecordPdfExporterInterface {
        return pdfExporter
    }

    @Provides
    @Singleton
    fun provideMedicationLogCsvExporter(
        csvExporter: MedicationLogCsvExporter
    ): MedicationLogCsvExporterInterface {
        return csvExporter
    }

    @Provides
    @Singleton
    fun provideMedicationLogPdfExporter(
        pdfExporter: MedicationLogPdfExporter
    ): MedicationLogPdfExporterInterface {
        return pdfExporter
    }

    @Provides
    @Singleton
    fun provideTaskCsvExporter(
        csvExporter: TaskCsvExporter
    ): TaskCsvExporterInterface {
        return csvExporter
    }

    @Provides
    @Singleton
    fun provideTaskPdfExporter(
        pdfExporter: TaskPdfExporter
    ): TaskPdfExporterInterface {
        return pdfExporter
    }

    @Provides
    @Singleton
    fun provideNoteCsvExporter(
        csvExporter: NoteCsvExporter
    ): NoteCsvExporterInterface {
        return csvExporter
    }

    @Provides
    @Singleton
    fun provideNotePdfExporter(
        pdfExporter: NotePdfExporter
    ): NotePdfExporterInterface {
        return pdfExporter
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository {
        return searchRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideClock(): Clock = SystemClock()

    @Provides
    @Singleton
    fun provideRootDetectionChecker(): RootDetectionChecker = RootDetector()

    @Provides
    @Singleton
    fun providePremiumFeatureGuard(
        billingRepository: BillingRepository,
        notificationCountDataSource: NotificationCountDataSource,
        clock: Clock
    ): PremiumFeatureGuard {
        return PremiumFeatureGuardImpl(billingRepository, notificationCountDataSource, clock)
    }
}
