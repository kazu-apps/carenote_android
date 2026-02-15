package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.testing.TestDataFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicationLogMapperTest {

    private lateinit var mapper: MedicationLogMapper

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

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
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(5).format(fmt),
            memo = "メモ"
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals(10L, result.medicationId)
        assertEquals(MedicationLogStatus.TAKEN, result.status)
        assertEquals(TestDataFixtures.NOW.withHour(8).withMinute(0), result.scheduledAt)
        assertEquals(TestDataFixtures.NOW.withHour(8).withMinute(5), result.recordedAt)
        assertEquals("メモ", result.memo)
    }

    @Test
    fun `toEntity maps domain to entity`() {
        val domain = MedicationLog(
            id = 1L,
            medicationId = 10L,
            status = MedicationLogStatus.SKIPPED,
            scheduledAt = TestDataFixtures.NOW.withHour(12).withMinute(0),
            recordedAt = TestDataFixtures.NOW.withHour(12).withMinute(30),
            memo = "飲めなかった"
        )

        val result = mapper.toEntity(domain)

        assertEquals(1L, result.id)
        assertEquals(10L, result.medicationId)
        assertEquals("SKIPPED", result.status)
        assertEquals(TestDataFixtures.NOW.withHour(12).withMinute(0).format(fmt), result.scheduledAt)
        assertEquals(TestDataFixtures.NOW.withHour(12).withMinute(30).format(fmt), result.recordedAt)
        assertEquals("飲めなかった", result.memo)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = MedicationLogEntity(
            id = 1L,
            medicationId = 10L,
            status = "POSTPONED",
            scheduledAt = TestDataFixtures.NOW.withHour(18).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(18).withMinute(0).format(fmt),
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
                scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
                recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(5).format(fmt)
            ),
            MedicationLogEntity(
                id = 2L, medicationId = 10L, status = "SKIPPED",
                scheduledAt = TestDataFixtures.NOW.withHour(12).withMinute(0).format(fmt),
                recordedAt = TestDataFixtures.NOW.withHour(12).withMinute(0).format(fmt)
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
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        assertEquals(MedicationLogStatus.TAKEN, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain maps SKIPPED status`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "SKIPPED",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        assertEquals(MedicationLogStatus.SKIPPED, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain maps POSTPONED status`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "POSTPONED",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        assertEquals(MedicationLogStatus.POSTPONED, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain throws IllegalArgumentException for invalid status`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "INVALID_STATUS",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            mapper.toDomain(entity)
        }

        assertTrue(exception.message!!.contains("INVALID_STATUS"))
        assertTrue(exception.message!!.contains("TAKEN"))
    }

    @Test
    fun `toDomain with empty memo`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "TAKEN",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            memo = ""
        )

        assertEquals("", mapper.toDomain(entity).memo)
    }

    @Test
    fun `toDomain maps null timing to null`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "TAKEN",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            timing = null
        )

        assertNull(mapper.toDomain(entity).timing)
    }

    @Test
    fun `toDomain maps MORNING timing`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "TAKEN",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            timing = "MORNING"
        )

        assertEquals(MedicationTiming.MORNING, mapper.toDomain(entity).timing)
    }

    @Test
    fun `toEntity maps timing to name string`() {
        val domain = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0),
            timing = MedicationTiming.EVENING
        )

        assertEquals("EVENING", mapper.toEntity(domain).timing)
    }

    @Test
    fun `toEntity maps null timing to null`() {
        val domain = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0),
            timing = null
        )

        assertNull(mapper.toEntity(domain).timing)
    }

    @Test
    fun `roundtrip preserves timing`() {
        val original = MedicationLogEntity(
            id = 1L, medicationId = 10L, status = "TAKEN",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(5).format(fmt),
            timing = "NOON"
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.timing, roundtrip.timing)
        assertEquals(MedicationTiming.NOON, domain.timing)
    }

    @Test
    fun `careRecipientId maps correctly in roundtrip`() {
        val entity = MedicationLogEntity(
            id = 1L,
            medicationId = 10L,
            status = "TAKEN",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(5).format(fmt),
            careRecipientId = 42L
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    @Test
    fun `toDomain falls back to null for invalid timing`() {
        val entity = MedicationLogEntity(
            id = 1L, medicationId = 1L, status = "TAKEN",
            scheduledAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            recordedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            timing = "INVALID_TIMING"
        )

        assertNull(mapper.toDomain(entity).timing)
    }
}
