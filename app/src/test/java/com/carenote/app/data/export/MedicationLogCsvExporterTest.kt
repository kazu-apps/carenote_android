package com.carenote.app.data.export

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationLogCsvExporterTest {

    @Test
    fun `CSV file prefix is correctly configured`() {
        assertEquals("medication_logs_", AppConfig.Export.MEDICATION_LOG_CSV_FILE_PREFIX)
    }

    @Test
    fun `PDF file prefix is correctly configured`() {
        assertEquals("medication_logs_", AppConfig.Export.MEDICATION_LOG_PDF_FILE_PREFIX)
    }

    @Test
    fun `export CSV analytics event is correctly configured`() {
        assertEquals("medication_log_export_csv", AppConfig.Analytics.EVENT_MEDICATION_LOG_EXPORT_CSV)
    }

    @Test
    fun `export PDF analytics event is correctly configured`() {
        assertEquals("medication_log_export_pdf", AppConfig.Analytics.EVENT_MEDICATION_LOG_EXPORT_PDF)
    }

    @Test
    fun `cache dir name is configured`() {
        assertEquals("exports", AppConfig.Export.CACHE_DIR_NAME)
    }

    @Test
    fun `PDF page dimensions are valid`() {
        assertTrue(AppConfig.Export.PDF_PAGE_WIDTH > 0)
        assertTrue(AppConfig.Export.PDF_PAGE_HEIGHT > 0)
        assertTrue(AppConfig.Export.PDF_MARGIN > 0)
    }
}
