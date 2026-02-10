package com.carenote.app.ui.screens.healthrecords

import android.net.Uri

sealed class ExportState {
    data object Idle : ExportState()
    data object Exporting : ExportState()
    data class Success(val uri: Uri, val mimeType: String) : ExportState()
    data class Error(val message: String) : ExportState()
}
