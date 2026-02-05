package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class MedicationLogRemoteMapperTest {

    private lateinit var timestampConverter: FirestoreTimestampConverter
    private lateinit var mapper: MedicationLogRemoteMapper

    private val testDateTime = LocalDateTime.of(2025, 3, 15, 10, 0, 0)
    private val scheduledAt = LocalDateTime.of(2025, 3, 15, 8, 0, 0)
    private val recordedAt = LocalDateTime.of(2025, 3, 15, 8, 5, 0)

    @Before
    fun setUp() {
        timestampConverter = FirestoreTimestampConverter()
        mapper = MedicationLogRemoteMapper(timestampConverter)
    }

    // region toDomain

    @Test
    fun `toDomain maps all fields correctly`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "TAKEN",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt),
            "memo" to "服用完了"
        )

        val result = mapper.toDomain(data)

        assertEquals(1L, result.id)
        assertEquals(100L, result.medicationId)
        assertEquals(MedicationLogStatus.TAKEN, result.status)
        assertEquals(scheduledAt, result.scheduledAt)
        assertEquals(recordedAt, result.recordedAt)
        assertEquals("服用完了", result.memo)
    }

    @Test
    fun `toDomain uses default value for optional memo`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "SKIPPED",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt)
        )

        val result = mapper.toDomain(data)

        assertEquals("", result.memo)
    }

    @Test
    fun `toDomain maps SKIPPED status`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "SKIPPED",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt)
        )

        val result = mapper.toDomain(data)

        assertEquals(MedicationLogStatus.SKIPPED, result.status)
    }

    @Test
    fun `toDomain maps POSTPONED status`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "POSTPONED",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt)
        )

        val result = mapper.toDomain(data)

        assertEquals(MedicationLogStatus.POSTPONED, result.status)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when localId is missing`() {
        val data = mapOf(
            "medicationLocalId" to 100L,
            "status" to "TAKEN",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt)
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when medicationLocalId is missing`() {
        val data = mapOf(
            "localId" to 1L,
            "status" to "TAKEN",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt)
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when status is missing`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt)
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when status is invalid`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "INVALID_STATUS",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt)
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when scheduledAt is missing`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "TAKEN",
            "recordedAt" to toTimestamp(recordedAt)
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when recordedAt is missing`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "TAKEN",
            "scheduledAt" to toTimestamp(scheduledAt)
        )

        mapper.toDomain(data)
    }

    // endregion

    // region toRemote

    @Test
    fun `toRemote maps all fields correctly`() {
        val medicationLog = MedicationLog(
            id = 1L,
            medicationId = 100L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = scheduledAt,
            recordedAt = recordedAt,
            memo = "服用完了"
        )

        val result = mapper.toRemote(medicationLog, null)

        assertEquals(1L, result["localId"])
        assertEquals(100L, result["medicationLocalId"])
        assertEquals("TAKEN", result["status"])
        assertEquals("服用完了", result["memo"])
    }

    @Test
    fun `toRemote adds syncMetadata when provided`() {
        val medicationLog = MedicationLog(
            id = 1L,
            medicationId = 100L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = scheduledAt,
            recordedAt = recordedAt
        )
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = null
        )

        val result = mapper.toRemote(medicationLog, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertNull(result["deletedAt"])
    }

    @Test
    fun `toRemote with syncMetadata including deletedAt`() {
        val medicationLog = MedicationLog(
            id = 1L,
            medicationId = 100L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = scheduledAt,
            recordedAt = recordedAt
        )
        val deletedAt = LocalDateTime.of(2025, 3, 17, 10, 0)
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = deletedAt
        )

        val result = mapper.toRemote(medicationLog, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertTrue(result.containsKey("deletedAt"))
    }

    @Test
    fun `toRemote without syncMetadata does not add metadata fields`() {
        val medicationLog = MedicationLog(
            id = 1L,
            medicationId = 100L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = scheduledAt,
            recordedAt = recordedAt
        )

        val result = mapper.toRemote(medicationLog, null)

        assertTrue(!result.containsKey("syncedAt"))
        assertTrue(!result.containsKey("deletedAt"))
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
        val original = MedicationLog(
            id = 1L,
            medicationId = 100L,
            status = MedicationLogStatus.SKIPPED,
            scheduledAt = scheduledAt,
            recordedAt = recordedAt,
            memo = "体調不良のためスキップ",
            timing = MedicationTiming.MORNING
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.medicationId, roundtrip.medicationId)
        assertEquals(original.status, roundtrip.status)
        assertEquals(original.scheduledAt, roundtrip.scheduledAt)
        assertEquals(original.recordedAt, roundtrip.recordedAt)
        assertEquals(original.memo, roundtrip.memo)
        assertEquals(original.timing, roundtrip.timing)
    }

    // endregion

    // region toDomainList

    @Test
    fun `toDomainList maps list of data to domain list`() {
        val dataList = listOf(
            mapOf(
                "localId" to 1L,
                "medicationLocalId" to 100L,
                "status" to "TAKEN",
                "scheduledAt" to toTimestamp(scheduledAt),
                "recordedAt" to toTimestamp(recordedAt)
            ),
            mapOf(
                "localId" to 2L,
                "medicationLocalId" to 100L,
                "status" to "SKIPPED",
                "scheduledAt" to toTimestamp(scheduledAt),
                "recordedAt" to toTimestamp(recordedAt)
            )
        )

        val result = mapper.toDomainList(dataList)

        assertEquals(2, result.size)
        assertEquals(MedicationLogStatus.TAKEN, result[0].status)
        assertEquals(MedicationLogStatus.SKIPPED, result[1].status)
    }

    // endregion

    // region timing

    @Test
    fun `toDomain maps timing field`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "TAKEN",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt),
            "timing" to "MORNING"
        )

        val result = mapper.toDomain(data)

        assertEquals(MedicationTiming.MORNING, result.timing)
    }

    @Test
    fun `toDomain maps null timing when field is missing`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "TAKEN",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt)
        )

        val result = mapper.toDomain(data)

        assertNull(result.timing)
    }

    @Test
    fun `toDomain falls back to null for invalid timing`() {
        val data = mapOf(
            "localId" to 1L,
            "medicationLocalId" to 100L,
            "status" to "TAKEN",
            "scheduledAt" to toTimestamp(scheduledAt),
            "recordedAt" to toTimestamp(recordedAt),
            "timing" to "INVALID"
        )

        val result = mapper.toDomain(data)

        assertNull(result.timing)
    }

    @Test
    fun `toRemote includes timing when present`() {
        val medicationLog = MedicationLog(
            id = 1L,
            medicationId = 100L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = scheduledAt,
            recordedAt = recordedAt,
            timing = MedicationTiming.EVENING
        )

        val result = mapper.toRemote(medicationLog, null)

        assertEquals("EVENING", result["timing"])
    }

    @Test
    fun `toRemote includes null timing when absent`() {
        val medicationLog = MedicationLog(
            id = 1L,
            medicationId = 100L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = scheduledAt,
            recordedAt = recordedAt,
            timing = null
        )

        val result = mapper.toRemote(medicationLog, null)

        assertNull(result["timing"])
    }

    @Test
    fun `roundtrip preserves timing`() {
        val original = MedicationLog(
            id = 1L,
            medicationId = 100L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = scheduledAt,
            recordedAt = recordedAt,
            timing = MedicationTiming.NOON
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertEquals(original.timing, roundtrip.timing)
    }

    // endregion

    private fun toTimestamp(dateTime: LocalDateTime): Timestamp {
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }
}
