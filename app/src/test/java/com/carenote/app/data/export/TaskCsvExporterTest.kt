package com.carenote.app.data.export

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskCsvExporterTest {
    @Test
    fun `CSV file prefix is correctly configured`() {
        assertEquals("tasks_", AppConfig.Export.TASK_CSV_FILE_PREFIX)
    }

    @Test
    fun `PDF file prefix is correctly configured`() {
        assertEquals("tasks_", AppConfig.Export.TASK_PDF_FILE_PREFIX)
    }

    @Test
    fun `export CSV analytics event is correctly configured`() {
        assertEquals("task_export_csv", AppConfig.Analytics.EVENT_TASK_EXPORT_CSV)
    }

    @Test
    fun `export PDF analytics event is correctly configured`() {
        assertEquals("task_export_pdf", AppConfig.Analytics.EVENT_TASK_EXPORT_PDF)
    }
}
