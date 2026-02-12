package com.carenote.app.fakes

import android.net.Uri
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.repository.MedicationLogPdfExporterInterface
import io.mockk.mockk

class FakeMedicationLogPdfExporter : MedicationLogPdfExporterInterface {

    var exportCallCount = 0
        private set
    var lastExportedLogs: List<MedicationLog>? = null
        private set
    var lastMedicationNames: Map<Long, String>? = null
        private set
    var shouldFail = false
    var fakeUri: Uri = mockk()

    override suspend fun export(
        logs: List<MedicationLog>,
        medicationNames: Map<Long, String>
    ): Uri {
        if (shouldFail) throw RuntimeException("Fake PDF export error")
        exportCallCount++
        lastExportedLogs = logs
        lastMedicationNames = medicationNames
        return fakeUri
    }

    fun reset() {
        exportCallCount = 0
        lastExportedLogs = null
        lastMedicationNames = null
        shouldFail = false
    }
}
