package com.carenote.app.ui.screens.settings.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig

@Composable
fun ChangePasswordDialog(
    onConfirm: (currentPassword: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordMismatchText = stringResource(
        R.string.settings_password_mismatch
    )
    val passwordTooShortText = stringResource(
        R.string.auth_password_too_short,
        AppConfig.Auth.PASSWORD_MIN_LENGTH
    )

    ChangePasswordAlertDialog(
        currentPassword = currentPassword,
        newPassword = newPassword,
        confirmPassword = confirmPassword,
        errorMessage = errorMessage,
        passwordTooShortText = passwordTooShortText,
        passwordMismatchText = passwordMismatchText,
        onCurrentPasswordChange = {
            currentPassword = it
            errorMessage = null
        },
        onNewPasswordChange = {
            newPassword = it
            errorMessage = null
        },
        onConfirmPasswordChange = {
            confirmPassword = it
            errorMessage = null
        },
        onErrorMessage = { errorMessage = it },
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Suppress("LongParameterList")
@Composable
private fun ChangePasswordAlertDialog(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    errorMessage: String?,
    passwordTooShortText: String,
    passwordMismatchText: String,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onErrorMessage: (String?) -> Unit,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { ChangePasswordTitle() },
        text = {
            ChangePasswordFields(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword,
                errorMessage = errorMessage,
                onCurrentPasswordChange = onCurrentPasswordChange,
                onNewPasswordChange = onNewPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onErrorMessage(
                        validateAndConfirm(
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            confirmPassword = confirmPassword,
                            passwordTooShortText = passwordTooShortText,
                            passwordMismatchText = passwordMismatchText,
                            onConfirm = onConfirm
                        )
                    )
                },
                enabled = currentPassword.isNotBlank() &&
                    newPassword.isNotBlank() &&
                    confirmPassword.isNotBlank()
            ) {
                Text(text = stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun ChangePasswordTitle() {
    Text(
        text = stringResource(R.string.settings_change_password_title),
        style = MaterialTheme.typography.titleLarge
    )
}

@Suppress("LongParameterList")
@Composable
private fun ChangePasswordFields(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    errorMessage: String?,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit
) {
    Column {
        PasswordField(
            value = currentPassword,
            onValueChange = onCurrentPasswordChange,
            labelResId = R.string.settings_current_password
        )
        Spacer(modifier = Modifier.height(8.dp))
        PasswordField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            labelResId = R.string.settings_new_password
        )
        Spacer(modifier = Modifier.height(8.dp))
        PasswordField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            labelResId = R.string.settings_confirm_password
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    labelResId: Int
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelResId)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Suppress("LongParameterList")
private fun validateAndConfirm(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    passwordTooShortText: String,
    passwordMismatchText: String,
    onConfirm: (String, String) -> Unit
): String? {
    return when {
        currentPassword.isBlank() ||
            newPassword.isBlank() ||
            confirmPassword.isBlank() -> null
        newPassword.length < AppConfig.Auth.PASSWORD_MIN_LENGTH ->
            passwordTooShortText
        newPassword != confirmPassword ->
            passwordMismatchText
        else -> {
            onConfirm(currentPassword, newPassword)
            null
        }
    }
}
