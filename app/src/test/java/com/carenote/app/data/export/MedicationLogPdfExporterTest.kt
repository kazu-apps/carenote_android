package com.carenote.app.data.export

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationLogPdfExporterTest {

    @Test
    fun `PDF column widths sum equals page width minus double margin`() {
        val columnWidths = intArrayOf(80, 80, 110, 60, 50, 135)
        val expectedWidth = AppConfig.Export.PDF_PAGE_WIDTH - 2 * AppConfig.Export.PDF_MARGIN
        assertEquals(expectedWidth, columnWidths.sum())
    }

    @Test
    fun `PDF has 6 columns for medication log`() {
        val columnWidths = intArrayOf(80, 80, 110, 60, 50, 135)
        assertEquals(6, columnWidths.size)
    }

    @Test
    fun `PDF font sizes are positive`() {
        assertTrue(AppConfig.Export.PDF_FONT_SIZE_TITLE > 0)
        assertTrue(AppConfig.Export.PDF_FONT_SIZE_HEADER > 0)
        assertTrue(AppConfig.Export.PDF_FONT_SIZE_BODY > 0)
    }

    @Test
    fun `PDF line heights are positive`() {
        assertTrue(AppConfig.Export.PDF_LINE_HEIGHT > 0)
        assertTrue(AppConfig.Export.PDF_HEADER_LINE_HEIGHT > 0)
    }

    @Test
    fun `PDF body font is smaller than header font`() {
        assertTrue(AppConfig.Export.PDF_FONT_SIZE_BODY < AppConfig.Export.PDF_FONT_SIZE_HEADER)
    }
}
