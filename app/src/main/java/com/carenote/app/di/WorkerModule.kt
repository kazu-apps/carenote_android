package com.carenote.app.di

import android.content.Context
import androidx.work.WorkManager
import com.carenote.app.data.worker.MedicationReminderScheduler
import com.carenote.app.data.worker.NoOpSyncWorkScheduler
import com.carenote.app.data.worker.SyncWorkScheduler
import com.carenote.app.data.worker.TaskReminderScheduler
import com.carenote.app.domain.repository.MedicationReminderSchedulerInterface
import com.carenote.app.domain.repository.SyncWorkSchedulerInterface
import com.carenote.app.domain.repository.TaskReminderSchedulerInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSyncWorkScheduler(
        availability: FirebaseAvailability,
        workManager: WorkManager
    ): SyncWorkSchedulerInterface {
        if (!availability.isAvailable) return NoOpSyncWorkScheduler()
        return SyncWorkScheduler(workManager)
    }

    @Provides
    @Singleton
    fun provideMedicationReminderScheduler(
        workManager: WorkManager
    ): MedicationReminderSchedulerInterface {
        return MedicationReminderScheduler(workManager)
    }

    @Provides
    @Singleton
    fun provideTaskReminderScheduler(
        workManager: WorkManager
    ): TaskReminderSchedulerInterface {
        return TaskReminderScheduler(workManager)
    }
}
