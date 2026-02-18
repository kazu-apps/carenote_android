package com.carenote.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import net.zetetic.database.sqlcipher.SQLiteDatabase
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DatabaseRecoveryHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun recoverIfNeeded(dbFile: File, passphrase: ByteArray) {
        if (!dbFile.exists()) return

        if (canOpenDatabase(dbFile, passphrase)) return

        Timber.w("Passphrase mismatch detected, backing up and deleting database for recovery")
        backupDatabaseFiles(dbFile)
        deleteDatabaseFiles(dbFile)
        Timber.i("Database files deleted, will be recreated on next access")
    }

    private fun backupDatabaseFiles(dbFile: File) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupDir = File(context.cacheDir, "db-backup").apply { mkdirs() }
            val backupFile = File(backupDir, "${dbFile.name}_$timestamp")
            dbFile.copyTo(backupFile, overwrite = true)

            val walFile = File("${dbFile.absolutePath}-wal")
            if (walFile.exists()) {
                walFile.copyTo(File(backupDir, "${walFile.name}_$timestamp"), overwrite = true)
            }
            val shmFile = File("${dbFile.absolutePath}-shm")
            if (shmFile.exists()) {
                shmFile.copyTo(File(backupDir, "${shmFile.name}_$timestamp"), overwrite = true)
            }

            Timber.i("Database backup created at: ${backupDir.absolutePath}")
        } catch (e: Exception) {
            Timber.w("Failed to backup database files: $e")
        }
    }

    internal fun canOpenDatabase(dbFile: File, passphrase: ByteArray): Boolean {
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                passphrase,
                null,
                SQLiteDatabase.OPEN_READONLY,
                null,
                null
            )
            true
        } catch (e: android.database.sqlite.SQLiteException) {
            if (e.message?.contains("not a database") == true) {
                Timber.d("Passphrase mismatch detected")
                false
            } else {
                throw e
            }
        } finally {
            db?.close()
        }
    }

    internal fun deleteDatabaseFiles(dbFile: File) {
        dbFile.delete()
        File("${dbFile.absolutePath}-wal").delete()
        File("${dbFile.absolutePath}-shm").delete()
        File("${dbFile.absolutePath}-journal").delete()
    }
}
