package com.carenote.app.testing

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.common.SyncResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

fun <T, E> Result<T, E>.assertSuccess(): T {
    assertTrue("Expected Success but was $this", this is Result.Success)
    return (this as Result.Success).value
}

fun <T, E> Result<T, E>.assertFailure(): E {
    assertTrue("Expected Failure but was $this", this is Result.Failure)
    return (this as Result.Failure).error
}

fun <T, E> Result<T, E>.assertSuccessValue(expected: T) {
    val value = assertSuccess()
    assertEquals(expected, value)
}

fun <T> Result<T, DomainError>.assertDatabaseError(message: String? = null): DomainError.DatabaseError {
    val error = assertFailure()
    assertTrue("Expected DatabaseError but was $error", error is DomainError.DatabaseError)
    if (message != null) {
        assertEquals(message, error.message)
    }
    return error as DomainError.DatabaseError
}

fun <T> Result<T, DomainError>.assertNotFoundError(message: String? = null): DomainError.NotFoundError {
    val error = assertFailure()
    assertTrue("Expected NotFoundError but was $error", error is DomainError.NotFoundError)
    if (message != null) {
        assertEquals(message, error.message)
    }
    return error as DomainError.NotFoundError
}

fun <T> Result<T, DomainError>.assertValidationError(message: String? = null): DomainError.ValidationError {
    val error = assertFailure()
    assertTrue("Expected ValidationError but was $error", error is DomainError.ValidationError)
    if (message != null) {
        assertEquals(message, error.message)
    }
    return error as DomainError.ValidationError
}

fun <T> Result<T, DomainError>.assertNetworkError(): DomainError.NetworkError {
    val error = assertFailure()
    assertTrue("Expected NetworkError but was $error", error is DomainError.NetworkError)
    return error as DomainError.NetworkError
}

fun <T> Result<T, DomainError>.assertUnauthorizedError(): DomainError.UnauthorizedError {
    val error = assertFailure()
    assertTrue("Expected UnauthorizedError but was $error", error is DomainError.UnauthorizedError)
    return error as DomainError.UnauthorizedError
}

fun <T> Result<T, DomainError>.assertUnknownError(): DomainError.UnknownError {
    val error = assertFailure()
    assertTrue("Expected UnknownError but was $error", error is DomainError.UnknownError)
    return error as DomainError.UnknownError
}

fun <T> Result<T, DomainError>.assertSecurityError(): DomainError.SecurityError {
    val error = assertFailure()
    assertTrue("Expected SecurityError but was $error", error is DomainError.SecurityError)
    return error as DomainError.SecurityError
}

fun SyncResult.assertSyncSuccess(): SyncResult.Success {
    assertTrue("Expected SyncResult.Success but was $this", this is SyncResult.Success)
    return this as SyncResult.Success
}

fun SyncResult.assertSyncFailure(): SyncResult.Failure {
    assertTrue("Expected SyncResult.Failure but was $this", this is SyncResult.Failure)
    return this as SyncResult.Failure
}

fun SyncResult.assertSyncPartialSuccess(): SyncResult.PartialSuccess {
    assertTrue("Expected SyncResult.PartialSuccess but was $this", this is SyncResult.PartialSuccess)
    return this as SyncResult.PartialSuccess
}
