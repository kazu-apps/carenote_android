package com.carenote.app.ui.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * ロケール対応の日付/時刻フォーマットユーティリティ
 *
 * 日本語ロケールと英語ロケールでの表示形式を統一管理する。
 */
object DateTimeFormatters {

    private val DATE_FORMAT_JP = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val DATE_FORMAT_EN = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
    private val DATE_SHORT_FORMAT = DateTimeFormatter.ofPattern("M/d")
    private val YEAR_MONTH_JP = DateTimeFormatter.ofPattern("yyyy年M月")
    private val YEAR_MONTH_EN = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

    /**
     * 日付をフォーマットする
     * JP: 2025/03/15, EN: 03/15/2025
     */
    fun formatDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        val formatter = if (isJapanese(locale)) DATE_FORMAT_JP else DATE_FORMAT_EN
        return date.format(formatter)
    }

    /**
     * 時刻をフォーマットする（24時間制）
     * 08:30
     */
    fun formatTime(time: LocalTime): String {
        return time.format(TIME_FORMAT)
    }

    /**
     * 日時をフォーマットする
     * JP: 2025/03/15 14:30, EN: 03/15/2025 14:30
     */
    fun formatDateTime(dateTime: LocalDateTime, locale: Locale = Locale.getDefault()): String {
        val dateStr = formatDate(dateTime.toLocalDate(), locale)
        val timeStr = formatTime(dateTime.toLocalTime())
        return "$dateStr $timeStr"
    }

    /**
     * 短い日付フォーマット（月/日のみ）
     * 3/5
     *
     * 現在の `M/d` フォーマットはロケール非依存だが、他の関数との API 統一性のため
     * [locale] パラメータを保持している。
     */
    fun formatDateShort(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        return date.format(DATE_SHORT_FORMAT)
    }

    /**
     * 相対日付を返す（今日/昨日/日付）
     */
    fun formatRelativeDate(
        date: LocalDate,
        todayLabel: String,
        yesterdayLabel: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val today = LocalDate.now()
        return when (date) {
            today -> todayLabel
            today.minusDays(1) -> yesterdayLabel
            else -> formatDate(date, locale)
        }
    }

    /**
     * 年月をフォーマットする
     * JP: 2025年12月, EN: December 2025
     */
    fun formatYearMonth(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        val formatter = if (isJapanese(locale)) YEAR_MONTH_JP else YEAR_MONTH_EN
        return date.format(formatter)
    }

    /**
     * 曜日名を返す
     */
    fun formatDayOfWeek(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        return date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    }

    private fun isJapanese(locale: Locale): Boolean {
        return locale.language == Locale.JAPANESE.language
    }
}
