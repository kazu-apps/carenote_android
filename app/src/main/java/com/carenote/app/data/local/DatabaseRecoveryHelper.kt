package com.carenote.app.data.local

import net.zetetic.database.sqlcipher.SQLiteDatabase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class DatabaseRecoveryHelper @Inject constructor() {

    fun recoverIfNeeded(dbFile: File, passphrase: ByteArray) {
        if (!dbFile.exists()) return

        if (canOpenDatabase(dbFile, passphrase)) return

        Timber.w("Passphrase mismatch detected, deleting database for recovery")
        deleteDatabaseFiles(dbFile)
        Timber.i("Database files deleted, will be recreated on next access")
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
