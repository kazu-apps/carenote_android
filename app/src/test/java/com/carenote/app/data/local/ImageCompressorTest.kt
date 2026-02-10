package com.carenote.app.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.carenote.app.config.AppConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.RandomAccessFile

@RunWith(RobolectricTestRunner::class)
class ImageCompressorTest {

    private lateinit var context: Context
    private lateinit var compressor: ImageCompressor
    private lateinit var cacheDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        compressor = ImageCompressor(context)
        cacheDir = File(context.cacheDir, AppConfig.Photo.CACHE_DIR_NAME)
        cacheDir.mkdirs()
        // Clean up before each test
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun `cleanupCache deletes files older than TTL`() = runTest {
        val oldFile = File(cacheDir, "old_photo.jpg").apply {
            createNewFile()
            writeBytes(ByteArray(1024))
        }
        val eightDaysAgo = System.currentTimeMillis() - 8L * 24 * 60 * 60 * 1000
        oldFile.setLastModified(eightDaysAgo)

        compressor.cleanupCache()

        assertTrue("Old file should be deleted", !oldFile.exists())
    }

    @Test
    fun `cleanupCache keeps files within TTL`() = runTest {
        val recentFile = File(cacheDir, "recent_photo.jpg").apply {
            createNewFile()
            writeBytes(ByteArray(1024))
        }
        val oneDayAgo = System.currentTimeMillis() - 1L * 24 * 60 * 60 * 1000
        recentFile.setLastModified(oneDayAgo)

        compressor.cleanupCache()

        assertTrue("Recent file should be kept", recentFile.exists())
    }

    @Test
    fun `cleanupCache deletes oldest files when size exceeds limit`() = runTest {
        // Create files that exceed the size limit (100MB)
        // Use 3 files of ~40MB each = 120MB total (over 100MB limit)
        val sizePerFile = 40L * 1024 * 1024 // 40MB

        val oldestFile = createLargeFile("oldest.jpg", sizePerFile, daysAgo = 3)
        val middleFile = createLargeFile("middle.jpg", sizePerFile, daysAgo = 2)
        val newestFile = createLargeFile("newest.jpg", sizePerFile, daysAgo = 1)

        compressor.cleanupCache()

        // Oldest file should be deleted to bring total under 100MB
        assertTrue("Oldest file should be deleted", !oldestFile.exists())
        // Middle and newest should remain (80MB < 100MB limit)
        assertTrue("Middle file should be kept", middleFile.exists())
        assertTrue("Newest file should be kept", newestFile.exists())
    }

    @Test
    fun `cleanupCache handles empty directory`() = runTest {
        // Directory exists but is empty — should not throw
        assertEquals(0, cacheDir.listFiles()?.size ?: 0)

        compressor.cleanupCache()

        assertTrue("Cache dir should still exist", cacheDir.exists())
    }

    @Test
    fun `cleanupCache handles nonexistent directory`() = runTest {
        // Delete the cache directory entirely
        cacheDir.deleteRecursively()
        assertTrue("Cache dir should not exist", !cacheDir.exists())

        // Should not throw
        compressor.cleanupCache()
    }

    private fun createLargeFile(name: String, size: Long, daysAgo: Int): File {
        val file = File(cacheDir, name)
        RandomAccessFile(file, "rw").use { it.setLength(size) }
        val timestamp = System.currentTimeMillis() - daysAgo.toLong() * 24 * 60 * 60 * 1000
        file.setLastModified(timestamp)
        return file
    }
}
