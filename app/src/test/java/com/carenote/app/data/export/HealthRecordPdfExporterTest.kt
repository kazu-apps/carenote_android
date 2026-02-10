package com.carenote.app.data.export

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * PdfDocument API requires real Android graphics subsystem and does not work
 * under Robolectric. Full PDF export tests belong in androidTest.
 *
 * This file contains unit tests for the config values used by the exporter.
 */
class HealthRecordPdfExporterTest {

    @Test
    fun `PDF page dimensions are A4`() {
        assertEquals(595, AppConfig.Export.PDF_PAGE_WIDTH)
        assertEquals(842, AppConfig.Export.PDF_PAGE_HEIGHT)
    }

    @Test
    fun `PDF margin is 40`() {
        assertEquals(40, AppConfig.Export.PDF_MARGIN)
    }

    @Test
    fun `PDF font sizes are configured`() {
        assertEquals(18f, AppConfig.Export.PDF_FONT_SIZE_TITLE)
        assertEquals(10f, AppConfig.Export.PDF_FONT_SIZE_HEADER)
        assertEquals(9f, AppConfig.Export.PDF_FONT_SIZE_BODY)
    }

    @Test
    fun `PDF line heights are configured`() {
        assertEquals(16f, AppConfig.Export.PDF_LINE_HEIGHT)
        assertEquals(20f, AppConfig.Export.PDF_HEADER_LINE_HEIGHT)
    }

    @Test
    fun `export cache directory name is correct`() {
        assertEquals("exports", AppConfig.Export.CACHE_DIR_NAME)
    }

    @Test
    fun `export file prefix is correct`() {
        assertEquals("health_records_", AppConfig.Export.PDF_FILE_PREFIX)
        assertEquals("health_records_", AppConfig.Export.CSV_FILE_PREFIX)
    }
}
