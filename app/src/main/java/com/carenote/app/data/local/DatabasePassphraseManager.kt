package com.carenote.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.carenote.app.config.AppConfig
import timber.log.Timber
import java.io.File
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class DatabasePassphraseManager @Inject constructor() {

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = getOrRecreatePrefs(context)

        val masterPassphrase = getMasterPassphrase(prefs)
        val salt = getOrCreateSalt(prefs)

        val derivedKey = deriveKey(masterPassphrase, salt)
        masterPassphrase.fill(0) // Zero-clear master passphrase from memory

        return derivedKey
    }

    private fun getMasterPassphrase(prefs: SharedPreferences): ByteArray {
        val existingPassphrase = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existingPassphrase != null) {
            return existingPassphrase.toByteArray(Charsets.ISO_8859_1)
        }

        val newPassphrase = ByteArray(PASSPHRASE_LENGTH).also {
            SecureRandom().nextBytes(it)
        }

        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, String(newPassphrase, Charsets.ISO_8859_1))
            .commit()

        return newPassphrase
    }

    private fun getOrCreateSalt(prefs: SharedPreferences): ByteArray {
        val existingSalt = prefs.getString(KEY_DB_SALT, null)
        if (existingSalt != null) {
            return existingSalt.toByteArray(Charsets.ISO_8859_1)
        }

        val newSalt = ByteArray(AppConfig.Security.PBKDF2_SALT_LENGTH).also {
            SecureRandom().nextBytes(it)
        }

        prefs.edit()
            .putString(KEY_DB_SALT, String(newSalt, Charsets.ISO_8859_1))
            .commit()

        return newSalt
    }

    private fun deriveKey(passphrase: ByteArray, salt: ByteArray): ByteArray {
        val chars = CharArray(passphrase.size) { passphrase[it].toInt().toChar() }
        val keySpec = PBEKeySpec(
            chars,
            salt,
            AppConfig.Security.PBKDF2_ITERATIONS,
            AppConfig.Security.PBKDF2_KEY_LENGTH_BITS
        )
        try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            return factory.generateSecret(keySpec).encoded
        } finally {
            keySpec.clearPassword()
            chars.fill('\u0000')
        }
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
        private const val KEY_DB_SALT = "db_salt"
        private const val PASSPHRASE_LENGTH = 32
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    }
}
