package com.carenote.app.fakes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import com.carenote.app.domain.repository.SyncWorkSchedulerInterface

/**
 * SyncWorkScheduler のテスト用 Fake
 *
 * WorkManager への依存を排除し、テストでの振る舞いを制御可能にする。
 * 実際の SyncWorkScheduler と同じ public API を持つが、
 * 内部では MutableLiveData を使って状態を管理する。
 */
class FakeSyncWorkScheduler : SyncWorkSchedulerInterface {

    private val _syncWorkInfo = MutableLiveData<List<WorkInfo>>(emptyList())
    private val _immediateSyncWorkInfo = MutableLiveData<List<WorkInfo>>(emptyList())

    // テストで呼び出し回数を検証するためのカウンター
    var schedulePeriodicSyncCallCount = 0
        private set

    var triggerImmediateSyncCallCount = 0
        private set

    var cancelAllSyncWorkCallCount = 0
        private set

    /**
     * 定期同期をスケジュールする（テスト用スタブ）
     */
    override fun schedulePeriodicSync() {
        schedulePeriodicSyncCallCount++
    }

    /**
     * 即時同期をトリガーする（テスト用スタブ）
     */
    override fun triggerImmediateSync() {
        triggerImmediateSyncCallCount++
    }

    /**
     * すべての同期ワークをキャンセルする（テスト用スタブ）
     */
    override fun cancelAllSyncWork() {
        cancelAllSyncWorkCallCount++
    }

    /**
     * 定期同期ワークの状態を観察する LiveData を取得
     */
    override fun getSyncWorkInfo(): LiveData<List<WorkInfo>> = _syncWorkInfo

    /**
     * 即時同期ワークの状態を観察する LiveData を取得
     */
    override fun getImmediateSyncWorkInfo(): LiveData<List<WorkInfo>> = _immediateSyncWorkInfo

    /**
     * テスト用: 定期同期ワークの状態を設定
     */
    fun setSyncWorkInfo(workInfoList: List<WorkInfo>) {
        _syncWorkInfo.value = workInfoList
    }

    /**
     * テスト用: 即時同期ワークの状態を設定
     */
    fun setImmediateSyncWorkInfo(workInfoList: List<WorkInfo>) {
        _immediateSyncWorkInfo.value = workInfoList
    }

    /**
     * テスト用: すべての状態をリセット
     */
    fun clear() {
        _syncWorkInfo.value = emptyList()
        _immediateSyncWorkInfo.value = emptyList()
        schedulePeriodicSyncCallCount = 0
        triggerImmediateSyncCallCount = 0
        cancelAllSyncWorkCallCount = 0
    }
}
