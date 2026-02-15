package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.MedicationEntity
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.testing.TestDataFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

class MedicationMapperTest {

    private lateinit var mapper: MedicationMapper

    @Before
    fun setUp() {
        mapper = MedicationMapper()
    }

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            dosage = "1錠",
            timings = "MORNING,NOON",
            times = "MORNING=08:00;NOON=12:00",
            reminderEnabled = true,
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW_STRING
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals("テスト薬", result.name)
        assertEquals("1錠", result.dosage)
        assertEquals(2, result.timings.size)
        assertTrue(result.timings.contains(MedicationTiming.MORNING))
        assertTrue(result.timings.contains(MedicationTiming.NOON))
    }

    @Test
    fun `toDomain maps empty timings`() {
        val entity = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            timings = "",
            times = "",
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW_STRING
        )

        val result = mapper.toDomain(entity)

        assertTrue(result.timings.isEmpty())
        assertTrue(result.times.isEmpty())
    }

    @Test
    fun `toDomain maps times correctly`() {
        val entity = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            timings = "MORNING",
            times = "MORNING=08:30",
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW_STRING
        )

        val result = mapper.toDomain(entity)

        assertEquals(LocalTime.of(8, 30), result.times[MedicationTiming.MORNING])
    }

    @Test
    fun `toEntity maps domain to entity`() {
        val domain = Medication(
            id = 1L,
            name = "テスト薬",
            dosage = "1錠",
            timings = listOf(MedicationTiming.MORNING, MedicationTiming.EVENING),
            times = mapOf(
                MedicationTiming.MORNING to LocalTime.of(8, 0),
                MedicationTiming.EVENING to LocalTime.of(18, 0)
            ),
            reminderEnabled = true,
            createdAt = TestDataFixtures.NOW,
            updatedAt = TestDataFixtures.NOW
        )

        val result = mapper.toEntity(domain)

        assertEquals(1L, result.id)
        assertEquals("テスト薬", result.name)
        assertEquals("1錠", result.dosage)
        assertTrue(result.timings.contains("MORNING"))
        assertTrue(result.timings.contains("EVENING"))
    }

    @Test
    fun `roundtrip entity to domain to entity preserves data`() {
        val original = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            dosage = "2錠",
            timings = "MORNING,NOON,EVENING",
            times = "MORNING=08:00;NOON=12:00;EVENING=18:00",
            reminderEnabled = false,
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW_STRING
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.name, roundtrip.name)
        assertEquals(original.dosage, roundtrip.dosage)
        assertEquals(original.reminderEnabled, roundtrip.reminderEnabled)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            MedicationEntity(
                id = 1L, name = "薬A", timings = "", times = "",
                createdAt = TestDataFixtures.NOW_STRING, updatedAt = TestDataFixtures.NOW_STRING
            ),
            MedicationEntity(
                id = 2L, name = "薬B", timings = "", times = "",
                createdAt = TestDataFixtures.NOW_STRING, updatedAt = TestDataFixtures.NOW_STRING
            )
        )

        val result = mapper.toDomainList(entities)

        assertEquals(2, result.size)
        assertEquals("薬A", result[0].name)
        assertEquals("薬B", result[1].name)
    }

    @Test
    fun `toDomain handles invalid timing gracefully`() {
        val entity = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            timings = "MORNING,INVALID",
            times = "MORNING=08:00",
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW_STRING
        )

        val result = mapper.toDomain(entity)

        assertEquals(1, result.timings.size)
        assertEquals(MedicationTiming.MORNING, result.timings[0])
    }

    @Test
    fun `toDomain maps stock fields correctly`() {
        val entity = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            timings = "MORNING",
            times = "MORNING=08:00",
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW_STRING,
            currentStock = 20,
            lowStockThreshold = 5
        )

        val result = mapper.toDomain(entity)

        assertEquals(20, result.currentStock)
        assertEquals(5, result.lowStockThreshold)
    }

    @Test
    fun `toEntity maps stock fields correctly`() {
        val domain = Medication(
            id = 1L,
            name = "テスト薬",
            timings = listOf(MedicationTiming.MORNING),
            times = mapOf(MedicationTiming.MORNING to LocalTime.of(8, 0)),
            createdAt = TestDataFixtures.NOW,
            updatedAt = TestDataFixtures.NOW,
            currentStock = 15,
            lowStockThreshold = 3
        )

        val result = mapper.toEntity(domain)

        assertEquals(15, result.currentStock)
        assertEquals(3, result.lowStockThreshold)
    }

    @Test
    fun `careRecipientId maps correctly in roundtrip`() {
        val entity = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            timings = "MORNING",
            times = "MORNING=08:00",
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW_STRING,
            careRecipientId = 42L
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    @Test
    fun `toDomain handles invalid time format gracefully`() {
        val entity = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            timings = "MORNING",
            times = "MORNING=invalid",
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW_STRING
        )

        val result = mapper.toDomain(entity)

        assertTrue(result.times.isEmpty())
    }
}
