package com.carenote.app.fakes

import android.net.Uri
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.repository.TaskCsvExporterInterface
import io.mockk.mockk

class FakeTaskCsvExporter : TaskCsvExporterInterface {
    var exportCallCount = 0
        private set
    var lastExportedItems: List<CalendarEvent>? = null
        private set
    var shouldFail = false
    var fakeUri: Uri = mockk()

    override suspend fun export(events: List<CalendarEvent>): Uri {
        if (shouldFail) throw RuntimeException("Fake Task CSV export error")
        exportCallCount++
        lastExportedItems = events
        return fakeUri
    }

    fun reset() {
        exportCallCount = 0
        lastExportedItems = null
        shouldFail = false
    }
}
