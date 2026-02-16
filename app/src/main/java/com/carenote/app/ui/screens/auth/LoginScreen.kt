package com.carenote.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.SnackbarEvent

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val formState by viewModel.loginFormState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.authSuccessEvent.collect { success ->
            if (success) {
                onLoginSuccess()
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LoginContent(
            formState = formState,
            passwordVisible = passwordVisible,
            onEmailChange = viewModel::updateLoginEmail,
            onPasswordChange = viewModel::updateLoginPassword,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            onForgotPassword = onNavigateToForgotPassword,
            onSignIn = viewModel::signIn,
            onNavigateToRegister = onNavigateToRegister,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun LoginContent(
    formState: LoginFormState,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginHeader()
        LoginFormFields(
            formState = formState,
            passwordVisible = passwordVisible,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            onForgotPassword = onForgotPassword,
            onSignIn = onSignIn
        )
        LoginActions(
            isLoading = formState.isLoading,
            onSignIn = onSignIn,
            onNavigateToRegister = onNavigateToRegister
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun LoginHeader() {
    Spacer(modifier = Modifier.height(48.dp))
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.auth_login_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(48.dp))
}

@Suppress("LongParameterList")
@Composable
private fun LoginFormFields(
    formState: LoginFormState,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignIn: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    CareNoteTextField(
        value = formState.email,
        onValueChange = onEmailChange,
        label = stringResource(R.string.auth_email),
        placeholder = stringResource(R.string.auth_email_placeholder),
        errorMessage = formState.emailError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
    LoginPasswordField(
        formState = formState,
        passwordVisible = passwordVisible,
        onPasswordChange = onPasswordChange,
        onTogglePasswordVisibility = onTogglePasswordVisibility,
        onSignIn = onSignIn
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onForgotPassword) {
        Text(
            text = stringResource(R.string.auth_forgot_password),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun LoginPasswordField(
    formState: LoginFormState,
    passwordVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignIn: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    CareNoteTextField(
        value = formState.password,
        onValueChange = onPasswordChange,
        label = stringResource(R.string.auth_password),
        placeholder = stringResource(R.string.auth_password_placeholder),
        errorMessage = formState.passwordError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onSignIn()
            }
        ),
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            PasswordVisibilityIcon(
                passwordVisible = passwordVisible,
                onClick = onTogglePasswordVisibility
            )
        }
    )
}

@Composable
private fun PasswordVisibilityIcon(
    passwordVisible: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (passwordVisible) {
                Icons.Filled.VisibilityOff
            } else {
                Icons.Filled.Visibility
            },
            contentDescription = if (passwordVisible) {
                stringResource(R.string.auth_hide_password)
            } else {
                stringResource(R.string.auth_show_password)
            }
        )
    }
}

@Composable
private fun LoginActions(
    isLoading: Boolean,
    onSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Button(
        onClick = onSignIn,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = ButtonShape,
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = stringResource(R.string.auth_sign_in),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.auth_no_account),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onNavigateToRegister) {
            Text(
                text = stringResource(R.string.auth_sign_up_link),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun LoginContentPreview() {
    CareNoteTheme {
        LoginContent(
            formState = PreviewData.loginFormState,
            passwordVisible = false,
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onForgotPassword = {},
            onSignIn = {},
            onNavigateToRegister = {}
        )
    }
}
