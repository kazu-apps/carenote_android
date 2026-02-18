package com.carenote.app.ui.screens.carerecipient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Gender
import com.carenote.app.ui.components.CareNoteDatePickerDialog
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.SnackbarEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CareRecipientScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: (() -> Unit)? = null,
    showBackButton: Boolean = true,
    viewModel: CareRecipientViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    CareRecipientEffects(viewModel, snackbarHostState, context, onSaveSuccess)

    Scaffold(
        topBar = {
            CareRecipientTopBar(
                showBackButton = showBackButton,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        CareRecipientContent(
            uiState = uiState,
            onNameChange = { viewModel.updateName(it) },
            onNicknameChange = { viewModel.updateNickname(it) },
            onBirthDateClick = { showDatePicker = true },
            onGenderChange = { viewModel.updateGender(it) },
            onCareLevelChange = { viewModel.updateCareLevel(it) },
            onMedicalHistoryChange = { viewModel.updateMedicalHistory(it) },
            onAllergiesChange = { viewModel.updateAllergies(it) },
            onMemoChange = { viewModel.updateMemo(it) },
            onSave = { viewModel.save() },
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showDatePicker) {
        CareNoteDatePickerDialog(
            initialDate = uiState.birthDate ?: LocalDate.now(),
            onDateSelected = { date ->
                viewModel.updateBirthDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun CareRecipientEffects(
    viewModel: CareRecipientViewModel,
    snackbarHostState: SnackbarHostState,
    context: android.content.Context,
    onSaveSuccess: (() -> Unit)?
) {
    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId -> context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            onSaveSuccess?.invoke()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CareRecipientTopBar(
    showBackButton: Boolean,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.care_recipient_profile),
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CareRecipientContent(
    uiState: CareRecipientUiState,
    onNameChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onBirthDateClick: () -> Unit,
    onGenderChange: (Gender) -> Unit,
    onCareLevelChange: (String) -> Unit,
    onMedicalHistoryChange: (String) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(AppConfig.UI.ITEM_SPACING_DP.dp)
    ) {
        BasicInfoFields(
            uiState = uiState,
            onNameChange = onNameChange,
            onNicknameChange = onNicknameChange,
            onBirthDateClick = onBirthDateClick,
            onGenderChange = onGenderChange
        )

        CareLevelAndMedicalFields(
            uiState = uiState,
            onCareLevelChange = onCareLevelChange,
            onMedicalHistoryChange = onMedicalHistoryChange,
            onAllergiesChange = onAllergiesChange,
            onMemoChange = onMemoChange
        )

        Button(
            onClick = onSave,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppConfig.UI.ITEM_SPACING_DP.dp)
        ) {
            Text(text = stringResource(R.string.common_save))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicInfoFields(
    uiState: CareRecipientUiState,
    onNameChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onBirthDateClick: () -> Unit,
    onGenderChange: (Gender) -> Unit
) {
    OutlinedTextField(
        value = uiState.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.care_recipient_name)) },
        placeholder = { Text(stringResource(R.string.care_recipient_name_hint)) },
        isError = uiState.nameError != null,
        supportingText = uiState.nameError?.let { error ->
            { Text(error.asString()) }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.nickname,
        onValueChange = onNicknameChange,
        label = { Text(stringResource(R.string.care_recipient_nickname)) },
        placeholder = { Text(stringResource(R.string.care_recipient_nickname_hint)) },
        isError = uiState.nicknameError != null,
        supportingText = uiState.nicknameError?.let { error ->
            { Text(error.asString()) }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    BirthDateField(
        birthDate = uiState.birthDate,
        onBirthDateClick = onBirthDateClick
    )

    Text(
        text = stringResource(R.string.care_recipient_gender),
        style = MaterialTheme.typography.bodyLarge
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Gender.entries.forEach { gender ->
            FilterChip(
                selected = uiState.gender == gender,
                onClick = { onGenderChange(gender) },
                label = { Text(genderLabel(gender)) }
            )
        }
    }
}

@Composable
private fun BirthDateField(
    birthDate: LocalDate?,
    onBirthDateClick: () -> Unit
) {
    val birthDateText = birthDate?.let {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(it)
    } ?: stringResource(R.string.care_recipient_birth_date_not_set)

    OutlinedTextField(
        value = birthDateText,
        onValueChange = {},
        label = { Text(stringResource(R.string.care_recipient_birth_date)) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        enabled = true,
        interactionSource = remember {
            androidx.compose.foundation.interaction.MutableInteractionSource()
        }.also { interactionSource ->
            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect {
                    if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                        onBirthDateClick()
                    }
                }
            }
        }
    )
}

@Composable
private fun CareLevelAndMedicalFields(
    uiState: CareRecipientUiState,
    onCareLevelChange: (String) -> Unit,
    onMedicalHistoryChange: (String) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onMemoChange: (String) -> Unit
) {
    OutlinedTextField(
        value = uiState.careLevel,
        onValueChange = onCareLevelChange,
        label = { Text(stringResource(R.string.care_recipient_care_level)) },
        placeholder = { Text(stringResource(R.string.care_recipient_care_level_hint)) },
        isError = uiState.careLevelError != null,
        supportingText = uiState.careLevelError?.let { error ->
            { Text(error.asString()) }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.medicalHistory,
        onValueChange = onMedicalHistoryChange,
        label = { Text(stringResource(R.string.care_recipient_medical_history)) },
        placeholder = { Text(stringResource(R.string.care_recipient_medical_history_hint)) },
        isError = uiState.medicalHistoryError != null,
        supportingText = uiState.medicalHistoryError?.let { error ->
            { Text(error.asString()) }
        },
        minLines = 3,
        maxLines = 6,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.allergies,
        onValueChange = onAllergiesChange,
        label = { Text(stringResource(R.string.care_recipient_allergies)) },
        placeholder = { Text(stringResource(R.string.care_recipient_allergies_hint)) },
        isError = uiState.allergiesError != null,
        supportingText = uiState.allergiesError?.let { error ->
            { Text(error.asString()) }
        },
        minLines = 3,
        maxLines = 6,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.memo,
        onValueChange = onMemoChange,
        label = { Text(stringResource(R.string.care_recipient_memo)) },
        placeholder = { Text(stringResource(R.string.care_recipient_memo_hint)) },
        isError = uiState.memoError != null,
        supportingText = uiState.memoError?.let { error ->
            { Text(error.asString()) }
        },
        minLines = 3,
        maxLines = 6,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun genderLabel(gender: Gender): String {
    return when (gender) {
        Gender.MALE -> stringResource(R.string.care_recipient_gender_male)
        Gender.FEMALE -> stringResource(R.string.care_recipient_gender_female)
        Gender.OTHER -> stringResource(R.string.care_recipient_gender_other)
        Gender.UNSPECIFIED -> stringResource(R.string.care_recipient_gender_unspecified)
    }
}

@LightDarkPreview
@Composable
private fun CareRecipientContentPreview() {
    CareNoteTheme {
        CareRecipientContent(
            uiState = PreviewData.careRecipientUiState,
            onNameChange = {},
            onNicknameChange = {},
            onBirthDateClick = {},
            onGenderChange = {},
            onCareLevelChange = {},
            onMedicalHistoryChange = {},
            onAllergiesChange = {},
            onMemoChange = {},
            onSave = {}
        )
    }
}
