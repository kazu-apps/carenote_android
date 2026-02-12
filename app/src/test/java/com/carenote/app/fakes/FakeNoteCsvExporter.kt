package com.carenote.app.fakes

import android.net.Uri
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.repository.NoteCsvExporterInterface
import io.mockk.mockk

class FakeNoteCsvExporter : NoteCsvExporterInterface {
    var exportCallCount = 0
        private set
    var lastExportedItems: List<Note>? = null
        private set
    var shouldFail = false
    var fakeUri: Uri = mockk()

    override suspend fun export(notes: List<Note>): Uri {
        if (shouldFail) throw RuntimeException("Fake Note CSV export error")
        exportCallCount++
        lastExportedItems = notes
        return fakeUri
    }

    fun reset() {
        exportCallCount = 0
        lastExportedItems = null
        shouldFail = false
    }
}
