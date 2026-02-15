package com.carenote.app.ui.screens.member

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import com.carenote.app.ui.components.CareNoteAddEditScaffold
import com.carenote.app.ui.util.SnackbarEvent

@Composable
fun SendInvitationScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SendInvitationViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { result ->
            val shareText = context.getString(R.string.send_invitation_share_text, result.inviteLink)
            val shareTitle = context.getString(R.string.send_invitation_share_title)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(shareIntent, shareTitle))
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

    CareNoteAddEditScaffold(
        title = stringResource(R.string.send_invitation_title),
        onNavigateBack = onNavigateBack,
        isDirty = viewModel.isDirty,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppConfig.UI.CONTENT_SPACING_DP.dp)
        ) {
            OutlinedTextField(
                value = formState.email,
                onValueChange = viewModel::updateEmail,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.send_invitation_email_label)) },
                placeholder = { Text(stringResource(R.string.send_invitation_email_placeholder)) },
                isError = formState.emailError != null,
                supportingText = formState.emailError?.let { error ->
                    { Text(error.asString()) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = formState.message,
                onValueChange = viewModel::updateMessage,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.send_invitation_message_label)) },
                placeholder = { Text(stringResource(R.string.send_invitation_message_placeholder)) },
                minLines = 3,
                maxLines = 5
            )

            Button(
                onClick = viewModel::send,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppConfig.UI.ITEM_SPACING_DP.dp),
                enabled = !formState.isSending
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppConfig.UI.ITEM_SPACING_DP.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null
                    )
                    Text(stringResource(R.string.send_invitation_send))
                }
            }
        }
    }
}
