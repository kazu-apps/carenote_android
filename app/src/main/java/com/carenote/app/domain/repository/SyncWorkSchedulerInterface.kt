package com.carenote.app.domain.repository

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo

/**
 * 同期ワークのスケジューリング機能を定義するインターフェース
 *
 * テスト時に Fake 実装を注入できるようにするために導入。
 */
interface SyncWorkSchedulerInterface {

    /**
     * 定期同期をスケジュールする
     */
    fun schedulePeriodicSync()

    /**
     * 即時同期をトリガーする
     */
    fun triggerImmediateSync()

    /**
     * すべての同期ワークをキャンセルする
     */
    fun cancelAllSyncWork()

    /**
     * 定期同期ワークの状態を観察する LiveData を取得
     */
    fun getSyncWorkInfo(): LiveData<List<WorkInfo>>

    /**
     * 即時同期ワークの状態を観察する LiveData を取得
     */
    fun getImmediateSyncWorkInfo(): LiveData<List<WorkInfo>>
}
