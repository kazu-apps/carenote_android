package com.carenote.app.di

import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.repository.FirestoreSyncRepositoryImpl
import com.carenote.app.data.repository.sync.CalendarEventSyncer
import com.carenote.app.data.repository.sync.HealthRecordSyncer
import com.carenote.app.data.repository.sync.MedicationLogSyncer
import com.carenote.app.data.repository.sync.MedicationSyncer
import com.carenote.app.data.repository.sync.NoteSyncer
import com.carenote.app.data.repository.sync.TaskSyncer
import com.carenote.app.domain.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncRepository(
        settingsDataSource: SettingsDataSource,
        syncMappingDao: SyncMappingDao,
        medicationSyncer: MedicationSyncer,
        medicationLogSyncer: MedicationLogSyncer,
        noteSyncer: NoteSyncer,
        healthRecordSyncer: HealthRecordSyncer,
        calendarEventSyncer: CalendarEventSyncer,
        taskSyncer: TaskSyncer
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
