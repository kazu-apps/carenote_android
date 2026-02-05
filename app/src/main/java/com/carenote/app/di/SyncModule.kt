package com.carenote.app.di

import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.local.entity.CalendarEventEntity
import com.carenote.app.data.local.entity.HealthRecordEntity
import com.carenote.app.data.local.entity.MedicationEntity
import com.carenote.app.data.local.entity.NoteEntity
import com.carenote.app.data.local.entity.TaskEntity
import com.carenote.app.data.mapper.CalendarEventMapper
import com.carenote.app.data.mapper.HealthRecordMapper
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.data.mapper.TaskMapper
import com.carenote.app.data.mapper.remote.CalendarEventRemoteMapper
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.mapper.remote.HealthRecordRemoteMapper
import com.carenote.app.data.mapper.remote.MedicationRemoteMapper
import com.carenote.app.data.mapper.remote.NoteRemoteMapper
import com.carenote.app.data.mapper.remote.TaskRemoteMapper
import com.carenote.app.data.repository.FirestoreSyncRepositoryImpl
import com.carenote.app.data.repository.sync.ConfigDrivenEntitySyncer
import com.carenote.app.data.repository.sync.EntitySyncer
import com.carenote.app.data.repository.sync.MedicationLogSyncer
import com.carenote.app.data.repository.sync.SyncerConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.repository.SyncRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    @Named("medication")
    fun provideMedicationSyncer(
        firestore: FirebaseFirestore,
        syncMappingDao: SyncMappingDao,
        timestampConverter: FirestoreTimestampConverter,
        dao: MedicationDao,
        entityMapper: MedicationMapper,
        remoteMapper: MedicationRemoteMapper
    ): EntitySyncer<*, *> {
        val config = SyncerConfig<MedicationEntity, Medication>(
            entityType = "medication",
            collectionPath = { "careRecipients/$it/medications" },
            getAllLocal = { dao.getAllMedications().first() },
            getLocalById = { dao.getMedicationById(it).first() },
            saveLocal = { dao.insertMedication(it) },
            deleteLocal = { dao.deleteMedication(it) },
            entityToDomain = { entityMapper.toDomain(it) },
            domainToEntity = { entityMapper.toEntity(it) },
            domainToRemote = { domain, meta -> remoteMapper.toRemote(domain, meta) },
            remoteToDomain = { remoteMapper.toDomain(it) },
            extractSyncMetadata = { remoteMapper.extractSyncMetadata(it) },
            getLocalId = { it.id },
            getUpdatedAt = { LocalDateTime.parse(it.updatedAt) }
        )
        return ConfigDrivenEntitySyncer(config, firestore, syncMappingDao, timestampConverter)
    }

    @Provides
    @Singleton
    @Named("note")
    fun provideNoteSyncer(
        firestore: FirebaseFirestore,
        syncMappingDao: SyncMappingDao,
        timestampConverter: FirestoreTimestampConverter,
        dao: NoteDao,
        entityMapper: NoteMapper,
        remoteMapper: NoteRemoteMapper
    ): EntitySyncer<*, *> {
        val config = SyncerConfig<NoteEntity, Note>(
            entityType = "note",
            collectionPath = { "careRecipients/$it/notes" },
            getAllLocal = { dao.getAllNotes().first() },
            getLocalById = { dao.getNoteById(it).first() },
            saveLocal = { dao.insertNote(it) },
            deleteLocal = { dao.deleteNote(it) },
            entityToDomain = { entityMapper.toDomain(it) },
            domainToEntity = { entityMapper.toEntity(it) },
            domainToRemote = { domain, meta -> remoteMapper.toRemote(domain, meta) },
            remoteToDomain = { remoteMapper.toDomain(it) },
            extractSyncMetadata = { remoteMapper.extractSyncMetadata(it) },
            getLocalId = { it.id },
            getUpdatedAt = { LocalDateTime.parse(it.updatedAt) }
        )
        return ConfigDrivenEntitySyncer(config, firestore, syncMappingDao, timestampConverter)
    }

    @Provides
    @Singleton
    @Named("healthRecord")
    fun provideHealthRecordSyncer(
        firestore: FirebaseFirestore,
        syncMappingDao: SyncMappingDao,
        timestampConverter: FirestoreTimestampConverter,
        dao: HealthRecordDao,
        entityMapper: HealthRecordMapper,
        remoteMapper: HealthRecordRemoteMapper
    ): EntitySyncer<*, *> {
        val config = SyncerConfig<HealthRecordEntity, HealthRecord>(
            entityType = "healthRecord",
            collectionPath = { "careRecipients/$it/healthRecords" },
            getAllLocal = { dao.getAllRecords().first() },
            getLocalById = { dao.getRecordById(it).first() },
            saveLocal = { dao.insertRecord(it) },
            deleteLocal = { dao.deleteRecord(it) },
            entityToDomain = { entityMapper.toDomain(it) },
            domainToEntity = { entityMapper.toEntity(it) },
            domainToRemote = { domain, meta -> remoteMapper.toRemote(domain, meta) },
            remoteToDomain = { remoteMapper.toDomain(it) },
            extractSyncMetadata = { remoteMapper.extractSyncMetadata(it) },
            getLocalId = { it.id },
            getUpdatedAt = { LocalDateTime.parse(it.updatedAt) }
        )
        return ConfigDrivenEntitySyncer(config, firestore, syncMappingDao, timestampConverter)
    }

    @Provides
    @Singleton
    @Named("calendarEvent")
    fun provideCalendarEventSyncer(
        firestore: FirebaseFirestore,
        syncMappingDao: SyncMappingDao,
        timestampConverter: FirestoreTimestampConverter,
        dao: CalendarEventDao,
        entityMapper: CalendarEventMapper,
        remoteMapper: CalendarEventRemoteMapper
    ): EntitySyncer<*, *> {
        val config = SyncerConfig<CalendarEventEntity, CalendarEvent>(
            entityType = "calendarEvent",
            collectionPath = { "careRecipients/$it/calendarEvents" },
            getAllLocal = { dao.getAllEvents().first() },
            getLocalById = { dao.getEventById(it).first() },
            saveLocal = { dao.insertEvent(it) },
            deleteLocal = { dao.deleteEvent(it) },
            entityToDomain = { entityMapper.toDomain(it) },
            domainToEntity = { entityMapper.toEntity(it) },
            domainToRemote = { domain, meta -> remoteMapper.toRemote(domain, meta) },
            remoteToDomain = { remoteMapper.toDomain(it) },
            extractSyncMetadata = { remoteMapper.extractSyncMetadata(it) },
            getLocalId = { it.id },
            getUpdatedAt = { LocalDateTime.parse(it.updatedAt) }
        )
        return ConfigDrivenEntitySyncer(config, firestore, syncMappingDao, timestampConverter)
    }

    @Provides
    @Singleton
    @Named("task")
    fun provideTaskSyncer(
        firestore: FirebaseFirestore,
        syncMappingDao: SyncMappingDao,
        timestampConverter: FirestoreTimestampConverter,
        dao: TaskDao,
        entityMapper: TaskMapper,
        remoteMapper: TaskRemoteMapper
    ): EntitySyncer<*, *> {
        val config = SyncerConfig<TaskEntity, Task>(
            entityType = "task",
            collectionPath = { "careRecipients/$it/tasks" },
            getAllLocal = { dao.getAllTasks().first() },
            getLocalById = { dao.getTaskById(it).first() },
            saveLocal = { dao.insertTask(it) },
            deleteLocal = { dao.deleteTask(it) },
            entityToDomain = { entityMapper.toDomain(it) },
            domainToEntity = { entityMapper.toEntity(it) },
            domainToRemote = { domain, meta -> remoteMapper.toRemote(domain, meta) },
            remoteToDomain = { remoteMapper.toDomain(it) },
            extractSyncMetadata = { remoteMapper.extractSyncMetadata(it) },
            getLocalId = { it.id },
            getUpdatedAt = { LocalDateTime.parse(it.updatedAt) }
        )
        return ConfigDrivenEntitySyncer(config, firestore, syncMappingDao, timestampConverter)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        settingsDataSource: SettingsDataSource,
        syncMappingDao: SyncMappingDao,
        @Named("medication") medicationSyncer: EntitySyncer<*, *>,
        medicationLogSyncer: MedicationLogSyncer,
        @Named("note") noteSyncer: EntitySyncer<*, *>,
        @Named("healthRecord") healthRecordSyncer: EntitySyncer<*, *>,
        @Named("calendarEvent") calendarEventSyncer: EntitySyncer<*, *>,
        @Named("task") taskSyncer: EntitySyncer<*, *>
    ): SyncRepository {
        return FirestoreSyncRepositoryImpl(
            settingsDataSource,
            syncMappingDao,
            medicationSyncer,
            medicationLogSyncer,
            noteSyncer,
            healthRecordSyncer,
            calendarEventSyncer,
            taskSyncer
        )
    }
}
