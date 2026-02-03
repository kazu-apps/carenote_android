package com.carenote.app.di

import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.mapper.CalendarEventMapper
import com.carenote.app.data.mapper.HealthRecordMapper
import com.carenote.app.data.mapper.MedicationLogMapper
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.data.mapper.TaskMapper
import com.carenote.app.data.repository.SettingsRepositoryImpl
import com.carenote.app.data.repository.CalendarEventRepositoryImpl
import com.carenote.app.data.repository.HealthRecordRepositoryImpl
import com.carenote.app.data.repository.MedicationLogRepositoryImpl
import com.carenote.app.data.repository.MedicationRepositoryImpl
import com.carenote.app.data.repository.NoteRepositoryImpl
import com.carenote.app.data.repository.TaskRepositoryImpl
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.TaskRepository
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
    fun provideMedicationRepository(
        medicationDao: MedicationDao,
        mapper: MedicationMapper
    ): MedicationRepository {
        return MedicationRepositoryImpl(medicationDao, mapper)
    }

    @Provides
    @Singleton
    fun provideMedicationLogRepository(
        medicationLogDao: MedicationLogDao,
        mapper: MedicationLogMapper
    ): MedicationLogRepository {
        return MedicationLogRepositoryImpl(medicationLogDao, mapper)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao,
        mapper: NoteMapper
    ): NoteRepository {
        return NoteRepositoryImpl(noteDao, mapper)
    }

    @Provides
    @Singleton
    fun provideHealthRecordRepository(
        healthRecordDao: HealthRecordDao,
        mapper: HealthRecordMapper
    ): HealthRecordRepository {
        return HealthRecordRepositoryImpl(healthRecordDao, mapper)
    }

    @Provides
    @Singleton
    fun provideCalendarEventRepository(
        calendarEventDao: CalendarEventDao,
        mapper: CalendarEventMapper
    ): CalendarEventRepository {
        return CalendarEventRepositoryImpl(calendarEventDao, mapper)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        mapper: TaskMapper
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao, mapper)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataSource: SettingsDataSource
    ): SettingsRepository {
        return SettingsRepositoryImpl(dataSource)
    }
}
