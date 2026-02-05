package com.carenote.app.di

import android.content.Context
import androidx.work.WorkManager
import com.carenote.app.data.worker.SyncWorkScheduler
import com.carenote.app.data.worker.SyncWorkSchedulerInterface
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * WorkManager 関連の依存を提供する Hilt モジュール
 *
 * ## 提供する依存
 * - WorkManager: バックグラウンドタスクの実行管理
 * - SyncWorkSchedulerInterface: 同期スケジュール管理
 *
 * ## 関連クラス
 * - SyncWorker: 定期同期を実行するワーカー（data/worker/SyncWorker.kt）
 * - SyncWorkScheduler: 同期スケジュール管理（data/worker/SyncWorkScheduler.kt、@Inject で提供）
 *
 * @see com.carenote.app.data.worker.SyncWorker
 * @see com.carenote.app.data.worker.SyncWorkScheduler
 */
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
        workManager: WorkManager
    ): SyncWorkSchedulerInterface {
        return SyncWorkScheduler(workManager)
    }
}
