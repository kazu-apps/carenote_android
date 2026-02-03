package com.carenote.app.domain.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Result<T, E> のユニットテスト
 * 成功/失敗の基本操作、変換、チェイン、例外キャッチを検証
 */
class ResultTest {

    @Test
    fun `Success isSuccess returns true`() {
        val result: Result<String, DomainError> = Result.Success("value")
        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
    }

    @Test
    fun `Failure isFailure returns true`() {
        val result: Result<String, DomainError> = Result.Failure(
            DomainError.NotFoundError("not found")
        )
        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
    }

    @Test
    fun `getOrNull returns value on Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `getOrNull returns null on Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("error")
        )
        assertNull(result.getOrNull())
    }

    @Test
    fun `getOrDefault returns value on Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)
        assertEquals(42, result.getOrDefault(0))
    }

    @Test
    fun `getOrDefault returns default on Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("error")
        )
        assertEquals(0, result.getOrDefault(0))
    }

    @Test
    fun `errorOrNull returns null on Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)
        assertNull(result.errorOrNull())
    }

    @Test
    fun `errorOrNull returns error on Failure`() {
        val error = DomainError.NotFoundError("not found")
        val result: Result<Int, DomainError> = Result.Failure(error)
        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun `map transforms Success value`() {
        val result = Result.Success(10).map { it * 2 }
        assertEquals(20, result.getOrNull())
    }

    @Test
    fun `map does not transform Failure`() {
        val error = DomainError.DatabaseError("error")
        val result: Result<Int, DomainError> = Result.Failure(error)
        val mapped = result.map { it * 2 }
        assertEquals(error, mapped.errorOrNull())
    }

    @Test
    fun `flatMap chains Success operations`() {
        val result = Result.Success(10)
            .flatMap { Result.Success(it + 5) }
            .flatMap { Result.Success(it * 2) }
        assertEquals(30, result.getOrNull())
    }

    @Test
    fun `flatMap short-circuits on Failure`() {
        val error = DomainError.ValidationError("invalid", "field")
        val initial: Result<Int, DomainError> = Result.Success(10)
        val result = initial
            .flatMap<Int> { Result.Failure(error) }
            .flatMap { Result.Success(it * 2) }
        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun `fold returns success value`() {
        val result: Result<Int, DomainError> = Result.Success(42)
        val folded = result.fold(
            onSuccess = { "success: $it" },
            onFailure = { "failure: ${it.message}" }
        )
        assertEquals("success: 42", folded)
    }

    @Test
    fun `fold returns failure value`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("db error")
        )
        val folded = result.fold(
            onSuccess = { "success: $it" },
            onFailure = { "failure: ${it.message}" }
        )
        assertEquals("failure: db error", folded)
    }

    @Test
    fun `onSuccess executes action on Success`() {
        var captured = 0
        Result.Success(42).onSuccess { captured = it }
        assertEquals(42, captured)
    }

    @Test
    fun `onSuccess does not execute on Failure`() {
        var captured = 0
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("error")
        )
        result.onSuccess { captured = it }
        assertEquals(0, captured)
    }

    @Test
    fun `onFailure executes action on Failure`() {
        var captured = ""
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("error msg")
        )
        result.onFailure { captured = it.message }
        assertEquals("error msg", captured)
    }

    @Test
    fun `onFailure does not execute on Success`() {
        var captured = ""
        Result.Success(42).onFailure { captured = "should not happen" }
        assertEquals("", captured)
    }

    @Test
    fun `mapError transforms error type`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("db error")
        )
        val mapped = result.mapError { it.message }
        assertEquals("db error", (mapped as Result.Failure).error)
    }

    @Test
    fun `recover converts Failure to Success`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.DatabaseError("error")
        )
        val recovered = result.recover { -1 }
        assertEquals(-1, recovered.getOrNull())
    }

    @Test
    fun `catching returns Success on no exception`() {
        val result = Result.catching { 42 }
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `catching returns Failure on exception`() {
        val result = Result.catching { throw RuntimeException("boom") }
        assertTrue(result.isFailure)
        val error = result.errorOrNull()
        assertTrue(error is DomainError.UnknownError)
        assertEquals("boom", error?.message)
    }

    @Test
    fun `getOrThrow returns value on Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)
        assertEquals(42, result.getOrThrow())
    }

    @Test(expected = DomainException::class)
    fun `getOrThrow throws on Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(
            DomainError.NotFoundError("not found")
        )
        result.getOrThrow()
    }

    @Test(expected = CancellationException::class)
    fun `catching rethrows CancellationException`() {
        Result.catching { throw CancellationException("cancelled") }
    }

    @Test(expected = CancellationException::class)
    fun `catchingSuspend rethrows CancellationException`() = runTest {
        Result.catchingSuspend { throw CancellationException("cancelled") }
    }
}
