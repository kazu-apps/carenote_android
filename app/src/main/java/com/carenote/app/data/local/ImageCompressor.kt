package com.carenote.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.ImageCompressorInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageCompressorInterface {

    override suspend fun compress(sourceUri: Uri): Uri = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(sourceUri)
            ?: resolveMimeTypeFromExtension(sourceUri)
        if (mimeType !in ALLOWED_MIME_TYPES) {
            throw IllegalArgumentException(
                "Unsupported image format: $mimeType"
            )
        }

        val fileDescriptor = context.contentResolver
            .openAssetFileDescriptor(sourceUri, "r")
        val fileSize = fileDescriptor?.length ?: -1L
        fileDescriptor?.close()
        if (fileSize > AppConfig.Photo.MAX_IMAGE_SIZE_BYTES) {
            throw IllegalArgumentException(
                "Image too large: $fileSize bytes"
            )
        }

        val inputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw IllegalArgumentException("Cannot open input stream for URI: $sourceUri")

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        inputStream.use { BitmapFactory.decodeStream(it, null, options) }

        val maxDim = AppConfig.Photo.MAX_DIMENSION
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxDim)
        options.inJustDecodeBounds = false

        val decodedStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw IllegalArgumentException("Cannot re-open input stream for URI: $sourceUri")
        val bitmap = decodedStream.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalArgumentException("Failed to decode bitmap from URI: $sourceUri")

        val photosDir = File(context.cacheDir, AppConfig.Photo.CACHE_DIR_NAME).also { it.mkdirs() }
        val outputFile = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")

        FileOutputStream(outputFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, AppConfig.Photo.COMPRESSION_QUALITY, fos)
        }
        bitmap.recycle()

        Timber.d("Compressed photo: ${outputFile.length()} bytes")
        outputFile.toUri()
    }

    override suspend fun cleanupCache(): Unit = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, AppConfig.Photo.CACHE_DIR_NAME)
        if (!cacheDir.exists() || !cacheDir.isDirectory) return@withContext

        val files = cacheDir.listFiles() ?: return@withContext
        val now = System.currentTimeMillis()
        var deletedByTtl = 0

        // Phase 1: TTL — 期限切れファイルを削除
        val remaining = files.filter { file ->
            if (now - file.lastModified() > AppConfig.Photo.CACHE_MAX_AGE_MS) {
                file.delete()
                deletedByTtl++
                false
            } else {
                true
            }
        }

        // Phase 2: サイズ上限 — 古い順に削除
        var deletedBySize = 0
        var totalSize = remaining.sumOf { it.length() }
        if (totalSize > AppConfig.Photo.CACHE_MAX_SIZE_BYTES) {
            val sorted = remaining.sortedBy { it.lastModified() }
            for (file in sorted) {
                if (totalSize <= AppConfig.Photo.CACHE_MAX_SIZE_BYTES) break
                val size = file.length()
                file.delete()
                totalSize -= size
                deletedBySize++
            }
        }

        if (deletedByTtl > 0 || deletedBySize > 0) {
            Timber.d("Cache cleanup: deleted $deletedByTtl by TTL, $deletedBySize by size limit")
        }
    }

    companion object {
        private val ALLOWED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/webp"
        )
    }

    private fun resolveMimeTypeFromExtension(uri: Uri): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        if (width > maxDim || height > maxDim) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            while ((halfWidth / inSampleSize) >= maxDim && (halfHeight / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
