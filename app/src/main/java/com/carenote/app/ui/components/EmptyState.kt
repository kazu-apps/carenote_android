package com.carenote.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.theme.ButtonShape

/**
 * データが空の場合の表示コンポーネント
 *
 * @param icon 表示アイコン
 * @param message 表示メッセージ
 * @param actionLabel アクションボタンのラベル（null の場合ボタン非表示）
 * @param onAction アクションボタン押下時のコールバック
 * @param modifier Modifier
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    message: String = stringResource(R.string.ui_empty_default),
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppConfig.UI.ICON_SIZE_XLARGE_DP.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(AppConfig.UI.CONTENT_SPACING_DP.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(AppConfig.UI.CONTENT_SPACING_DP.dp))
            Button(
                onClick = onAction,
                shape = ButtonShape
            ) {
                Text(text = actionLabel)
            }
        }
    }
}
