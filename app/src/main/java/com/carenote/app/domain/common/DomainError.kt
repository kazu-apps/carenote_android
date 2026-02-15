package com.carenote.app.domain.common

/**
 * ドメイン層で発生するエラーを表す sealed class
 *
 * すべてのビジネスロジックエラーはこの型を継承し、
 * when 式での網羅的な処理を保証する。
 */
sealed class DomainError {
    abstract val message: String
    open val cause: Throwable? = null

    /**
     * データベース操作に関するエラー
     * Room での insert/update/delete/query 失敗時に使用
     */
    data class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * リソースが見つからない場合のエラー
     * 指定されたIDのエンティティが存在しない場合などに使用
     */
    data class NotFoundError(
        override val message: String
    ) : DomainError()

    /**
     * 入力値のバリデーションエラー
     * ユーザー入力やドメインルール違反時に使用
     */
    data class ValidationError(
        override val message: String,
        val field: String? = null
    ) : DomainError()

    /**
     * ネットワーク関連のエラー
     * API呼び出しやクラウド同期の失敗時に使用
     */
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * 認証・認可エラー
     * セッション切れやアクセス権限不足時に使用
     */
    data class UnauthorizedError(
        override val message: String
    ) : DomainError()

    /**
     * 予期しないエラー
     * 分類できないエラーや想定外の例外時に使用
     */
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * セキュリティポリシー違反エラー
     * Root 検出時の機密操作ブロック等に使用
     */
    data class SecurityError(
        override val message: String
    ) : DomainError()
}
