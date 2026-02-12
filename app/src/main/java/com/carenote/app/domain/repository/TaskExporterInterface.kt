package com.carenote.app.domain.repository

import android.net.Uri
import com.carenote.app.domain.model.Task

interface TaskCsvExporterInterface {
    suspend fun export(tasks: List<Task>): Uri
}

interface TaskPdfExporterInterface {
    suspend fun export(tasks: List<Task>): Uri
}
