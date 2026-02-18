package com.carenote.app.ui.screens.carerecipient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Gender
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.CareRecipientRepository
import com.carenote.app.domain.util.Clock
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.FormValidator.combineValidations
import com.carenote.app.ui.util.FormValidator.validateMaxLength
import com.carenote.app.ui.util.FormValidator.validateRequired
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class CareRecipientUiState(
    val name: String = "",
    val birthDate: LocalDate? = null,
    val gender: Gender = Gender.UNSPECIFIED,
    val nickname: String = "",
    val careLevel: String = "",
    val medicalHistory: String = "",
    val allergies: String = "",
    val memo: String = "",
    val nameError: UiText? = null,
    val nicknameError: UiText? = null,
    val careLevelError: UiText? = null,
    val medicalHistoryError: UiText? = null,
    val allergiesError: UiText? = null,
    val memoError: UiText? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class CareRecipientViewModel @Inject constructor(
    private val repository: CareRecipientRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess: SharedFlow<Unit> = _saveSuccess.asSharedFlow()

    private val _uiState = MutableStateFlow(CareRecipientUiState())
    val uiState: StateFlow<CareRecipientUiState> = _uiState.asStateFlow()

    private var existingId: Long = 0
    private var existingCreatedAt: LocalDateTime = clock.now()

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
                        nickname = careRecipient.nickname,
                        careLevel = careRecipient.careLevel,
                        medicalHistory = careRecipient.medicalHistory,
                        allergies = careRecipient.allergies,
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
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun updateBirthDate(birthDate: LocalDate?) {
        _uiState.value = _uiState.value.copy(birthDate = birthDate)
    }

    fun updateGender(gender: Gender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(nickname = nickname, nicknameError = null)
    }

    fun updateCareLevel(careLevel: String) {
        _uiState.value = _uiState.value.copy(careLevel = careLevel, careLevelError = null)
    }

    fun updateMedicalHistory(medicalHistory: String) {
        _uiState.value = _uiState.value.copy(medicalHistory = medicalHistory, medicalHistoryError = null)
    }

    fun updateAllergies(allergies: String) {
        _uiState.value = _uiState.value.copy(allergies = allergies, allergiesError = null)
    }

    fun updateMemo(memo: String) {
        _uiState.value = _uiState.value.copy(memo = memo, memoError = null)
    }

    fun save() {
        val current = _uiState.value

        val nameError = combineValidations(
            validateRequired(current.name, R.string.care_recipient_name_required),
            validateMaxLength(current.name, AppConfig.CareRecipient.NAME_MAX_LENGTH)
        )
        val nicknameError = validateMaxLength(current.nickname, AppConfig.CareRecipient.NICKNAME_MAX_LENGTH)
        val careLevelError = validateMaxLength(current.careLevel, AppConfig.CareRecipient.CARE_LEVEL_MAX_LENGTH)
        val medicalHistoryError = validateMaxLength(current.medicalHistory, AppConfig.CareRecipient.MEDICAL_HISTORY_MAX_LENGTH)
        val allergiesError = validateMaxLength(current.allergies, AppConfig.CareRecipient.ALLERGIES_MAX_LENGTH)
        val memoError = validateMaxLength(current.memo, AppConfig.CareRecipient.MEMO_MAX_LENGTH)

        if (nameError != null || nicknameError != null || careLevelError != null ||
            medicalHistoryError != null || allergiesError != null || memoError != null
        ) {
            _uiState.value = current.copy(
                nameError = nameError,
                nicknameError = nicknameError,
                careLevelError = careLevelError,
                medicalHistoryError = medicalHistoryError,
                allergiesError = allergiesError,
                memoError = memoError
            )
            return
        }

        _uiState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val now = clock.now()
            val careRecipient = CareRecipient(
                id = existingId,
                name = current.name.trim(),
                birthDate = current.birthDate,
                gender = current.gender,
                nickname = current.nickname.trim(),
                careLevel = current.careLevel.trim(),
                medicalHistory = current.medicalHistory.trim(),
                allergies = current.allergies.trim(),
                memo = current.memo.trim(),
                createdAt = if (existingId == 0L) now else existingCreatedAt,
                updatedAt = now
            )

            repository.saveCareRecipient(careRecipient)
                .onSuccess {
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_CARE_RECIPIENT_SAVED)
                    snackbarController.showMessage(R.string.care_recipient_save_success)
                    _saveSuccess.emit(Unit)
                }
                .onFailure {
                    snackbarController.showMessage(R.string.care_recipient_save_error)
                }

            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }
}
