package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.User
import com.carenote.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class FakeAuthRepository : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    var shouldFail = false
    var failureError: DomainError = DomainError.UnauthorizedError("Test error")
    var isEmailVerified = true

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    fun clear() {
        _currentUser.value = null
        shouldFail = false
        failureError = DomainError.UnauthorizedError("Test error")
        isEmailVerified = true
    }

    override fun getCurrentUser(): User? = _currentUser.value

    override suspend fun signIn(email: String, password: String): Result<User, DomainError> {
        if (shouldFail) return Result.Failure(failureError)
        val user = createUser(email = email)
        _currentUser.value = user
        return Result.Success(user)
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<User, DomainError> {
        if (shouldFail) return Result.Failure(failureError)
        val user = createUser(email = email, name = displayName)
        _currentUser.value = user
        return Result.Success(user)
    }

    override suspend fun signOut(): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(failureError)
        _currentUser.value = null
        return Result.Success(Unit)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(failureError)
        return Result.Success(Unit)
    }

    override suspend fun sendEmailVerification(): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(failureError)
        return Result.Success(Unit)
    }

    override suspend fun reauthenticate(password: String): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(failureError)
        return Result.Success(Unit)
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(failureError)
        return Result.Success(Unit)
    }

    override suspend fun deleteAccount(): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(failureError)
        _currentUser.value = null
        return Result.Success(Unit)
    }

    private fun createUser(
        uid: String = "test-uid",
        email: String = "test@example.com",
        name: String = "Test User"
    ) = User(
        uid = uid,
        email = email,
        name = name,
        createdAt = LocalDateTime.now(),
        isPremium = false,
        isEmailVerified = isEmailVerified
    )
}
