package com.carenote.app.di

import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.mapper.MedicationLogMapper
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.data.repository.MedicationLogRepositoryImpl
import com.carenote.app.data.repository.MedicationRepositoryImpl
import com.carenote.app.data.repository.NoteRepositoryImpl
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.NoteRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

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
}
