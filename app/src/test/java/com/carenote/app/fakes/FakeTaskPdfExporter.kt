package com.carenote.app.fakes

import android.net.Uri
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.repository.TaskPdfExporterInterface
import io.mockk.mockk

class FakeTaskPdfExporter : TaskPdfExporterInterface {
    var exportCallCount = 0
        private set
    var lastExportedItems: List<Task>? = null
        private set
    var shouldFail = false
    var fakeUri: Uri = mockk()

    override suspend fun export(tasks: List<Task>): Uri {
        if (shouldFail) throw RuntimeException("Fake Task PDF export error")
        exportCallCount++
        lastExportedItems = tasks
        return fakeUri
    }

    fun reset() {
        exportCallCount = 0
        lastExportedItems = null
        shouldFail = false
    }
}
