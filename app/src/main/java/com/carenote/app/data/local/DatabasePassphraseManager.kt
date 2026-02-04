package com.carenote.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject

class DatabasePassphraseManager @Inject constructor() {

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = getOrRecreatePrefs(context)

        val existingPassphrase = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existingPassphrase != null) {
            return existingPassphrase.toByteArray(Charsets.ISO_8859_1)
        }

        val newPassphrase = ByteArray(PASSPHRASE_LENGTH).also {
            SecureRandom().nextBytes(it)
        }

        // Use commit() for synchronous write — passphrase must be persisted
        // before the database is encrypted with it.
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, String(newPassphrase, Charsets.ISO_8859_1))
            .commit()

        return newPassphrase
    }

    private fun getOrRecreatePrefs(context: Context): SharedPreferences {
        return try {
            createEncryptedPrefs(context)
        } catch (e: Exception) {
            Timber.w("EncryptedSharedPreferences corrupted, recreating: $e")
            deletePrefsFile(context)
            createEncryptedPrefs(context)
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun deletePrefsFile(context: Context) {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        File(prefsDir, "$PREFS_FILE_NAME.xml").delete()
    }

    companion object {
        private const val PREFS_FILE_NAME = "carenote_db_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
        private const val PASSPHRASE_LENGTH = 32
    }
}
