package com.carenote.app.ui.screens.healthrecords

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.screens.healthrecords.components.SelectionFormSection
import com.carenote.app.ui.screens.healthrecords.components.VitalSignsFormSection
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.util.SnackbarEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHealthRecordScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditHealthRecordViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { saved ->
            if (saved) {
                onNavigateBack()
            }
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

    val title = if (formState.isEditMode) {
        stringResource(R.string.health_records_edit)
    } else {
        stringResource(R.string.health_records_add)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AddEditHealthRecordTopBar(title = title, onNavigateBack = onNavigateBack) }
    ) { innerPadding ->
        AddEditHealthRecordContent(
            formState = formState,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditHealthRecordTopBar(
    title: String,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_close)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun AddEditHealthRecordContent(
    formState: AddEditHealthRecordFormState,
    viewModel: AddEditHealthRecordViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (formState.generalError != null) {
            GeneralErrorBanner(error = formState.generalError!!)
        }

        VitalSignsFormSection(formState = formState, viewModel = viewModel)

        SelectionFormSection(formState = formState, viewModel = viewModel)

        ConditionNoteField(
            value = formState.conditionNote,
            onValueChange = viewModel::updateConditionNote
        )

        FormActionButtons(
            isSaving = formState.isSaving,
            onCancel = onNavigateBack,
            onSave = viewModel::saveRecord
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GeneralErrorBanner(error: UiText) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = error.asString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun ConditionNoteField(
    value: String,
    onValueChange: (String) -> Unit
) {
    CareNoteTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.health_records_condition),
        placeholder = stringResource(R.string.health_records_condition_placeholder),
        singleLine = false,
        maxLines = Int.MAX_VALUE,
        modifier = Modifier.height((AppConfig.Note.CONTENT_MIN_LINES * 28).dp)
    )
}

@Composable
private fun FormActionButtons(
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = ButtonShape
        ) {
            Text(text = stringResource(R.string.common_cancel))
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            shape = ButtonShape,
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(20.dp)
                        .width(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = stringResource(R.string.common_save))
            }
        }
    }
}
