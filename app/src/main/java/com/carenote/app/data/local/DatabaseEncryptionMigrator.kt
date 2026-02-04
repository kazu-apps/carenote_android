package com.carenote.app.data.local

import net.zetetic.database.sqlcipher.SQLiteDatabase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class DatabaseEncryptionMigrator @Inject constructor() {

    fun migrateIfNeeded(dbFile: File, passphrase: ByteArray) {
        if (!dbFile.exists()) return
        if (!isUnencrypted(dbFile)) return

        Timber.i("Migrating plaintext database to encrypted")
        encryptDatabase(dbFile, passphrase)
        Timber.i("Database encryption migration completed")
    }

    private fun isUnencrypted(dbFile: File): Boolean {
        return try {
            val db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                "",
                null,
                SQLiteDatabase.OPEN_READONLY,
                null,
                null
            )
            db.close()
            true
        } catch (e: Exception) {
            Timber.d("Database is not plaintext (already encrypted or inaccessible): $e")
            false
        }
    }

    private fun encryptDatabase(dbFile: File, passphrase: ByteArray) {
        val encryptedFile = File(dbFile.parent, "${dbFile.name}-encrypted")
        val backupFile = File(dbFile.parent, "${dbFile.name}-backup")

        try {
            exportToEncrypted(dbFile, encryptedFile, passphrase)
            swapFiles(dbFile, encryptedFile, backupFile)
        } catch (e: Exception) {
            Timber.e("Database encryption migration failed: $e")
            encryptedFile.delete()
            throw e
        }
    }

    private fun exportToEncrypted(
        dbFile: File,
        encryptedFile: File,
        passphrase: ByteArray
    ) {
        // SQLCipher ATTACH DATABASE does not support parameterized binding.
        // The path is derived from context.getDatabasePath() (system-controlled).
        val passphraseHex = passphrase.joinToString("") { "%02x".format(it) }

        val db = SQLiteDatabase.openDatabase(
            dbFile.absolutePath, "", null,
            SQLiteDatabase.OPEN_READWRITE, null, null
        )

        val version = db.version

        db.rawExecSQL(
            "ATTACH DATABASE '${encryptedFile.absolutePath}' " +
                "AS encrypted KEY \"x'$passphraseHex'\""
        )
        db.rawExecSQL("SELECT sqlcipher_export('encrypted')")
        db.rawExecSQL("DETACH DATABASE encrypted")
        db.close()

        val encryptedDb = SQLiteDatabase.openDatabase(
            encryptedFile.absolutePath, passphrase, null,
            SQLiteDatabase.OPEN_READWRITE, null, null
        )
        encryptedDb.version = version
        encryptedDb.close()
    }

    private fun swapFiles(dbFile: File, encryptedFile: File, backupFile: File) {
        check(dbFile.renameTo(backupFile)) {
            "Failed to rename original DB to backup"
        }
        if (!encryptedFile.renameTo(dbFile)) {
            backupFile.renameTo(dbFile)
            error("Failed to rename encrypted DB to target path")
        }
        backupFile.delete()
        cleanupJournalFiles(dbFile)
    }

    private fun cleanupJournalFiles(dbFile: File) {
        File("${dbFile.absolutePath}-wal").delete()
        File("${dbFile.absolutePath}-shm").delete()
        File("${dbFile.absolutePath}-journal").delete()
    }
}
