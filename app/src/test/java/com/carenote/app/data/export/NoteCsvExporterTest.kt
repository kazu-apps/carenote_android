package com.carenote.app.data.export

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteCsvExporterTest {
    @Test
    fun `CSV file prefix is correctly configured`() {
        assertEquals("notes_", AppConfig.Export.NOTE_CSV_FILE_PREFIX)
    }

    @Test
    fun `PDF file prefix is correctly configured`() {
        assertEquals("notes_", AppConfig.Export.NOTE_PDF_FILE_PREFIX)
    }

    @Test
    fun `export CSV analytics event is correctly configured`() {
        assertEquals("note_export_csv", AppConfig.Analytics.EVENT_NOTE_EXPORT_CSV)
    }

    @Test
    fun `export PDF analytics event is correctly configured`() {
        assertEquals("note_export_pdf", AppConfig.Analytics.EVENT_NOTE_EXPORT_PDF)
    }
}
