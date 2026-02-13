package com.carenote.app.data.util

import java.io.File
import java.io.RandomAccessFile
import kotlin.random.Random

/**
 * PII を含むファイルの安全な削除ユーティリティ。
 * ファイルを上書き（ランダム → 0xFF → 0x00）してからディスク削除する。
 * ファイルロック時は graceful fallback（通常の delete）を行う。
 */
object SecureFileDeleter {
    fun delete(file: File): Boolean {
        if (!file.exists()) return true
        return try {
            val length = file.length().toInt()
            if (length > 0) {
                RandomAccessFile(file, "rw").use { raf ->
                    val buffer = ByteArray(length)
                    Random.nextBytes(buffer)
                    raf.seek(0)
                    raf.write(buffer)
                    for (i in buffer.indices) { buffer[i] = 0xFF.toByte() }
                    raf.seek(0)
                    raf.write(buffer)
                    for (i in buffer.indices) { buffer[i] = 0x00 }
                    raf.seek(0)
                    raf.write(buffer)
                }
            }
            file.delete()
        } catch (_: Exception) {
            file.delete()
        }
    }

    fun deleteDirectory(dir: File): Boolean {
        if (!dir.exists()) return true
        if (!dir.isDirectory) return delete(dir)
        var allDeleted = true
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                if (!deleteDirectory(child)) allDeleted = false
            } else {
                if (!delete(child)) allDeleted = false
            }
        }
        if (!dir.delete()) allDeleted = false
        return allDeleted
    }
}
