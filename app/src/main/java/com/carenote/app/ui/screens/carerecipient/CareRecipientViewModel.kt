package com.carenote.app.ui.screens.carerecipient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.domain.model.Gender
import com.carenote.app.domain.repository.CareRecipientRepository
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class CareRecipientUiState(
    val name: String = "",
    val birthDate: LocalDate? = null,
    val gender: Gender = Gender.UNSPECIFIED,
    val memo: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class CareRecipientViewModel @Inject constructor(
    private val repository: CareRecipientRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _uiState = MutableStateFlow(CareRecipientUiState())
    val uiState: StateFlow<CareRecipientUiState> = _uiState.asStateFlow()

    private var existingId: Long = 0
    private var existingCreatedAt: LocalDateTime = LocalDateTime.now()

    init {
        viewModelScope.launch {
            repository.getCareRecipient().collect { careRecipient ->
                if (careRecipient != null) {
                    existingId = careRecipient.id
                    existingCreatedAt = careRecipient.createdAt
                    _uiState.value = CareRecipientUiState(
                        name = careRecipient.name,
                        birthDate = careRecipient.birthDate,
                        gender = careRecipient.gender,
                        memo = careRecipient.memo,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateBirthDate(birthDate: LocalDate?) {
        _uiState.value = _uiState.value.copy(birthDate = birthDate)
    }

    fun updateGender(gender: Gender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateMemo(memo: String) {
        _uiState.value = _uiState.value.copy(memo = memo)
    }

    fun save() {
        val current = _uiState.value
        _uiState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val now = LocalDateTime.now()
            val careRecipient = CareRecipient(
                id = existingId,
                name = current.name.trim(),
                birthDate = current.birthDate,
                gender = current.gender,
                memo = current.memo.trim(),
                createdAt = if (existingId == 0L) now else existingCreatedAt,
                updatedAt = now
            )

            repository.saveCareRecipient(careRecipient)
                .onSuccess {
                    snackbarController.showMessage(R.string.care_recipient_save_success)
                }
                .onFailure {
                    snackbarController.showMessage(R.string.care_recipient_save_error)
                }

            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }
}
