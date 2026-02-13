package com.carenote.app.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExceptionMaskerTest {

    @Test
    fun `mask IllegalArgumentException returns type name only`() {
        val e = IllegalArgumentException("user@email.com is invalid")
        assertEquals("IllegalArgumentException", ExceptionMasker.mask(e))
    }

    @Test
    fun `mask RuntimeException returns type name only`() {
        val e = RuntimeException("Unexpected error for uid=abc123")
        assertEquals("RuntimeException", ExceptionMasker.mask(e))
    }

    @Test
    fun `mask NullPointerException returns type name only`() {
        val e = NullPointerException("field was null")
        assertEquals("NullPointerException", ExceptionMasker.mask(e))
    }

    @Test
    fun `mask does not include exception message in output`() {
        val piiMessage = "User john@example.com failed with uid=12345"
        val e = RuntimeException(piiMessage)
        val result = ExceptionMasker.mask(e)
        assertFalse(result.contains("john@example.com"))
        assertFalse(result.contains("12345"))
    }

    @Test
    fun `mask Exception with null message returns type name`() {
        val e = RuntimeException(null as String?)
        assertEquals("RuntimeException", ExceptionMasker.mask(e))
    }

    @Test
    fun `mask generic Exception returns Exception`() {
        assertEquals("Exception", ExceptionMasker.mask(Exception("error")))
    }

    @Test
    fun `mask IndexOutOfBoundsException returns type name`() {
        assertEquals("IndexOutOfBoundsException", ExceptionMasker.mask(IndexOutOfBoundsException("idx 5")))
    }

    @Test
    fun `mask UnsupportedOperationException returns type name`() {
        assertEquals("UnsupportedOperationException", ExceptionMasker.mask(UnsupportedOperationException("nope")))
    }
}
