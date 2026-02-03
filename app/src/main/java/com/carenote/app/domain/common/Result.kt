package com.carenote.app.domain.common

/**
 * 操作の成功または失敗を表す型安全な結果ラッパー
 *
 * @param T 成功時の値の型
 * @param E エラーの型（通常は DomainError）
 *
 * Usage:
 * ```
 * fun findMedication(id: Long): Result<Medication, DomainError> {
 *     val medication = medicationDao.findById(id)
 *     return if (medication != null) {
 *         Result.Success(medication)
 *     } else {
 *         Result.Failure(DomainError.NotFoundError("Medication not found: $id"))
 *     }
 * }
 *
 * // 使用例
 * findMedication(123)
 *     .map { it.name }
 *     .onSuccess { println("Medication name: $it") }
 *     .onFailure { println("Error: ${it.message}") }
 * ```
 */
sealed class Result<out T, out E> {

    /**
     * 操作が成功した場合の結果
     */
    data class Success<out T>(val value: T) : Result<T, Nothing>()

    /**
     * 操作が失敗した場合の結果
     */
    data class Failure<out E>(val error: E) : Result<Nothing, E>()

    /**
     * 成功かどうかを返す
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 失敗かどうかを返す
     */
    val isFailure: Boolean
        get() = this is Failure

    /**
     * 成功時の値を返す。失敗時は null を返す。
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    /**
     * 成功時の値を返す。失敗時はデフォルト値を返す。
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default
    }

    /**
     * 失敗時のエラーを返す。成功時は null を返す。
     */
    fun errorOrNull(): E? = when (this) {
        is Success -> null
        is Failure -> error
    }

    /**
     * 成功時の値を変換する。失敗時は何もしない。
     */
    inline fun <R> map(transform: (T) -> R): Result<R, E> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    /**
     * 成功時に別の Result を返す操作をチェインする。
     * 連続した操作で、途中で失敗した場合はそこで終了する。
     */
    inline fun <R> flatMap(transform: (T) -> Result<R, @UnsafeVariance E>): Result<R, E> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    /**
     * 成功と失敗の両方のケースを処理し、単一の値を返す。
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (E) -> R
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }

    /**
     * 成功時にアクションを実行し、自身を返す（チェイン可能）。
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T, E> {
        if (this is Success) {
            action(value)
        }
        return this
    }

    /**
     * 失敗時にアクションを実行し、自身を返す（チェイン可能）。
     */
    inline fun onFailure(action: (E) -> Unit): Result<T, E> {
        if (this is Failure) {
            action(error)
        }
        return this
    }

    /**
     * 失敗時のエラーを変換する。成功時は何もしない。
     */
    inline fun <F> mapError(transform: (E) -> F): Result<T, F> = when (this) {
        is Success -> this
        is Failure -> Failure(transform(error))
    }

    /**
     * 失敗時にリカバリー値を提供して成功に変換する。
     */
    inline fun recover(transform: (E) -> @UnsafeVariance T): Result<T, E> = when (this) {
        is Success -> this
        is Failure -> Success(transform(error))
    }

    companion object {
        /**
         * 例外をキャッチして Result に変換する
         */
        inline fun <T> catching(
            errorTransform: (Throwable) -> DomainError = {
                DomainError.UnknownError(it.message ?: "Unknown error", it)
            },
            block: () -> T
        ): Result<T, DomainError> {
            return try {
                Success(block())
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Failure(errorTransform(e))
            }
        }

        /**
         * suspend 関数用の例外キャッチ
         */
        suspend inline fun <T> catchingSuspend(
            crossinline errorTransform: (Throwable) -> DomainError = {
                DomainError.UnknownError(it.message ?: "Unknown error", it)
            },
            crossinline block: suspend () -> T
        ): Result<T, DomainError> {
            return try {
                Success(block())
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Failure(errorTransform(e))
            }
        }
    }
}

/**
 * Result<T, DomainError> の拡張関数
 * 成功時の値を返す。失敗時は例外をスローする。
 */
fun <T> Result<T, DomainError>.getOrThrow(): T = when (this) {
    is Result.Success -> value
    is Result.Failure -> throw DomainException(error)
}

/**
 * DomainError を Exception にラップしたクラス
 */
class DomainException(val domainError: DomainError) : Exception(domainError.message, domainError.cause)
