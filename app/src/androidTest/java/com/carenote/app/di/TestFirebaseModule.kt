package com.carenote.app.di

import android.content.Context
import androidx.work.WorkManager
import com.carenote.app.data.worker.SyncWorkSchedulerInterface
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.SyncRepository
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeSyncRepository
import com.carenote.app.fakes.FakeSyncWorkScheduler
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

/**
 * E2E テスト用 Firebase/Sync モジュール
 *
 * 本番の FirebaseModule, SyncModule, WorkerModule を Fake 実装で置換し、
 * Firebase SDK / WorkManager への依存を排除する。
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseModule::class, SyncModule::class, WorkerModule::class]
)
object TestFirebaseModule {

    @Provides
    @Singleton
    fun provideFakeAuthRepository(): FakeAuthRepository {
        return FakeAuthRepository()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(fake: FakeAuthRepository): AuthRepository {
        return fake
    }

    @Provides
    @Singleton
    fun provideFakeSyncRepository(): FakeSyncRepository {
        return FakeSyncRepository()
    }

    @Provides
    @Singleton
    fun provideSyncRepository(fake: FakeSyncRepository): SyncRepository {
        return fake
    }

    @Provides
    @Singleton
    fun provideFakeSyncWorkScheduler(): FakeSyncWorkScheduler {
        return FakeSyncWorkScheduler()
    }

    @Provides
    @Singleton
    fun provideSyncWorkSchedulerInterface(fake: FakeSyncWorkScheduler): SyncWorkSchedulerInterface {
        return fake
    }

    /**
     * テスト用 FirebaseFirestore モック
     *
     * SyncWorker が依存しているため提供。実際の Firestore への接続は行わない。
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return mockk(relaxed = true)
    }

    /**
     * テスト用 FirebaseMessaging モック
     *
     * CareNoteMessagingService が依存しているため提供。実際の FCM への接続は行わない。
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return mockk(relaxed = true)
    }

    /**
     * テスト用 WorkManager
     *
     * テスト環境での WorkManager インスタンスを提供。
     */
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
