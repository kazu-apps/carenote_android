package com.carenote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.domain.common.SyncState
import com.carenote.app.domain.repository.SyncRepository
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    init {
        observeSyncState()
    }

    private fun observeSyncState() {
        viewModelScope.launch {
            var hasSyncedAtLeastOnce = false
            syncRepository.syncState.collect { state ->
                when (state) {
                    is SyncState.Syncing -> hasSyncedAtLeastOnce = true
                    is SyncState.Success -> {
                        if (hasSyncedAtLeastOnce) {
                            snackbarController.showMessage(R.string.sync_complete_snackbar)
                        }
                    }
                    is SyncState.Error -> {
                        snackbarController.showMessage(R.string.sync_error_snackbar)
                    }
                    is SyncState.Idle -> { /* no-op */ }
                }
            }
        }
    }
}
