package com.carenote.app.data.export

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskPdfExporterTest {
    private val columnWidths = intArrayOf(90, 120, 65, 45, 50, 55, 90)

    @Test
    fun `column widths sum equals usable page width`() {
        val usableWidth = AppConfig.Export.PDF_PAGE_WIDTH - 2 * AppConfig.Export.PDF_MARGIN
        assertEquals(usableWidth, columnWidths.sum())
    }

    @Test
    fun `column count is 7`() {
        assertEquals(7, columnWidths.size)
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
        assertEquals("task_export_pdf", AppConfig.Analytics.EVENT_TASK_EXPORT_PDF)
    }
}
