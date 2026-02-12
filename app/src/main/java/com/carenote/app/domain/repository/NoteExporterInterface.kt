package com.carenote.app.domain.repository

import android.net.Uri
import com.carenote.app.domain.model.Note

interface NoteCsvExporterInterface {
    suspend fun export(notes: List<Note>): Uri
}

interface NotePdfExporterInterface {
    suspend fun export(notes: List<Note>): Uri
}
