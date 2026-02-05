package com.carenote.app.data.mapper.remote

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class FirestoreTimestampConverterTest {

    private lateinit var converter: FirestoreTimestampConverter

    @Before
    fun setUp() {
        converter = FirestoreTimestampConverter()
    }

    // region Timestamp <-> LocalDateTime

    @Test
    fun `toLocalDateTime converts Timestamp to LocalDateTime`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 10, 30, 0, 0)
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        val timestamp = Timestamp(instant.epochSecond, instant.nano)

        val result = converter.toLocalDateTime(timestamp)

        assertEquals(dateTime, result)
    }

    @Test
    fun `toTimestamp converts LocalDateTime to Timestamp`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 10, 30, 0, 0)

        val result = converter.toTimestamp(dateTime)

        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        assertEquals(instant.epochSecond, result.seconds)
        assertEquals(instant.nano, result.nanoseconds)
    }

    @Test
    fun `roundtrip LocalDateTime to Timestamp preserves data`() {
        val original = LocalDateTime.of(2025, 3, 15, 10, 30, 45, 123000000)

        val timestamp = converter.toTimestamp(original)
        val roundtrip = converter.toLocalDateTime(timestamp)

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toLocalDateTimeFromAny converts Timestamp`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 10, 30, 0, 0)
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        val timestamp = Timestamp(instant.epochSecond, instant.nano)

        val result = converter.toLocalDateTimeFromAny(timestamp)

        assertEquals(dateTime, result)
    }

    @Test
    fun `toLocalDateTimeFromAny converts Long epoch millis`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 10, 30, 0, 0)
        val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val result = converter.toLocalDateTimeFromAny(millis)

        assertEquals(dateTime, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toLocalDateTimeFromAny throws on invalid type`() {
        converter.toLocalDateTimeFromAny("invalid string")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toLocalDateTimeFromAny throws on null`() {
        converter.toLocalDateTimeFromAny(null)
    }

    @Test
    fun `toLocalDateTimeFromAnyOrNull returns null for null input`() {
        val result = converter.toLocalDateTimeFromAnyOrNull(null)

        assertNull(result)
    }

    @Test
    fun `toLocalDateTimeFromAnyOrNull converts valid Timestamp`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 10, 30, 0, 0)
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        val timestamp = Timestamp(instant.epochSecond, instant.nano)

        val result = converter.toLocalDateTimeFromAnyOrNull(timestamp)

        assertEquals(dateTime, result)
    }

    // endregion

    // region String <-> LocalDate

    @Test
    fun `toLocalDate parses ISO date string`() {
        val dateString = "2025-03-15"

        val result = converter.toLocalDate(dateString)

        assertEquals(LocalDate.of(2025, 3, 15), result)
    }

    @Test
    fun `toDateString formats LocalDate to ISO string`() {
        val date = LocalDate.of(2025, 3, 15)

        val result = converter.toDateString(date)

        assertEquals("2025-03-15", result)
    }

    @Test
    fun `roundtrip LocalDate to String preserves data`() {
        val original = LocalDate.of(2025, 12, 31)

        val dateString = converter.toDateString(original)
        val roundtrip = converter.toLocalDate(dateString)

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toLocalDateFromAny converts String`() {
        val result = converter.toLocalDateFromAny("2025-03-15")

        assertEquals(LocalDate.of(2025, 3, 15), result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toLocalDateFromAny throws on invalid type`() {
        converter.toLocalDateFromAny(12345L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toLocalDateFromAny throws on null`() {
        converter.toLocalDateFromAny(null)
    }

    @Test
    fun `toLocalDateFromAnyOrNull returns null for null input`() {
        val result = converter.toLocalDateFromAnyOrNull(null)

        assertNull(result)
    }

    @Test
    fun `toLocalDateFromAnyOrNull converts valid String`() {
        val result = converter.toLocalDateFromAnyOrNull("2025-03-15")

        assertEquals(LocalDate.of(2025, 3, 15), result)
    }

    // endregion

    // region String <-> LocalTime

    @Test
    fun `toLocalTime parses time string`() {
        val timeString = "08:30"

        val result = converter.toLocalTime(timeString)

        assertEquals(LocalTime.of(8, 30), result)
    }

    @Test
    fun `toTimeString formats LocalTime`() {
        val time = LocalTime.of(8, 30)

        val result = converter.toTimeString(time)

        assertEquals("08:30", result)
    }

    @Test
    fun `roundtrip LocalTime to String preserves data`() {
        val original = LocalTime.of(14, 45)

        val timeString = converter.toTimeString(original)
        val roundtrip = converter.toLocalTime(timeString)

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toLocalTimeFromAny converts String`() {
        val result = converter.toLocalTimeFromAny("18:00")

        assertEquals(LocalTime.of(18, 0), result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toLocalTimeFromAny throws on invalid type`() {
        converter.toLocalTimeFromAny(12345L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toLocalTimeFromAny throws on null`() {
        converter.toLocalTimeFromAny(null)
    }

    @Test
    fun `toLocalTimeFromAnyOrNull returns null for null input`() {
        val result = converter.toLocalTimeFromAnyOrNull(null)

        assertNull(result)
    }

    @Test
    fun `toLocalTimeFromAnyOrNull converts valid String`() {
        val result = converter.toLocalTimeFromAnyOrNull("08:30")

        assertEquals(LocalTime.of(8, 30), result)
    }

    // endregion
}
