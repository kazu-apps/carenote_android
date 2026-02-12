package com.carenote.app.domain.repository

import android.net.Uri
import com.carenote.app.domain.model.MedicationLog

interface MedicationLogCsvExporterInterface {
    suspend fun export(logs: List<MedicationLog>, medicationNames: Map<Long, String>): Uri
}

interface MedicationLogPdfExporterInterface {
    suspend fun export(logs: List<MedicationLog>, medicationNames: Map<Long, String>): Uri
}
