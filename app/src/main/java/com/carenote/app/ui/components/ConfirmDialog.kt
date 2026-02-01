package com.carenote.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R

/**
 * 確認ダイアログコンポーネント
 *
 * 削除操作等の確認に使用する。
 *
 * @param title ダイアログタイトル
 * @param message ダイアログメッセージ
 * @param confirmLabel 確認ボタンラベル
 * @param dismissLabel キャンセルボタンラベル
 * @param onConfirm 確認時のコールバック
 * @param onDismiss キャンセル時のコールバック
 * @param isDestructive 破壊的操作の場合 true（ボタンが赤くなる）
 * @param modifier Modifier
 */
@Composable
fun ConfirmDialog(
    title: String = stringResource(R.string.ui_confirm_title),
    message: String,
    confirmLabel: String = stringResource(R.string.ui_confirm_yes),
    dismissLabel: String = stringResource(R.string.ui_confirm_no),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissLabel)
            }
        }
    )
}
