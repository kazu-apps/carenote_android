package com.carenote.app.data.repository

import com.carenote.app.data.mapper.UserMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.User
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.BillingRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userMapper: UserMapper,
    private val billingRepository: BillingRepository
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.let { userMapper.toDomain(it, billingRepository.premiumStatus.value.isActive) }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let { userMapper.toDomain(it, billingRepository.premiumStatus.value.isActive) }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<User, DomainError> {
        return Result.catchingSuspend(::mapFirebaseException) {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("User is null after sign up")

            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdate).await()

            firebaseUser.sendEmailVerification().await()

            Timber.d("User signed up successfully")
            userMapper.toDomain(firebaseUser, billingRepository.premiumStatus.value.isActive)
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<User, DomainError> {
        return Result.catchingSuspend(::mapFirebaseException) {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("User is null after sign in")
            Timber.d("User signed in successfully")
            userMapper.toDomain(firebaseUser, billingRepository.premiumStatus.value.isActive)
        }
    }

    override suspend fun signOut(): Result<Unit, DomainError> {
        return Result.catchingSuspend(::mapFirebaseException) {
            firebaseAuth.signOut()
            Timber.d("User signed out")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit, DomainError> {
        return Result.catchingSuspend(::mapFirebaseException) {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Timber.d("Password reset email sent")
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit, DomainError> {
        return Result.catchingSuspend(::mapFirebaseException) {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No user logged in")
            user.sendEmailVerification().await()
            Timber.d("Verification email sent")
        }
    }

    override suspend fun reauthenticate(password: String): Result<Unit, DomainError> {
        return Result.catchingSuspend(::mapFirebaseException) {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No user logged in")
            val email = user.email
                ?: throw IllegalStateException("User has no email")
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            Timber.d("User reauthenticated successfully")
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit, DomainError> {
        return Result.catchingSuspend(::mapFirebaseException) {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No user logged in")
            user.updatePassword(newPassword).await()
            Timber.d("Password updated successfully")
        }
    }

    override suspend fun deleteAccount(): Result<Unit, DomainError> {
        return Result.catchingSuspend(::mapFirebaseException) {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No user logged in")
            user.delete().await()
            Timber.d("Account deleted successfully")
        }
    }

    /**
     * エラーメッセージから PII（メールアドレス等）を除去
     */
    private fun sanitizeErrorMessage(message: String?): String {
        return message
            ?.replace(
                Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"),
                "[EMAIL]"
            )
            ?: "Unknown error"
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun mapFirebaseException(throwable: Throwable): DomainError {
        Timber.w("Firebase Auth error: ${sanitizeErrorMessage(throwable.message)}")

        if (throwable is FirebaseAuthException) {
            return when (throwable.errorCode) {
                // Validation errors
                "ERROR_INVALID_EMAIL",
                "ERROR_INVALID_CREDENTIAL" ->
                    DomainError.ValidationError(
                        message = "メールアドレスの形式が正しくありません",
                        field = "email"
                    )

                "ERROR_WEAK_PASSWORD" ->
                    DomainError.ValidationError(
                        message = "パスワードは6文字以上で入力してください",
                        field = "password"
                    )

                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    DomainError.ValidationError(
                        message = "このメールアドレスは既に使用されています",
                        field = "email"
                    )

                // Unauthorized errors
                "ERROR_USER_NOT_FOUND" ->
                    DomainError.UnauthorizedError(
                        message = "アカウントが見つかりません"
                    )

                "ERROR_WRONG_PASSWORD" ->
                    DomainError.UnauthorizedError(
                        message = "パスワードが正しくありません"
                    )

                "ERROR_USER_DISABLED" ->
                    DomainError.UnauthorizedError(
                        message = "このアカウントは無効化されています"
                    )

                "ERROR_REQUIRES_RECENT_LOGIN" ->
                    DomainError.UnauthorizedError(
                        message = "再認証が必要です。ログインし直してください"
                    )

                // Network errors
                "ERROR_NETWORK_REQUEST_FAILED" ->
                    DomainError.NetworkError(
                        message = "ネットワーク接続に失敗しました",
                        cause = throwable
                    )

                "ERROR_TOO_MANY_REQUESTS" ->
                    DomainError.NetworkError(
                        message = "リクエストが多すぎます。しばらく待ってから再試行してください",
                        cause = throwable
                    )

                else ->
                    DomainError.UnknownError(
                        message = throwable.message ?: "認証エラーが発生しました",
                        cause = throwable
                    )
            }
        }

        if (throwable is IllegalStateException) {
            return DomainError.UnauthorizedError(
                message = throwable.message ?: "ログインしていません"
            )
        }

        return DomainError.UnknownError(
            message = throwable.message ?: "予期しないエラーが発生しました",
            cause = throwable
        )
    }
}
