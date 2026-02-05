package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationTiming
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class MedicationRemoteMapperTest {

    private lateinit var timestampConverter: FirestoreTimestampConverter
    private lateinit var mapper: MedicationRemoteMapper

    private val testDateTime = LocalDateTime.of(2025, 3, 15, 10, 0, 0)

    @Before
    fun setUp() {
        timestampConverter = FirestoreTimestampConverter()
        mapper = MedicationRemoteMapper(timestampConverter)
    }

    // region toDomain

    @Test
    fun `toDomain maps all fields correctly`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "name" to "テスト薬",
            "dosage" to "1錠",
            "timings" to listOf("MORNING", "NOON"),
            "times" to mapOf("MORNING" to "08:00", "NOON" to "12:00"),
            "reminderEnabled" to true,
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(1L, result.id)
        assertEquals("テスト薬", result.name)
        assertEquals("1錠", result.dosage)
        assertEquals(2, result.timings.size)
        assertTrue(result.timings.contains(MedicationTiming.MORNING))
        assertTrue(result.timings.contains(MedicationTiming.NOON))
        assertEquals(LocalTime.of(8, 0), result.times[MedicationTiming.MORNING])
        assertEquals(LocalTime.of(12, 0), result.times[MedicationTiming.NOON])
        assertTrue(result.reminderEnabled)
        assertEquals(testDateTime, result.createdAt)
        assertEquals(testDateTime, result.updatedAt)
    }

    @Test
    fun `toDomain uses default values for optional fields`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "name" to "テスト薬",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals("", result.dosage)
        assertTrue(result.timings.isEmpty())
        assertTrue(result.times.isEmpty())
        assertTrue(result.reminderEnabled)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when localId is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "name" to "テスト薬",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when name is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when createdAt is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "name" to "テスト薬",
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when updatedAt is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "name" to "テスト薬",
            "createdAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test
    fun `toDomain ignores invalid timing values`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "name" to "テスト薬",
            "timings" to listOf("MORNING", "INVALID", "EVENING"),
            "times" to emptyMap<String, String>(),
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(2, result.timings.size)
        assertTrue(result.timings.contains(MedicationTiming.MORNING))
        assertTrue(result.timings.contains(MedicationTiming.EVENING))
    }

    @Test
    fun `toDomain handles empty timings list`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "name" to "テスト薬",
            "timings" to emptyList<String>(),
            "times" to emptyMap<String, String>(),
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertTrue(result.timings.isEmpty())
    }

    // endregion

    // region toRemote

    @Test
    fun `toRemote maps all fields correctly`() {
        val medication = Medication(
            id = 1L,
            name = "テスト薬",
            dosage = "1錠",
            timings = listOf(MedicationTiming.MORNING, MedicationTiming.NOON),
            times = mapOf(
                MedicationTiming.MORNING to LocalTime.of(8, 0),
                MedicationTiming.NOON to LocalTime.of(12, 0)
            ),
            reminderEnabled = true,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(medication, null)

        assertEquals(1L, result["localId"])
        assertEquals("テスト薬", result["name"])
        assertEquals("1錠", result["dosage"])
        @Suppress("UNCHECKED_CAST")
        val timings = result["timings"] as List<String>
        assertTrue(timings.contains("MORNING"))
        assertTrue(timings.contains("NOON"))
        @Suppress("UNCHECKED_CAST")
        val times = result["times"] as Map<String, String>
        assertEquals("08:00", times["MORNING"])
        assertEquals("12:00", times["NOON"])
        assertEquals(true, result["reminderEnabled"])
    }

    @Test
    fun `toRemote adds syncMetadata when provided`() {
        val medication = Medication(
            id = 1L,
            name = "テスト薬",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val syncedAt = LocalDateTime.of(2025, 3, 16, 10, 0)
        val deletedAt = LocalDateTime.of(2025, 3, 17, 10, 0)
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = syncedAt,
            deletedAt = deletedAt
        )

        val result = mapper.toRemote(medication, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertTrue(result.containsKey("deletedAt"))
    }

    @Test
    fun `toRemote without syncMetadata does not add metadata fields`() {
        val medication = Medication(
            id = 1L,
            name = "テスト薬",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(medication, null)

        assertTrue(!result.containsKey("syncedAt"))
        assertTrue(!result.containsKey("deletedAt"))
    }

    @Test
    fun `toRemote with syncMetadata but null deletedAt`() {
        val medication = Medication(
            id = 1L,
            name = "テスト薬",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = null
        )

        val result = mapper.toRemote(medication, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertNull(result["deletedAt"])
    }

    // endregion

    // region extractSyncMetadata

    @Test
    fun `extractSyncMetadata extracts all fields correctly`() {
        val syncedAt = LocalDateTime.of(2025, 3, 16, 10, 0)
        val deletedAt = LocalDateTime.of(2025, 3, 17, 10, 0)
        val data = mapOf(
            "localId" to 1L,
            "syncedAt" to toTimestamp(syncedAt),
            "deletedAt" to toTimestamp(deletedAt)
        )

        val result = mapper.extractSyncMetadata(data)

        assertEquals(1L, result.localId)
        assertEquals(syncedAt, result.syncedAt)
        assertEquals(deletedAt, result.deletedAt)
    }

    @Test
    fun `extractSyncMetadata with null deletedAt`() {
        val syncedAt = LocalDateTime.of(2025, 3, 16, 10, 0)
        val data = mapOf(
            "localId" to 1L,
            "syncedAt" to toTimestamp(syncedAt),
            "deletedAt" to null
        )

        val result = mapper.extractSyncMetadata(data)

        assertEquals(1L, result.localId)
        assertEquals(syncedAt, result.syncedAt)
        assertNull(result.deletedAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `extractSyncMetadata throws when localId is missing`() {
        val data = mapOf(
            "syncedAt" to toTimestamp(testDateTime)
        )

        mapper.extractSyncMetadata(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `extractSyncMetadata throws when syncedAt is missing`() {
        val data = mapOf(
            "localId" to 1L
        )

        mapper.extractSyncMetadata(data)
    }

    // endregion

    // region roundtrip

    @Test
    fun `roundtrip domain to remote to domain preserves data`() {
        val original = Medication(
            id = 1L,
            name = "テスト薬",
            dosage = "2錠",
            timings = listOf(MedicationTiming.MORNING, MedicationTiming.NOON, MedicationTiming.EVENING),
            times = mapOf(
                MedicationTiming.MORNING to LocalTime.of(8, 0),
                MedicationTiming.NOON to LocalTime.of(12, 0),
                MedicationTiming.EVENING to LocalTime.of(18, 0)
            ),
            reminderEnabled = false,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.name, roundtrip.name)
        assertEquals(original.dosage, roundtrip.dosage)
        assertEquals(original.timings, roundtrip.timings)
        assertEquals(original.times, roundtrip.times)
        assertEquals(original.reminderEnabled, roundtrip.reminderEnabled)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    // endregion

    // region toDomainList

    @Test
    fun `toDomainList maps list of data to domain list`() {
        val timestamp = toTimestamp(testDateTime)
        val dataList = listOf(
            mapOf(
                "localId" to 1L,
                "name" to "薬A",
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            ),
            mapOf(
                "localId" to 2L,
                "name" to "薬B",
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            )
        )

        val result = mapper.toDomainList(dataList)

        assertEquals(2, result.size)
        assertEquals("薬A", result[0].name)
        assertEquals("薬B", result[1].name)
    }

    // endregion

    private fun toTimestamp(dateTime: LocalDateTime): Timestamp {
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }
}
