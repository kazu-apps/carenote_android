package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.testing.TestDataFixtures
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class HealthRecordRemoteMapperTest {

    private lateinit var timestampConverter: FirestoreTimestampConverter
    private lateinit var mapper: HealthRecordRemoteMapper

    private val testDateTime = TestDataFixtures.NOW
    private val recordedAt = TestDataFixtures.NOW.minusHours(2)

    @Before
    fun setUp() {
        timestampConverter = FirestoreTimestampConverter()
        mapper = HealthRecordRemoteMapper(timestampConverter)
    }

    // region toDomain

    @Test
    fun `toDomain maps all fields correctly`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "temperature" to 36.5,
            "bloodPressureHigh" to 120,
            "bloodPressureLow" to 80,
            "pulse" to 72,
            "weight" to 65.0,
            "meal" to "FULL",
            "excretion" to "NORMAL",
            "conditionNote" to "体調良好",
            "createdBy" to "user1",
            "recordedAt" to toTimestamp(recordedAt),
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(1L, result.id)
        assertEquals(36.5, result.temperature!!, 0.01)
        assertEquals(120, result.bloodPressureHigh)
        assertEquals(80, result.bloodPressureLow)
        assertEquals(72, result.pulse)
        assertEquals(65.0, result.weight!!, 0.01)
        assertEquals(MealAmount.FULL, result.meal)
        assertEquals(ExcretionType.NORMAL, result.excretion)
        assertEquals("体調良好", result.conditionNote)
        assertEquals("user1", result.createdBy)
        assertEquals(recordedAt, result.recordedAt)
        assertEquals(testDateTime, result.createdAt)
        assertEquals(testDateTime, result.updatedAt)
    }

    @Test
    fun `toDomain handles nullable fields as null`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "recordedAt" to toTimestamp(recordedAt),
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertNull(result.temperature)
        assertNull(result.bloodPressureHigh)
        assertNull(result.bloodPressureLow)
        assertNull(result.pulse)
        assertNull(result.weight)
        assertNull(result.meal)
        assertNull(result.excretion)
        assertEquals("", result.conditionNote)
    }

    @Test
    fun `toDomain maps all MealAmount values`() {
        val timestamp = toTimestamp(testDateTime)

        MealAmount.entries.forEach { mealAmount ->
            val data = mapOf(
                "localId" to 1L,
                "meal" to mealAmount.name,
                "recordedAt" to toTimestamp(recordedAt),
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            )

            val result = mapper.toDomain(data)

            assertEquals(mealAmount, result.meal)
        }
    }

    @Test
    fun `toDomain maps all ExcretionType values`() {
        val timestamp = toTimestamp(testDateTime)

        ExcretionType.entries.forEach { excretionType ->
            val data = mapOf(
                "localId" to 1L,
                "excretion" to excretionType.name,
                "recordedAt" to toTimestamp(recordedAt),
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            )

            val result = mapper.toDomain(data)

            assertEquals(excretionType, result.excretion)
        }
    }

    @Test
    fun `toDomain returns null for invalid MealAmount`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "meal" to "INVALID_MEAL",
            "recordedAt" to toTimestamp(recordedAt),
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertNull(result.meal)
    }

    @Test
    fun `toDomain returns null for invalid ExcretionType`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "excretion" to "INVALID_EXCRETION",
            "recordedAt" to toTimestamp(recordedAt),
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertNull(result.excretion)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when localId is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "recordedAt" to toTimestamp(recordedAt),
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when recordedAt is missing`() {
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
            "recordedAt" to toTimestamp(recordedAt),
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when updatedAt is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "recordedAt" to toTimestamp(recordedAt),
            "createdAt" to timestamp
        )

        mapper.toDomain(data)
    }

    // endregion

    // region toRemote

    @Test
    fun `toRemote maps all fields correctly`() {
        val healthRecord = HealthRecord(
            id = 1L,
            temperature = 36.5,
            bloodPressureHigh = 120,
            bloodPressureLow = 80,
            pulse = 72,
            weight = 65.0,
            meal = MealAmount.FULL,
            excretion = ExcretionType.NORMAL,
            conditionNote = "体調良好",
            createdBy = "user1",
            recordedAt = recordedAt,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(healthRecord, null)

        assertEquals(1L, result["localId"])
        assertEquals(36.5, result["temperature"])
        assertEquals(120, result["bloodPressureHigh"])
        assertEquals(80, result["bloodPressureLow"])
        assertEquals(72, result["pulse"])
        assertEquals(65.0, result["weight"])
        assertEquals("FULL", result["meal"])
        assertEquals("NORMAL", result["excretion"])
        assertEquals("体調良好", result["conditionNote"])
        assertEquals("user1", result["createdBy"])
    }

    @Test
    fun `toRemote handles nullable fields as null`() {
        val healthRecord = HealthRecord(
            id = 1L,
            temperature = null,
            bloodPressureHigh = null,
            bloodPressureLow = null,
            pulse = null,
            weight = null,
            meal = null,
            excretion = null,
            recordedAt = recordedAt,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(healthRecord, null)

        assertNull(result["temperature"])
        assertNull(result["bloodPressureHigh"])
        assertNull(result["bloodPressureLow"])
        assertNull(result["pulse"])
        assertNull(result["weight"])
        assertNull(result["meal"])
        assertNull(result["excretion"])
    }

    @Test
    fun `toRemote adds syncMetadata when provided`() {
        val healthRecord = HealthRecord(
            id = 1L,
            recordedAt = recordedAt,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = null
        )

        val result = mapper.toRemote(healthRecord, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertNull(result["deletedAt"])
    }

    @Test
    fun `toRemote with syncMetadata including deletedAt`() {
        val healthRecord = HealthRecord(
            id = 1L,
            recordedAt = recordedAt,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val deletedAt = TestDataFixtures.NOW.plusDays(2)
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = deletedAt
        )

        val result = mapper.toRemote(healthRecord, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertTrue(result.containsKey("deletedAt"))
    }

    @Test
    fun `toRemote without syncMetadata does not add metadata fields`() {
        val healthRecord = HealthRecord(
            id = 1L,
            recordedAt = recordedAt,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(healthRecord, null)

        assertTrue(!result.containsKey("syncedAt"))
        assertTrue(!result.containsKey("deletedAt"))
    }

    // endregion

    // region extractSyncMetadata

    @Test
    fun `extractSyncMetadata extracts all fields correctly`() {
        val syncedAt = TestDataFixtures.NOW.plusDays(1)
        val deletedAt = TestDataFixtures.NOW.plusDays(2)
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
        val syncedAt = TestDataFixtures.NOW.plusDays(1)
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
        val original = HealthRecord(
            id = 1L,
            temperature = 36.5,
            bloodPressureHigh = 120,
            bloodPressureLow = 80,
            pulse = 72,
            weight = 65.0,
            meal = MealAmount.MOSTLY,
            excretion = ExcretionType.SOFT,
            conditionNote = "少し疲れ気味",
            recordedAt = recordedAt,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.temperature!!, roundtrip.temperature!!, 0.01)
        assertEquals(original.bloodPressureHigh, roundtrip.bloodPressureHigh)
        assertEquals(original.bloodPressureLow, roundtrip.bloodPressureLow)
        assertEquals(original.pulse, roundtrip.pulse)
        assertEquals(original.weight!!, roundtrip.weight!!, 0.01)
        assertEquals(original.meal, roundtrip.meal)
        assertEquals(original.excretion, roundtrip.excretion)
        assertEquals(original.conditionNote, roundtrip.conditionNote)
        assertEquals(original.recordedAt, roundtrip.recordedAt)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    @Test
    fun `roundtrip with null optional fields preserves data`() {
        val original = HealthRecord(
            id = 1L,
            temperature = null,
            bloodPressureHigh = null,
            bloodPressureLow = null,
            pulse = null,
            weight = null,
            meal = null,
            excretion = null,
            conditionNote = "",
            recordedAt = recordedAt,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertEquals(original.id, roundtrip.id)
        assertNull(roundtrip.temperature)
        assertNull(roundtrip.bloodPressureHigh)
        assertNull(roundtrip.bloodPressureLow)
        assertNull(roundtrip.pulse)
        assertNull(roundtrip.weight)
        assertNull(roundtrip.meal)
        assertNull(roundtrip.excretion)
        assertEquals(original.conditionNote, roundtrip.conditionNote)
    }

    // endregion

    // region toDomainList

    @Test
    fun `toDomainList maps list of data to domain list`() {
        val timestamp = toTimestamp(testDateTime)
        val dataList = listOf(
            mapOf(
                "localId" to 1L,
                "temperature" to 36.5,
                "recordedAt" to toTimestamp(recordedAt),
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            ),
            mapOf(
                "localId" to 2L,
                "temperature" to 37.0,
                "recordedAt" to toTimestamp(recordedAt),
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            )
        )

        val result = mapper.toDomainList(dataList)

        assertEquals(2, result.size)
        assertEquals(36.5, result[0].temperature!!, 0.01)
        assertEquals(37.0, result[1].temperature!!, 0.01)
    }

    // endregion

    private fun toTimestamp(dateTime: LocalDateTime): Timestamp {
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }
}
