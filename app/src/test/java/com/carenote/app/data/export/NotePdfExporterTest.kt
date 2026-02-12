package com.carenote.app.data.export

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotePdfExporterTest {
    private val columnWidths = intArrayOf(100, 205, 60, 75, 75)

    @Test
    fun `column widths sum equals usable page width`() {
        val usableWidth = AppConfig.Export.PDF_PAGE_WIDTH - 2 * AppConfig.Export.PDF_MARGIN
        assertEquals(usableWidth, columnWidths.sum())
    }

    @Test
    fun `column count is 5`() {
        assertEquals(5, columnWidths.size)
    }

    @Test
    fun `PDF page dimensions are valid`() {
        assertTrue(AppConfig.Export.PDF_PAGE_WIDTH > 0)
        assertTrue(AppConfig.Export.PDF_PAGE_HEIGHT > 0)
    }

    @Test
    fun `PDF font sizes are positive`() {
        assertTrue(AppConfig.Export.PDF_FONT_SIZE_TITLE > 0)
        assertTrue(AppConfig.Export.PDF_FONT_SIZE_HEADER > 0)
        assertTrue(AppConfig.Export.PDF_FONT_SIZE_BODY > 0)
    }

    @Test
    fun `export PDF analytics event is correctly configured`() {
        assertEquals("note_export_pdf", AppConfig.Analytics.EVENT_NOTE_EXPORT_PDF)
    }
}
