package com.carenote.app.testing

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultMatchersTest {

    // --- assertSuccess ---

    @Test
    fun `assertSuccess returns value on Success`() {
        val result: Result<String, DomainError> = Result.Success("hello")
        val value = result.assertSuccess()
        assertEquals("hello", value)
    }

    @Test(expected = AssertionError::class)
    fun `assertSuccess throws on Failure`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.NotFoundError("not found"))
        result.assertSuccess()
    }

    @Test
    fun `assertSuccessValue passes on matching value`() {
        val result: Result<Int, DomainError> = Result.Success(42)
        result.assertSuccessValue(42)
    }

    // --- assertFailure ---

    @Test
    fun `assertFailure returns error on Failure`() {
        val error = DomainError.DatabaseError("db error")
        val result: Result<String, DomainError> = Result.Failure(error)
        val returned = result.assertFailure()
        assertEquals(error, returned)
    }

    @Test(expected = AssertionError::class)
    fun `assertFailure throws on Success`() {
        val result: Result<String, DomainError> = Result.Success("hello")
        result.assertFailure()
    }

    // --- DomainError specific matchers ---

    @Test
    fun `assertDatabaseError passes on DatabaseError`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.DatabaseError("db fail"))
        val error = result.assertDatabaseError()
        assertEquals("db fail", error.message)
    }

    @Test
    fun `assertDatabaseError with message check`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.DatabaseError("specific msg"))
        result.assertDatabaseError("specific msg")
    }

    @Test
    fun `assertNotFoundError passes on NotFoundError`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.NotFoundError("not found"))
        val error = result.assertNotFoundError()
        assertEquals("not found", error.message)
    }

    @Test
    fun `assertValidationError passes on ValidationError`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.ValidationError("invalid"))
        val error = result.assertValidationError()
        assertEquals("invalid", error.message)
    }

    @Test
    fun `assertNetworkError passes on NetworkError`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.NetworkError("timeout"))
        val error = result.assertNetworkError()
        assertEquals("timeout", error.message)
    }

    @Test
    fun `assertUnauthorizedError passes on UnauthorizedError`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.UnauthorizedError("no auth"))
        val error = result.assertUnauthorizedError()
        assertEquals("no auth", error.message)
    }

    @Test
    fun `assertUnknownError passes on UnknownError`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.UnknownError("unknown"))
        val error = result.assertUnknownError()
        assertEquals("unknown", error.message)
    }

    @Test
    fun `assertSecurityError passes on SecurityError`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.SecurityError("root detected"))
        val error = result.assertSecurityError()
        assertEquals("root detected", error.message)
    }

    @Test(expected = AssertionError::class)
    fun `assertDatabaseError throws on wrong error type`() {
        val result: Result<String, DomainError> = Result.Failure(DomainError.NotFoundError("not found"))
        result.assertDatabaseError()
    }
}
