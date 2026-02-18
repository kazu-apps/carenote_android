package com.carenote.app.domain.repository

import android.net.Uri
import com.carenote.app.domain.model.CalendarEvent

interface TaskCsvExporterInterface {
    suspend fun export(events: List<CalendarEvent>): Uri
}

interface TaskPdfExporterInterface {
    suspend fun export(events: List<CalendarEvent>): Uri
}
