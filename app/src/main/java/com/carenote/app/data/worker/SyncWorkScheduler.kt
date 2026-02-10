package com.carenote.app.data.worker

import androidx.lifecycle.LiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.SyncWorkSchedulerInterface
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 同期ワークのスケジューリングを管理するクラス
 *
 * ## 機能
 * - 定期同期のスケジュール（15分間隔）
 * - 即時同期のトリガー（手動同期用）
 * - 同期ワークのキャンセル（ログアウト時）
 * - 同期状態の観察（UI表示用）
 *
 * ## 使用例
 * ```kotlin
 * // ログイン成功時
 * syncWorkScheduler.schedulePeriodicSync()
 *
 * // 手動同期ボタン押下時
 * syncWorkScheduler.triggerImmediateSync()
 *
 * // ログアウト時
 * syncWorkScheduler.cancelAllSyncWork()
 *
 * // UI で同期状態を観察
 * syncWorkScheduler.getSyncWorkInfo().observe(lifecycleOwner) { workInfoList ->
 *     val isRunning = workInfoList.any { it.state == WorkInfo.State.RUNNING }
 *     updateSyncIndicator(isRunning)
 * }
 * ```
 */
class SyncWorkScheduler(
    private val workManager: WorkManager
) : SyncWorkSchedulerInterface {

    /**
     * 定期同期をスケジュールする
     *
     * ネットワーク接続が必要な制約付きで、15分間隔の定期実行をスケジュール。
     * 既存のスケジュールがある場合は保持（KEEP ポリシー）。
     *
     * @see AppConfig.Sync.SYNC_INTERVAL_MINUTES
     */
    override fun schedulePeriodicSync() {
        Timber.d("SyncWorkScheduler: Scheduling periodic sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            AppConfig.Sync.SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(
                AppConfig.Sync.SYNC_INITIAL_DELAY_MINUTES,
                TimeUnit.MINUTES
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                AppConfig.Sync.SYNC_BACKOFF_INITIAL_MS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

        Timber.d("SyncWorkScheduler: Periodic sync scheduled")
    }

    /**
     * 即時同期をトリガーする
     *
     * 設定画面などから手動で同期を実行したい場合に使用。
     * ネットワーク接続が必要な制約付き。
     */
    override fun triggerImmediateSync() {
        Timber.d("SyncWorkScheduler: Triggering immediate sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                AppConfig.Sync.SYNC_BACKOFF_INITIAL_MS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )

        Timber.d("SyncWorkScheduler: Immediate sync enqueued")
    }

    /**
     * すべての同期ワークをキャンセルする
     *
     * ログアウト時に呼び出して、定期同期と即時同期の両方をキャンセル。
     */
    override fun cancelAllSyncWork() {
        Timber.d("SyncWorkScheduler: Cancelling all sync work")

        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        workManager.cancelUniqueWork(SyncWorker.IMMEDIATE_WORK_NAME)

        Timber.d("SyncWorkScheduler: All sync work cancelled")
    }

    /**
     * 定期同期ワークの状態を観察する LiveData を取得
     *
     * UI レイヤーで同期中インジケーターを表示するために使用。
     *
     * @return 同期ワークの状態を持つ LiveData
     */
    override fun getSyncWorkInfo(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
    }

    /**
     * 即時同期ワークの状態を観察する LiveData を取得
     *
     * @return 即時同期ワークの状態を持つ LiveData
     */
    override fun getImmediateSyncWorkInfo(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(SyncWorker.IMMEDIATE_WORK_NAME)
    }
}
