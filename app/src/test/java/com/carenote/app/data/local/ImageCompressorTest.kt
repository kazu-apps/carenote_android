package com.carenote.app.data.local

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.test.core.app.ApplicationProvider
import com.carenote.app.config.AppConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import android.graphics.Bitmap
import androidx.core.net.toUri
import org.junit.Assert.fail
import java.io.File
import java.io.FileOutputStream
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
        // Register MIME types so extension-based fallback resolves correctly
        @Suppress("DEPRECATION")
        val mimeTypeShadow = Shadows.shadowOf(MimeTypeMap.getSingleton())
        mimeTypeShadow.addExtensionMimeTypeMapping("jpg", "image/jpeg")
        mimeTypeShadow.addExtensionMimeTypeMapping("jpeg", "image/jpeg")
        mimeTypeShadow.addExtensionMimeTypeMapping("png", "image/png")
        mimeTypeShadow.addExtensionMimeTypeMapping("webp", "image/webp")
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

    @Test
    fun `compress throws for unsupported MIME type`() = runTest {
        // Create a text file and get its URI
        val textFile = File(context.cacheDir, "test.txt").apply {
            writeText("not an image")
        }
        val uri = textFile.toUri()

        try {
            compressor.compress(uri)
            fail("Expected IllegalArgumentException for unsupported MIME type")
        } catch (e: IllegalArgumentException) {
            assertTrue(
                "Error should mention unsupported format",
                e.message?.contains("Unsupported image format") == true
            )
        }
    }

    @Test
    fun `compress throws for file exceeding size limit`() = runTest {
        // Create an oversized JPEG file (just header + padding)
        // AppConfig.Photo.MAX_IMAGE_SIZE_BYTES = 5_242_880 (5MB)
        val largeFile = File(context.cacheDir, "large.jpg").apply {
            // Write JPEG header bytes followed by padding to exceed limit
            outputStream().use { os ->
                // JPEG magic bytes
                os.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()))
                // Write 6MB of padding
                val buffer = ByteArray(1024 * 1024) // 1MB
                repeat(6) { os.write(buffer) }
            }
        }
        val uri = largeFile.toUri()

        try {
            compressor.compress(uri)
            fail("Expected IllegalArgumentException for oversized file")
        } catch (e: IllegalArgumentException) {
            assertTrue(
                "Error should mention image too large",
                e.message?.contains("Image too large") == true ||
                    e.message?.contains("Unsupported image format") == true
            )
        }
    }

    @Test
    fun `compress succeeds for valid JPEG`() = runTest {
        // Create a minimal valid bitmap and save as JPEG
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val jpegFile = File(context.cacheDir, "valid_test.jpg")
        FileOutputStream(jpegFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        bitmap.recycle()
        val uri = jpegFile.toUri()

        val result = compressor.compress(uri)
        assertTrue(
            "Result URI should point to a file",
            result.path?.isNotEmpty() == true
        )
    }

    private fun createLargeFile(name: String, size: Long, daysAgo: Int): File {
        val file = File(cacheDir, name)
        RandomAccessFile(file, "rw").use { it.setLength(size) }
        val timestamp = System.currentTimeMillis() - daysAgo.toLong() * 24 * 60 * 60 * 1000
        file.setLastModified(timestamp)
        return file
    }
}
