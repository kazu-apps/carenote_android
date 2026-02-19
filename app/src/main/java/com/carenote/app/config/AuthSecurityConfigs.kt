package com.carenote.app.config

/**
 * 認証・セキュリティ関連の設定値
 */
object AuthSecurityConfigs {

    /**
     * 認証関連の設定値
     */
    object Auth {
        /** メールアドレスの最大文字数 */
        const val EMAIL_MAX_LENGTH = 255

        /** パスワードの最小文字数 */
        const val PASSWORD_MIN_LENGTH = 8

        /** パスワードの最大文字数 */
        const val PASSWORD_MAX_LENGTH = 100

        /** 表示名の最大文字数 */
        const val DISPLAY_NAME_MAX_LENGTH = 50
    }

    /**
     * セッション管理の設定値
     */
    object Session {
        /** デフォルトのセッションタイムアウト（分） */
        const val DEFAULT_TIMEOUT_MINUTES = 5

        /** セッションタイムアウトの最小値（分） */
        const val MIN_TIMEOUT_MINUTES = 1

        /** セッションタイムアウトの最大値（分） */
        const val MAX_TIMEOUT_MINUTES = 60
    }

    /**
     * セキュリティ関連の設定値
     */
    object Security {
        /** PBKDF2 イテレーション回数 */
        const val PBKDF2_ITERATIONS = 100_000

        /** PBKDF2 鍵長（ビット） */
        const val PBKDF2_KEY_LENGTH_BITS = 256

        /** PBKDF2 ソルト長（バイト） */
        const val PBKDF2_SALT_LENGTH = 32

        /** Root 検出時の短縮セッションタイムアウト（ミリ秒） */
        const val ROOT_SESSION_TIMEOUT_MS = 60_000L
    }

    /**
     * 生体認証関連の設定値
     */
    object Biometric {
        /** バックグラウンドタイムアウト（ミリ秒）— この時間を超えて復帰したら再認証を要求 */
        const val BACKGROUND_TIMEOUT_MS = 30_000L
    }
}
