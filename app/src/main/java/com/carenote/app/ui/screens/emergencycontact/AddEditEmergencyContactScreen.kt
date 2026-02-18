package com.carenote.app.ui.screens.emergencycontact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.RelationshipType
import com.carenote.app.ui.components.CareNoteAddEditScaffold
import com.carenote.app.ui.util.SnackbarEvent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditEmergencyContactScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditEmergencyContactViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId -> context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    val title = stringResource(
        if (formState.isEditMode) R.string.emergency_contact_edit
        else R.string.emergency_contact_add
    )

    CareNoteAddEditScaffold(
        title = title,
        onNavigateBack = onNavigateBack,
        isDirty = viewModel.isDirty,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        EmergencyContactFormContent(
            formState = formState,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmergencyContactFormContent(
    formState: EmergencyContactFormState,
    viewModel: AddEditEmergencyContactViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppConfig.UI.CONTENT_SPACING_DP.dp)
    ) {
        EmergencyContactFormFields(formState = formState, viewModel = viewModel)
        RelationshipSelector(
            selectedRelationship = formState.relationship,
            onRelationshipSelect = viewModel::updateRelationship
        )
        OutlinedTextField(
            value = formState.memo,
            onValueChange = viewModel::updateMemo,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.emergency_contact_memo_label)) },
            placeholder = { Text(stringResource(R.string.emergency_contact_memo_placeholder)) },
            isError = formState.memoError != null,
            supportingText = formState.memoError?.let { error ->
                { Text(error.asString()) }
            },
            minLines = 2,
            maxLines = 4
        )
        Button(
            onClick = viewModel::save,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !formState.isSaving
        ) {
            Text(stringResource(R.string.common_save))
        }
    }
}

@Composable
private fun EmergencyContactFormFields(
    formState: EmergencyContactFormState,
    viewModel: AddEditEmergencyContactViewModel
) {
    OutlinedTextField(
        value = formState.name,
        onValueChange = viewModel::updateName,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.emergency_contact_name_label)) },
        placeholder = { Text(stringResource(R.string.emergency_contact_name_placeholder)) },
        isError = formState.nameError != null,
        supportingText = formState.nameError?.let { error ->
            { Text(error.asString()) }
        },
        singleLine = true
    )
    OutlinedTextField(
        value = formState.phoneNumber,
        onValueChange = viewModel::updatePhoneNumber,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.emergency_contact_phone_label)) },
        placeholder = { Text(stringResource(R.string.emergency_contact_phone_placeholder)) },
        isError = formState.phoneNumberError != null,
        supportingText = formState.phoneNumberError?.let { error ->
            { Text(error.asString()) }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelationshipSelector(
    selectedRelationship: RelationshipType,
    onRelationshipSelect: (RelationshipType) -> Unit
) {
    Text(
        text = stringResource(R.string.emergency_contact_relationship_label),
        style = MaterialTheme.typography.titleSmall
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RelationshipType.entries.forEach { type ->
            FilterChip(
                selected = selectedRelationship == type,
                onClick = { onRelationshipSelect(type) },
                label = { Text(relationshipChipLabel(type)) }
            )
        }
    }
}

@Composable
private fun relationshipChipLabel(type: RelationshipType): String {
    return when (type) {
        RelationshipType.FAMILY -> stringResource(R.string.emergency_contact_rel_family)
        RelationshipType.FRIEND -> stringResource(R.string.emergency_contact_rel_friend)
        RelationshipType.DOCTOR -> stringResource(R.string.emergency_contact_rel_doctor)
        RelationshipType.HOSPITAL -> stringResource(R.string.emergency_contact_rel_hospital)
        RelationshipType.EMERGENCY -> stringResource(R.string.emergency_contact_rel_emergency)
        RelationshipType.OTHER -> stringResource(R.string.emergency_contact_rel_other)
    }
}
