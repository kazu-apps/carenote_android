package com.carenote.app.data.local

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class DatabasePassphraseManagerTest {

    @Test
    fun `PBKDF2 produces consistent output for same passphrase and salt`() {
        val passphrase = "test-passphrase".toByteArray()
        val salt = ByteArray(32) { it.toByte() }

        val key1 = derivePbkdf2Key(passphrase, salt)
        val key2 = derivePbkdf2Key(passphrase, salt)

        assertArrayEquals(key1, key2)
    }

    @Test
    fun `PBKDF2 produces different output for different salts`() {
        val passphrase = "test-passphrase".toByteArray()
        val salt1 = ByteArray(32) { it.toByte() }
        val salt2 = ByteArray(32) { (it + 1).toByte() }

        val key1 = derivePbkdf2Key(passphrase, salt1)
        val key2 = derivePbkdf2Key(passphrase, salt2)

        assertFalse(key1.contentEquals(key2))
    }

    @Test
    fun `PBKDF2 produces different output for different passphrases`() {
        val salt = ByteArray(32) { it.toByte() }
        val passphrase1 = "passphrase-one".toByteArray()
        val passphrase2 = "passphrase-two".toByteArray()

        val key1 = derivePbkdf2Key(passphrase1, salt)
        val key2 = derivePbkdf2Key(passphrase2, salt)

        assertFalse(key1.contentEquals(key2))
    }

    @Test
    fun `PBKDF2 produces 256-bit key`() {
        val passphrase = "test-passphrase".toByteArray()
        val salt = ByteArray(32) { it.toByte() }

        val key = derivePbkdf2Key(passphrase, salt)

        assertEquals(32, key.size) // 256 bits = 32 bytes
    }

    @Test
    fun `PBKDF2 key is not empty`() {
        val passphrase = "test-passphrase".toByteArray()
        val salt = ByteArray(32) { it.toByte() }

        val key = derivePbkdf2Key(passphrase, salt)

        assertNotNull(key)
        assertTrue(key.any { it != 0.toByte() })
    }

    @Test
    fun `PBKDF2 key differs from raw passphrase`() {
        val passphrase = ByteArray(32) { it.toByte() }
        val salt = ByteArray(32) { (it + 100).toByte() }

        val key = derivePbkdf2Key(passphrase, salt)

        assertFalse(key.contentEquals(passphrase))
    }

    @Test
    fun `PBEKeySpec clearPassword zeroes the char array`() {
        val chars = charArrayOf('t', 'e', 's', 't')
        val salt = ByteArray(32) { it.toByte() }
        val keySpec = PBEKeySpec(
            chars,
            salt,
            AppConfig.Security.PBKDF2_ITERATIONS,
            AppConfig.Security.PBKDF2_KEY_LENGTH_BITS
        )

        keySpec.clearPassword()

        // After clearPassword, getPassword throws IllegalStateException
        try {
            keySpec.password
            assertTrue("Expected IllegalStateException", false)
        } catch (_: IllegalStateException) {
            // Expected -- password was cleared
        }
    }

    @Test
    fun `zero-clear fills byte array with zeros`() {
        val passphrase = byteArrayOf(1, 2, 3, 4, 5)
        passphrase.fill(0)

        assertTrue(passphrase.all { it == 0.toByte() })
    }

    /**
     * Helper: mirrors the PBKDF2 derivation logic from DatabasePassphraseManager
     */
    private fun derivePbkdf2Key(passphrase: ByteArray, salt: ByteArray): ByteArray {
        val chars = CharArray(passphrase.size) { passphrase[it].toInt().toChar() }
        val keySpec = PBEKeySpec(
            chars,
            salt,
            AppConfig.Security.PBKDF2_ITERATIONS,
            AppConfig.Security.PBKDF2_KEY_LENGTH_BITS
        )
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            return factory.generateSecret(keySpec).encoded
        } finally {
            keySpec.clearPassword()
            chars.fill('\u0000')
        }
    }
}
