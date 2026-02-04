package com.carenote.app.data.mapper.remote

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore Timestamp と Java Time API 間の変換ユーティリティ
 */
@Singleton
class FirestoreTimestampConverter @Inject constructor() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // region Timestamp <-> LocalDateTime

    /**
     * Firestore Timestamp を LocalDateTime に変換
     */
    fun toLocalDateTime(timestamp: Timestamp): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong()),
            ZoneId.systemDefault()
        )
    }

    /**
     * LocalDateTime を Firestore Timestamp に変換
     */
    fun toTimestamp(dateTime: LocalDateTime): Timestamp {
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }

    /**
     * Any? から LocalDateTime に変換（Firestore 読み取り用）
     *
     * @throws IllegalArgumentException 変換不可能な型の場合
     */
    fun toLocalDateTimeFromAny(value: Any?): LocalDateTime {
        return when (value) {
            is Timestamp -> toLocalDateTime(value)
            is Long -> LocalDateTime.ofInstant(
                Instant.ofEpochMilli(value),
                ZoneId.systemDefault()
            )
            else -> throw IllegalArgumentException(
                "Cannot convert ${value?.javaClass?.name} to LocalDateTime"
            )
        }
    }

    /**
     * Any? から LocalDateTime に変換（nullable 版）
     */
    fun toLocalDateTimeFromAnyOrNull(value: Any?): LocalDateTime? {
        if (value == null) return null
        return toLocalDateTimeFromAny(value)
    }

    // endregion

    // region String <-> LocalDate

    /**
     * String (YYYY-MM-DD) を LocalDate に変換
     */
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, dateFormatter)
    }

    /**
     * LocalDate を String (YYYY-MM-DD) に変換
     */
    fun toDateString(date: LocalDate): String {
        return date.format(dateFormatter)
    }

    /**
     * Any? から LocalDate に変換（Firestore 読み取り用）
     *
     * @throws IllegalArgumentException 変換不可能な型の場合
     */
    fun toLocalDateFromAny(value: Any?): LocalDate {
        return when (value) {
            is String -> toLocalDate(value)
            else -> throw IllegalArgumentException(
                "Cannot convert ${value?.javaClass?.name} to LocalDate"
            )
        }
    }

    /**
     * Any? から LocalDate に変換（nullable 版）
     */
    fun toLocalDateFromAnyOrNull(value: Any?): LocalDate? {
        if (value == null) return null
        return toLocalDateFromAny(value)
    }

    // endregion

    // region String <-> LocalTime

    /**
     * String (HH:mm) を LocalTime に変換
     */
    fun toLocalTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString, timeFormatter)
    }

    /**
     * LocalTime を String (HH:mm) に変換
     */
    fun toTimeString(time: LocalTime): String {
        return time.format(timeFormatter)
    }

    /**
     * Any? から LocalTime に変換（Firestore 読み取り用）
     *
     * @throws IllegalArgumentException 変換不可能な型の場合
     */
    fun toLocalTimeFromAny(value: Any?): LocalTime {
        return when (value) {
            is String -> toLocalTime(value)
            else -> throw IllegalArgumentException(
                "Cannot convert ${value?.javaClass?.name} to LocalTime"
            )
        }
    }

    /**
     * Any? から LocalTime に変換（nullable 版）
     */
    fun toLocalTimeFromAnyOrNull(value: Any?): LocalTime? {
        if (value == null) return null
        return toLocalTimeFromAny(value)
    }

    // endregion
}
