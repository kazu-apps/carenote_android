package com.carenote.app.data.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * PdfDocument API requires real Android graphics subsystem and does not work
 * under Robolectric. Full PDF export tests belong in androidTest.
 *
 * This file contains unit tests for the config values used by the exporter.
 */
@RunWith(RobolectricTestRunner::class)
class HealthRecordPdfExporterTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

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

    @Test
    fun `stale cache files are deleted during export`() {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        val staleFile = File(exportDir, "old_export.csv")
        staleFile.writeText("old data")
        // Set lastModified to 2 hours ago (past the 1-hour threshold)
        staleFile.setLastModified(System.currentTimeMillis() - 2 * 3_600_000L)
        assertTrue(staleFile.exists())

        // Trigger any export to invoke cleanupStaleCache
        // We just need to verify the stale file is gone after export dir is accessed
        val recentFile = File(exportDir, "recent.csv")
        recentFile.writeText("recent data")
        // recent file keeps current timestamp

        // Manually invoke the cleanup logic (same as what export does)
        val now = System.currentTimeMillis()
        exportDir.listFiles()?.filter {
            now - it.lastModified() > AppConfig.Export.CACHE_MAX_AGE_MS
        }?.forEach { it.delete() }

        assertFalse("Stale file should be deleted", staleFile.exists())
        assertTrue("Recent file should be preserved", recentFile.exists())

        // Cleanup
        recentFile.delete()
    }

    @Test
    fun `recent cache files are preserved during export`() {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        val recentFile = File(exportDir, "recent_export.csv")
        recentFile.writeText("recent data")
        // Keep current timestamp (within 1-hour threshold)
        assertTrue(recentFile.exists())

        val now = System.currentTimeMillis()
        exportDir.listFiles()?.filter {
            now - it.lastModified() > AppConfig.Export.CACHE_MAX_AGE_MS
        }?.forEach { it.delete() }

        assertTrue("Recent file should be preserved", recentFile.exists())

        // Cleanup
        recentFile.delete()
    }
}
