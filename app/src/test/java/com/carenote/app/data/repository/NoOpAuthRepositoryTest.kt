package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoOpAuthRepositoryTest {

    private lateinit var repository: NoOpAuthRepository

    @Before
    fun setup() {
        repository = NoOpAuthRepository()
    }

    @Test
    fun `currentUser emits null`() = runTest {
        repository.currentUser.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `getCurrentUser returns null`() {
        assertNull(repository.getCurrentUser())
    }

    @Test
    fun `signUp returns NetworkError`() = runTest {
        val result = repository.signUp("test@example.com", "password", "Test")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.NetworkError)
    }

    @Test
    fun `signIn returns NetworkError`() = runTest {
        val result = repository.signIn("test@example.com", "password")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.NetworkError)
    }

    @Test
    fun `signOut returns Success`() = runTest {
        val result = repository.signOut()
        assertTrue(result is Result.Success)
    }

    @Test
    fun `sendPasswordResetEmail returns NetworkError`() = runTest {
        val result = repository.sendPasswordResetEmail("test@example.com")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.NetworkError)
    }

    @Test
    fun `sendEmailVerification returns NetworkError`() = runTest {
        val result = repository.sendEmailVerification()
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.NetworkError)
    }

    @Test
    fun `reauthenticate returns NetworkError`() = runTest {
        val result = repository.reauthenticate("password")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.NetworkError)
    }

    @Test
    fun `updatePassword returns NetworkError`() = runTest {
        val result = repository.updatePassword("newPassword")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.NetworkError)
    }

    @Test
    fun `deleteAccount returns NetworkError`() = runTest {
        val result = repository.deleteAccount()
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.NetworkError)
    }
}
