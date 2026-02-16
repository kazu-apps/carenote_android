package com.carenote.app.ui.screens.settings.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.theme.CareNoteTheme

enum class ExportFormat { CSV, PDF }

@Composable
fun DataExportDialog(
    entityLabel: String,
    onExport: (format: ExportFormat, periodDays: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.CSV) }
    var selectedPeriodDays by remember {
        mutableStateOf<Long?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.data_export_dialog_title))
        },
        text = {
            ExportDialogContent(
                selectedFormat = selectedFormat,
                onFormatSelected = { selectedFormat = it },
                selectedPeriodDays = selectedPeriodDays,
                onPeriodSelected = { selectedPeriodDays = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onExport(selectedFormat, selectedPeriodDays)
                }
            ) {
                Text(stringResource(R.string.data_export_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun ExportDialogContent(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit,
    selectedPeriodDays: Long?,
    onPeriodSelected: (Long?) -> Unit
) {
    Column {
        ExportFormatSelector(
            selectedFormat = selectedFormat,
            onFormatSelected = onFormatSelected
        )
        Spacer(modifier = Modifier.height(16.dp))
        ExportPeriodSelector(
            selectedPeriodDays = selectedPeriodDays,
            onPeriodSelected = onPeriodSelected
        )
    }
}

@Composable
private fun ExportFormatSelector(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit
) {
    Text(
        text = stringResource(R.string.data_export_format_label),
        style = MaterialTheme.typography.labelLarge
    )
    Column(modifier = Modifier.selectableGroup()) {
        FormatOption(
            text = stringResource(R.string.data_export_format_csv),
            selected = selectedFormat == ExportFormat.CSV,
            onClick = { onFormatSelected(ExportFormat.CSV) }
        )
        FormatOption(
            text = stringResource(R.string.data_export_format_pdf),
            selected = selectedFormat == ExportFormat.PDF,
            onClick = { onFormatSelected(ExportFormat.PDF) }
        )
    }
}

@Composable
private fun ExportPeriodSelector(
    selectedPeriodDays: Long?,
    onPeriodSelected: (Long?) -> Unit
) {
    Text(
        text = stringResource(R.string.data_export_period_label),
        style = MaterialTheme.typography.labelLarge
    )
    Column(modifier = Modifier.selectableGroup()) {
        PeriodOption(
            text = stringResource(R.string.data_export_period_all),
            selected = selectedPeriodDays == null,
            onClick = { onPeriodSelected(null) }
        )
        PeriodOption(
            text = stringResource(R.string.data_export_period_30_days),
            selected = selectedPeriodDays == 30L,
            onClick = { onPeriodSelected(30L) }
        )
        PeriodOption(
            text = stringResource(R.string.data_export_period_90_days),
            selected = selectedPeriodDays == 90L,
            onClick = { onPeriodSelected(90L) }
        )
        PeriodOption(
            text = stringResource(R.string.data_export_period_1_year),
            selected = selectedPeriodDays == 365L,
            onClick = { onPeriodSelected(365L) }
        )
    }
}

@Composable
private fun FormatOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun PeriodOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@LightDarkPreview
@Composable
private fun DataExportDialogPreview() {
    CareNoteTheme {
        DataExportDialog(
            entityLabel = "タスク",
            onExport = { _, _ -> },
            onDismiss = {}
        )
    }
}
