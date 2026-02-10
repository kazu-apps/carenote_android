package com.carenote.app.ui.screens.emergencycontact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.repository.EmergencyContactRepository
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EmergencyContactListViewModel @Inject constructor(
    private val repository: EmergencyContactRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    val contacts: StateFlow<List<EmergencyContact>> = repository.getAllContacts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            repository.deleteContact(id)
                .onSuccess {
                    Timber.d("Emergency contact deleted: id=$id")
                    snackbarController.showMessage(R.string.emergency_contact_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete emergency contact: $error")
                    snackbarController.showMessage(R.string.emergency_contact_delete_failed)
                }
        }
    }
}
