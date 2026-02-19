package com.carenote.app.data.util

/**
 * Exception メッセージから PII を除去し、型名 + エラーコードのみを出力するユーティリティ。
 * Exception.message にユーザーメール、ドキュメント ID 等が含まれる可能性があるため、
 * ログ出力時は必ず本ユーティリティを使用する。
 */
object ExceptionMasker {
    fun mask(e: Exception): String {
        val typeName = e::class.simpleName ?: "Unknown"
        val code = when (e) {
            is com.google.firebase.firestore.FirebaseFirestoreException -> "code=${e.code}"
            is com.google.firebase.auth.FirebaseAuthException -> "code=${e.errorCode}"
            is com.google.firebase.functions.FirebaseFunctionsException -> "code=${e.code}"
            else -> ""
        }
        return if (code.isNotEmpty()) "$typeName($code)" else typeName
    }
}
