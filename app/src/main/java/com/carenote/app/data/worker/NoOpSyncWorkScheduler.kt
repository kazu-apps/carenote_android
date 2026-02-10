package com.carenote.app.data.worker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import com.carenote.app.domain.repository.SyncWorkSchedulerInterface
import timber.log.Timber

class NoOpSyncWorkScheduler : SyncWorkSchedulerInterface {

    override fun schedulePeriodicSync() {
        Timber.d("NoOpSyncWorkScheduler: schedulePeriodicSync skipped (Firebase unavailable)")
    }

    override fun triggerImmediateSync() {
        Timber.d("NoOpSyncWorkScheduler: triggerImmediateSync skipped (Firebase unavailable)")
    }

    override fun cancelAllSyncWork() {
        Timber.d("NoOpSyncWorkScheduler: cancelAllSyncWork skipped (Firebase unavailable)")
    }

    override fun getSyncWorkInfo(): LiveData<List<WorkInfo>> {
        return MutableLiveData(emptyList())
    }

    override fun getImmediateSyncWorkInfo(): LiveData<List<WorkInfo>> {
        return MutableLiveData(emptyList())
    }
}
