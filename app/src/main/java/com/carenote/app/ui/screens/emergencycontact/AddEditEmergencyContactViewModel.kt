package com.carenote.app.ui.screens.emergencycontact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.model.RelationshipType
import com.carenote.app.domain.repository.EmergencyContactRepository
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

data class EmergencyContactFormState(
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: RelationshipType = RelationshipType.FAMILY,
    val memo: String = "",
    val nameError: UiText? = null,
    val phoneNumberError: UiText? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditEmergencyContactViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EmergencyContactRepository
) : ViewModel() {

    private val contactId: Long? = savedStateHandle.get<Long>("contactId")

    private val _formState = MutableStateFlow(
        EmergencyContactFormState(isEditMode = contactId != null)
    )
    val formState: StateFlow<EmergencyContactFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private var originalContact: EmergencyContact? = null
    private var _initialFormState: EmergencyContactFormState? = null

    val isDirty: Boolean
        get() {
            val initial = _initialFormState ?: return false
            val current = _formState.value.copy(
                nameError = null,
                phoneNumberError = null,
                isSaving = false,
                isEditMode = false
            )
            val baseline = initial.copy(
                nameError = null,
                phoneNumberError = null,
                isSaving = false,
                isEditMode = false
            )
            return current != baseline
        }

    init {
        if (contactId != null) {
            loadContact(contactId)
        } else {
            _initialFormState = _formState.value
        }
    }

    private fun loadContact(id: Long) {
        viewModelScope.launch {
            val contact = repository.getContactById(id).firstOrNull()
            if (contact != null) {
                originalContact = contact
                _formState.value = _formState.value.copy(
                    name = contact.name,
                    phoneNumber = contact.phoneNumber,
                    relationship = contact.relationship,
                    memo = contact.memo
                )
                _initialFormState = _formState.value
            }
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(
            name = name,
            nameError = null
        )
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _formState.value = _formState.value.copy(
            phoneNumber = phoneNumber,
            phoneNumberError = null
        )
    }

    fun updateRelationship(relationship: RelationshipType) {
        _formState.value = _formState.value.copy(relationship = relationship)
    }

    fun updateMemo(memo: String) {
        _formState.value = _formState.value.copy(memo = memo)
    }

    fun save() {
        val current = _formState.value

        val nameError = when {
            current.name.isBlank() -> UiText.Resource(R.string.emergency_contact_name_required)
            current.name.length > AppConfig.EmergencyContact.NAME_MAX_LENGTH ->
                UiText.ResourceWithArgs(
                    R.string.ui_validation_too_long,
                    listOf(AppConfig.EmergencyContact.NAME_MAX_LENGTH)
                )
            else -> null
        }

        val phoneNumberError = when {
            current.phoneNumber.isBlank() ->
                UiText.Resource(R.string.emergency_contact_phone_required)
            current.phoneNumber.length > AppConfig.EmergencyContact.PHONE_MAX_LENGTH ->
                UiText.ResourceWithArgs(
                    R.string.ui_validation_too_long,
                    listOf(AppConfig.EmergencyContact.PHONE_MAX_LENGTH)
                )
            else -> null
        }

        if (nameError != null || phoneNumberError != null) {
            _formState.value = current.copy(
                nameError = nameError,
                phoneNumberError = phoneNumberError
            )
            return
        }

        _formState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val now = LocalDateTime.now()
            val original = originalContact
            if (contactId != null && original != null) {
                val updatedContact = original.copy(
                    name = current.name.trim(),
                    phoneNumber = current.phoneNumber.trim(),
                    relationship = current.relationship,
                    memo = current.memo.trim(),
                    updatedAt = now
                )
                repository.updateContact(updatedContact)
                    .onSuccess {
                        Timber.d("Emergency contact updated: id=$contactId")
                        _savedEvent.send(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to update emergency contact: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                        snackbarController.showMessage(R.string.emergency_contact_save_failed)
                    }
            } else {
                val newContact = EmergencyContact(
                    name = current.name.trim(),
                    phoneNumber = current.phoneNumber.trim(),
                    relationship = current.relationship,
                    memo = current.memo.trim(),
                    createdAt = now,
                    updatedAt = now
                )
                repository.insertContact(newContact)
                    .onSuccess { id ->
                        Timber.d("Emergency contact saved: id=$id")
                        _savedEvent.send(true)
                    }
                    .onFailure { error ->
                        Timber.w("Failed to save emergency contact: $error")
                        _formState.value = _formState.value.copy(isSaving = false)
                        snackbarController.showMessage(R.string.emergency_contact_save_failed)
                    }
            }
        }
    }
}
