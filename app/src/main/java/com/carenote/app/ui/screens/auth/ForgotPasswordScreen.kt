package com.carenote.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.SnackbarEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val formState by viewModel.forgotPasswordFormState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId -> context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.auth_forgot_password_title),
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (formState.emailSent) {
                EmailSentContent(onNavigateBack = onNavigateBack)
            } else {
                ResetFormContent(
                    formState = formState,
                    onEmailChange = viewModel::updateForgotPasswordEmail,
                    onSendResetEmail = {
                        focusManager.clearFocus()
                        viewModel.sendPasswordResetEmail()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResetFormContent(
    formState: ForgotPasswordFormState,
    onEmailChange: (String) -> Unit,
    onSendResetEmail: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Text(
        text = stringResource(R.string.auth_password_reset_description),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    CareNoteTextField(
        value = formState.email,
        onValueChange = onEmailChange,
        label = stringResource(R.string.auth_email),
        placeholder = stringResource(R.string.auth_email_placeholder),
        errorMessage = formState.emailError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onSendResetEmail()
            }
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onSendResetEmail,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = ButtonShape,
        enabled = !formState.isLoading
    ) {
        if (formState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = stringResource(R.string.auth_send_reset_email),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun EmailSentContent(
    onNavigateBack: () -> Unit
) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.auth_password_reset_sent),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedButton(
        onClick = onNavigateBack,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = ButtonShape
    ) {
        Text(
            text = stringResource(R.string.auth_back_to_login),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@LightDarkPreview
@Composable
private fun ResetFormContentPreview() {
    CareNoteTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResetFormContent(
                formState = PreviewData.forgotPasswordFormState,
                onEmailChange = {},
                onSendResetEmail = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun EmailSentContentPreview() {
    CareNoteTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmailSentContent(onNavigateBack = {})
        }
    }
}
