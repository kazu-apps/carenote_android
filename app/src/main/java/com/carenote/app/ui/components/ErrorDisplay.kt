package com.carenote.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.CardShape

/**
 * エラー表示コンポーネント
 *
 * DomainError の種類に応じたメッセージを表示し、リトライボタンを提供する。
 *
 * @param error 表示する DomainError
 * @param onRetry リトライボタン押下時のコールバック（null の場合ボタン非表示）
 * @param modifier Modifier
 */
@Composable
fun ErrorDisplay(
    error: DomainError,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage(error),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        shape = ButtonShape
                    ) {
                        Text(text = stringResource(R.string.ui_retry))
                    }
                }
            }
        }
    }
}

/**
 * DomainError の種類に応じたユーザー向けメッセージを返す
 */
@Composable
private fun errorMessage(error: DomainError): String = when (error) {
    is DomainError.DatabaseError -> stringResource(R.string.ui_error_database)
    is DomainError.NotFoundError -> stringResource(R.string.ui_error_not_found)
    is DomainError.ValidationError -> stringResource(R.string.ui_error_validation)
    is DomainError.NetworkError -> stringResource(R.string.ui_error_network)
    is DomainError.UnauthorizedError -> stringResource(R.string.ui_error_unauthorized)
    is DomainError.UnknownError -> stringResource(R.string.ui_error_unknown)
}
