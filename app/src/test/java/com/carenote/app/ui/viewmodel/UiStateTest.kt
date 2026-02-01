package com.carenote.app.ui.viewmodel

import com.carenote.app.domain.common.DomainError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStateTest {

    @Test
    fun `Loading state isLoading returns true`() {
        val state: UiState<String> = UiState.Loading

        assertTrue(state.isLoading)
        assertFalse(state.isSuccess)
        assertFalse(state.isError)
    }

    @Test
    fun `Success state isSuccess returns true`() {
        val state: UiState<String> = UiState.Success("data")

        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertFalse(state.isError)
    }

    @Test
    fun `Error state isError returns true`() {
        val error = DomainError.UnknownError("test error")
        val state: UiState<String> = UiState.Error(error)

        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertTrue(state.isError)
    }

    @Test
    fun `Success state holds correct data`() {
        val data = listOf("item1", "item2")
        val state = UiState.Success(data)

        assertEquals(data, state.data)
    }

    @Test
    fun `Error state holds correct error`() {
        val error = DomainError.DatabaseError("db failure")
        val state = UiState.Error<String>(error)

        assertEquals(error, state.error)
    }

    @Test
    fun `getOrNull returns data for Success`() {
        val state: UiState<String> = UiState.Success("hello")

        assertEquals("hello", state.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Loading`() {
        val state: UiState<String> = UiState.Loading

        assertNull(state.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Error`() {
        val state: UiState<String> = UiState.Error(DomainError.UnknownError("err"))

        assertNull(state.getOrNull())
    }

    @Test
    fun `getOrDefault returns data for Success`() {
        val state: UiState<String> = UiState.Success("real")

        assertEquals("real", state.getOrDefault("default"))
    }

    @Test
    fun `getOrDefault returns default for Loading`() {
        val state: UiState<String> = UiState.Loading

        assertEquals("default", state.getOrDefault("default"))
    }

    @Test
    fun `getOrDefault returns default for Error`() {
        val state: UiState<String> = UiState.Error(DomainError.UnknownError("err"))

        assertEquals("default", state.getOrDefault("default"))
    }

    @Test
    fun `errorOrNull returns error for Error state`() {
        val error = DomainError.ValidationError("invalid", "field")
        val state: UiState<String> = UiState.Error(error)

        assertEquals(error, state.errorOrNull())
    }

    @Test
    fun `errorOrNull returns null for Success state`() {
        val state: UiState<String> = UiState.Success("data")

        assertNull(state.errorOrNull())
    }

    @Test
    fun `errorOrNull returns null for Loading state`() {
        val state: UiState<String> = UiState.Loading

        assertNull(state.errorOrNull())
    }

    @Test
    fun `Success states with same data are equal`() {
        val state1 = UiState.Success("data")
        val state2 = UiState.Success("data")

        assertEquals(state1, state2)
    }

    @Test
    fun `Error states with same error are equal`() {
        val error = DomainError.NotFoundError("not found")
        val state1 = UiState.Error<String>(error)
        val state2 = UiState.Error<String>(error)

        assertEquals(state1, state2)
    }

    @Test
    fun `Loading is singleton`() {
        val state1: UiState<String> = UiState.Loading
        val state2: UiState<Int> = UiState.Loading

        assertTrue(state1 === state2)
    }

    @Test
    fun `when expression is exhaustive`() {
        val state: UiState<String> = UiState.Loading
        val result = when (state) {
            is UiState.Loading -> "loading"
            is UiState.Success -> "success: ${state.data}"
            is UiState.Error -> "error: ${state.error.message}"
        }

        assertEquals("loading", result)
    }

    @Test
    fun `Success with null data is valid`() {
        val state = UiState.Success<String?>(null)

        assertTrue(state.isSuccess)
        assertNull(state.data)
        assertNull(state.getOrNull())
    }

    @Test
    fun `Error with different DomainError types`() {
        val errors = listOf(
            DomainError.DatabaseError("db"),
            DomainError.NotFoundError("nf"),
            DomainError.ValidationError("val"),
            DomainError.NetworkError("net"),
            DomainError.UnauthorizedError("unauth"),
            DomainError.UnknownError("unknown")
        )

        errors.forEach { error ->
            val state = UiState.Error<String>(error)
            assertTrue(state.isError)
            assertEquals(error, state.error)
        }
    }
}
