package com.carenote.app.ui.screens.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.screens.notes.components.NoteTagChip
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.util.SnackbarEvent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditNoteScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (viewModel.isDirty) showDiscardDialog = true else onNavigateBack()
    }

    BackHandler(enabled = viewModel.isDirty) {
        showDiscardDialog = true
    }

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
        stringResource(R.string.notes_edit)
    } else {
        stringResource(R.string.notes_add)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            CareNoteTextField(
                value = formState.title,
                onValueChange = viewModel::updateTitle,
                label = stringResource(R.string.notes_title_label),
                placeholder = stringResource(R.string.notes_title_placeholder),
                errorMessage = formState.titleError,
                singleLine = true
            )

            CareNoteTextField(
                value = formState.content,
                onValueChange = viewModel::updateContent,
                label = stringResource(R.string.notes_content_label),
                placeholder = stringResource(R.string.notes_content_placeholder),
                errorMessage = formState.contentError,
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier.height(
                    (AppConfig.Note.CONTENT_MIN_LINES * 28).dp
                )
            )

            Text(
                text = stringResource(R.string.notes_tag_label),
                style = MaterialTheme.typography.titleMedium
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NoteTag.entries.forEach { tag ->
                    NoteTagChip(
                        tag = tag,
                        selected = formState.tag == tag,
                        onClick = { viewModel.updateTag(tag) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = handleBack,
                    modifier = Modifier.weight(1f),
                    shape = ButtonShape
                ) {
                    Text(text = stringResource(R.string.common_cancel))
                }
                Button(
                    onClick = viewModel::saveNote,
                    modifier = Modifier.weight(1f),
                    shape = ButtonShape,
                    enabled = !formState.isSaving
                ) {
                    if (formState.isSaving) {
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDiscardDialog) {
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_discard_title),
            message = stringResource(R.string.ui_confirm_discard_message),
            confirmLabel = stringResource(R.string.ui_confirm_discard_yes),
            dismissLabel = stringResource(R.string.ui_confirm_discard_no),
            onConfirm = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false },
            isDestructive = true
        )
    }
}
