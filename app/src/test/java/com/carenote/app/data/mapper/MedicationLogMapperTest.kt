package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class MedicationLogMapperTest {

    private lateinit var mapper: MedicationLogMapper

    @Before
    fun setUp() {
        mapper = MedicationLogMapper()
    }

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = MedicationLogEntity(
            id = 1L,
            medicationId = 10L,
            status = "TAKEN",
            scheduledAt = "2025-03-15T08:00:00",
            recordedAt = "2025-03-15T08:05:00",
            memo = "メモ"
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals(10L, result.medicationId)
        assertEquals(MedicationLogStatus.TAKEN, result.status)
        assertEquals(LocalDateTime.of(2025, 3, 15, 8, 0), result.scheduledAt)
        assertEquals(LocalDateTime.of(2025, 3, 15, 8, 5), result.recordedAt)
        assertEquals("メモ", result.memo)
    }

    @Test
    fun `toEntity maps domain to entity`() {
        val domain = MedicationLog(
            id = 1L,
            medicationId = 10L,
            status = MedicationLogStatus.SKIPPED,
            scheduledAt = LocalDateTime.of(2025, 3, 15, 12, 0),
            recordedAt = LocalDateTime.of(2025, 3, 15, 12, 30),
            memo = "飲めなかった"
        )

        val result = mapper.toEntity(domain)

        assertEquals(1L, result.id)
        assertEquals(10L, result.medicationId)
        assertEquals("SKIPPED", result.status)
        assertEquals("2025-03-15T12:00:00", result.scheduledAt)
        assertEquals("2025-03-15T12:30:00", result.recordedAt)
        assertEquals("飲めなかった", result.memo)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = MedicationLogEntity(
            id = 1L,
            medicationId = 10L,
            status = "POSTPONED",
            scheduledAt = "2025-03-15T18:00:00",
            recordedAt = "2025-03-15T18:00:00",
            memo = ""
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.medicationId, roundtrip.medicationId)
        assertEquals(original.status, roundtrip.status)
        assertEquals(original.scheduledAt, roundtrip.scheduledAt)
        assertEquals(original.recordedAt, roundtrip.recordedAt)
        assertEquals(original.memo, roundtrip.memo)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            MedicationLogEntity(
                id = 1L, medicationId = 10L, status = "TAKEN",
                scheduledAt = "2025-03-15T08:00:00", recordedAt = "2025-03-15T08:05:00"
            ),
            MedicationLogEntity(
                id = 2L, medicationId = 10L, status = "SKIPPED",
                scheduledAt = "2025-03-15T12:00:00", recordedAt = "2025-03-15T12:00:00"
            )
        )

        val result = mapper.toDomainList(entities)

        assertEquals(2, result.size)
        assertEquals(MedicationLogStatus.TAKEN, result[0].status)
        assertEquals(MedicationLogStatus.SKIPPED, result[1].status)
    }

    @Test
    fun `toDomain maps TAKEN status`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "TAKEN",
            scheduledAt = "2025-03-15T08:00:00", recordedAt = "2025-03-15T08:00:00"
        )

        assertEquals(MedicationLogStatus.TAKEN, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain maps SKIPPED status`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "SKIPPED",
            scheduledAt = "2025-03-15T08:00:00", recordedAt = "2025-03-15T08:00:00"
        )

        assertEquals(MedicationLogStatus.SKIPPED, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain maps POSTPONED status`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "POSTPONED",
            scheduledAt = "2025-03-15T08:00:00", recordedAt = "2025-03-15T08:00:00"
        )

        assertEquals(MedicationLogStatus.POSTPONED, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain with empty memo`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "TAKEN",
            scheduledAt = "2025-03-15T08:00:00", recordedAt = "2025-03-15T08:00:00",
            memo = ""
        )

        assertEquals("", mapper.toDomain(entity).memo)
    }
}
