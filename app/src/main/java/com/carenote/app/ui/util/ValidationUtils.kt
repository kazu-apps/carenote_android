package com.carenote.app.ui.util

/**
 * 入力バリデーション関数群
 *
 * フォーム入力の検証に使用する。
 * 各関数は true（有効）/false（無効）を返すか、
 * エラーメッセージ文字列（null は有効）を返す。
 */
object ValidationUtils {

    /**
     * 空白でないことを検証する
     */
    fun isNotBlank(value: String): Boolean {
        return value.isNotBlank()
    }

    /**
     * 文字数が上限以内であることを検証する
     */
    fun isWithinLength(value: String, maxLength: Int): Boolean {
        return value.length <= maxLength
    }

    /**
     * 有効な数値であることを検証する
     */
    fun isValidNumber(value: String): Boolean {
        if (value.isEmpty()) return false
        return value.toDoubleOrNull() != null
    }

    /**
     * 数値が範囲内であることを検証する
     */
    fun isInRange(value: Double, min: Double, max: Double): Boolean {
        return value in min..max
    }

    /**
     * 必須入力のバリデーション
     * @return エラーメッセージ（有効な場合は null）
     */
    fun validateRequired(value: String, fieldName: String): String? {
        return if (value.isBlank()) {
            "${fieldName}は必須です"
        } else {
            null
        }
    }

    /**
     * 最大文字数のバリデーション
     * @return エラーメッセージ（有効な場合は null）
     */
    fun validateMaxLength(value: String, maxLength: Int, fieldName: String): String? {
        return if (value.length > maxLength) {
            "${fieldName}は${maxLength}文字以内で入力してください"
        } else {
            null
        }
    }

    /**
     * 数値範囲のバリデーション
     * @return エラーメッセージ（有効な場合は null）
     */
    fun validateNumberRange(
        value: String,
        min: Double,
        max: Double,
        fieldName: String
    ): String? {
        val number = value.toDoubleOrNull()
            ?: return "${fieldName}は数値で入力してください"

        return if (!isInRange(number, min, max)) {
            "${fieldName}は${min}〜${max}の範囲で入力してください"
        } else {
            null
        }
    }
}
