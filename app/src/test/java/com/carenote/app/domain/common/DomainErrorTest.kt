package com.carenote.app.domain.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * DomainError sealed class のユニットテスト
 * 各エラー型の生成、プロパティ、when式での網羅性を検証
 */
class DomainErrorTest {

    @Test
    fun `DatabaseError carries message and cause`() {
        val cause = RuntimeException("db crash")
        val error = DomainError.DatabaseError("Database failed", cause)
        assertEquals("Database failed", error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `DatabaseError cause is null by default`() {
        val error = DomainError.DatabaseError("Database failed")
        assertNull(error.cause)
    }

    @Test
    fun `NotFoundError carries message`() {
        val error = DomainError.NotFoundError("Medication not found: 123")
        assertEquals("Medication not found: 123", error.message)
        assertNull(error.cause)
    }

    @Test
    fun `ValidationError carries message and optional field`() {
        val error = DomainError.ValidationError("Name is required", "name")
        assertEquals("Name is required", error.message)
        assertEquals("name", error.field)
    }

    @Test
    fun `ValidationError field is null by default`() {
        val error = DomainError.ValidationError("Invalid input")
        assertNull(error.field)
    }

    @Test
    fun `NetworkError carries message and cause`() {
        val cause = java.io.IOException("timeout")
        val error = DomainError.NetworkError("Network failed", cause)
        assertEquals("Network failed", error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `UnauthorizedError carries message`() {
        val error = DomainError.UnauthorizedError("Session expired")
        assertEquals("Session expired", error.message)
        assertNull(error.cause)
    }

    @Test
    fun `UnknownError carries message and cause`() {
        val cause = IllegalStateException("unexpected")
        val error = DomainError.UnknownError("Something went wrong", cause)
        assertEquals("Something went wrong", error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `SecurityError carries message`() {
        val error = DomainError.SecurityError("Root detected")
        assertEquals("Root detected", error.message)
    }

    @Test
    fun `when expression covers all error types`() {
        val errors: List<DomainError> = listOf(
            DomainError.DatabaseError("db"),
            DomainError.NotFoundError("not found"),
            DomainError.ValidationError("invalid"),
            DomainError.NetworkError("network"),
            DomainError.UnauthorizedError("unauth"),
            DomainError.UnknownError("unknown"),
            DomainError.SecurityError("security")
        )

        errors.forEach { error ->
            val description = when (error) {
                is DomainError.DatabaseError -> "database"
                is DomainError.NotFoundError -> "not_found"
                is DomainError.ValidationError -> "validation"
                is DomainError.NetworkError -> "network"
                is DomainError.UnauthorizedError -> "unauthorized"
                is DomainError.UnknownError -> "unknown"
                is DomainError.SecurityError -> "security"
            }
            assertTrue(description.isNotEmpty())
        }
    }

    @Test
    fun `DomainError is not Throwable`() {
        val error: DomainError = DomainError.DatabaseError("test")
        assertTrue(error !is Throwable)
    }

    @Test
    fun `DomainException wraps DomainError`() {
        val domainError = DomainError.NotFoundError("not found")
        val exception = DomainException(domainError)
        assertEquals(domainError, exception.domainError)
        assertEquals("not found", exception.message)
    }
}
