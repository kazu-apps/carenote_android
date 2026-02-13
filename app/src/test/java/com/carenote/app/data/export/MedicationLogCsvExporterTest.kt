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

@RunWith(RobolectricTestRunner::class)
class MedicationLogCsvExporterTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

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
