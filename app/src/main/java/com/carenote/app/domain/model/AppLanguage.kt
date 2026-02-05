package com.carenote.app.domain.model

/**
 * アプリ内言語設定
 */
enum class AppLanguage {
    SYSTEM,
    JAPANESE,
    ENGLISH;

    fun toLocaleTag(): String = when (this) {
        SYSTEM -> ""
        JAPANESE -> "ja"
        ENGLISH -> "en"
    }
}
