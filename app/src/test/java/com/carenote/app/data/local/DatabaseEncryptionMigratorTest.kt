package com.carenote.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseEncryptionMigratorTest {

    @Test
    fun `passphraseHex formats bytes correctly`() {
        // Test the hex conversion logic used in exportToEncrypted
        val passphrase = byteArrayOf(
            0x0A.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x7F.toByte()
        )
        val passphraseHex = CharArray(passphrase.size * 2)
        passphrase.forEachIndexed { i, byte ->
            val hex = "%02x".format(byte)
            passphraseHex[i * 2] = hex[0]
            passphraseHex[i * 2 + 1] = hex[1]
        }

        assertEquals("0aff007f", String(passphraseHex))
    }

    @Test
    fun `passphraseHex zero-clear wipes data`() {
        val passphrase = byteArrayOf(0xDE.toByte(), 0xAD.toByte())
        val passphraseHex = CharArray(passphrase.size * 2)
        passphrase.forEachIndexed { i, byte ->
            val hex = "%02x".format(byte)
            passphraseHex[i * 2] = hex[0]
            passphraseHex[i * 2 + 1] = hex[1]
        }

        // Verify hex is correct before clearing
        assertEquals("dead", String(passphraseHex))

        // Zero-clear
        passphraseHex.fill('\u0000')

        // Verify all chars are null
        passphraseHex.forEach { char ->
            assertEquals('\u0000', char)
        }
    }
}
