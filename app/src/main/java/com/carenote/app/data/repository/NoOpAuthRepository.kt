package com.carenote.app.data.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.User
import com.carenote.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class NoOpAuthRepository : AuthRepository {

    override val currentUser: Flow<User?> = MutableStateFlow(null)

    override fun getCurrentUser(): User? = null

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<User, DomainError> {
        Timber.w("signUp called but Firebase is not configured")
        return Result.Failure(firebaseUnavailableError())
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<User, DomainError> {
        Timber.w("signIn called but Firebase is not configured")
        return Result.Failure(firebaseUnavailableError())
    }

    override suspend fun signOut(): Result<Unit, DomainError> {
        return Result.Success(Unit)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit, DomainError> {
        Timber.w("sendPasswordResetEmail called but Firebase is not configured")
        return Result.Failure(firebaseUnavailableError())
    }

    override suspend fun sendEmailVerification(): Result<Unit, DomainError> {
        Timber.w("sendEmailVerification called but Firebase is not configured")
        return Result.Failure(firebaseUnavailableError())
    }

    override suspend fun reauthenticate(password: String): Result<Unit, DomainError> {
        Timber.w("reauthenticate called but Firebase is not configured")
        return Result.Failure(firebaseUnavailableError())
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit, DomainError> {
        Timber.w("updatePassword called but Firebase is not configured")
        return Result.Failure(firebaseUnavailableError())
    }

    override suspend fun deleteAccount(): Result<Unit, DomainError> {
        Timber.w("deleteAccount called but Firebase is not configured")
        return Result.Failure(firebaseUnavailableError())
    }

    private fun firebaseUnavailableError(): DomainError.NetworkError =
        DomainError.NetworkError("Firebase is not configured. Please add google-services.json.")
}
