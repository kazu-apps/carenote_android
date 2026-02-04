package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** 現在の認証状態を観察 */
    val currentUser: Flow<User?>

    /** 現在ログイン中のユーザーを取得（同期的） */
    fun getCurrentUser(): User?

    /** メールアドレスとパスワードでサインアップ */
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<User, DomainError>

    /** メールアドレスとパスワードでサインイン */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<User, DomainError>

    /** サインアウト */
    suspend fun signOut(): Result<Unit, DomainError>

    /** パスワードリセットメール送信 */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit, DomainError>

    /** メールアドレス確認メール再送信 */
    suspend fun sendEmailVerification(): Result<Unit, DomainError>

    /** 現在のユーザーを再認証（センシティブ操作前） */
    suspend fun reauthenticate(password: String): Result<Unit, DomainError>

    /** パスワード変更 */
    suspend fun updatePassword(newPassword: String): Result<Unit, DomainError>

    /** アカウント削除 */
    suspend fun deleteAccount(): Result<Unit, DomainError>
}
