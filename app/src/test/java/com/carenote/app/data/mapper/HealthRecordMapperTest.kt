package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.HealthRecordEntity
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class HealthRecordMapperTest {

    private lateinit var mapper: HealthRecordMapper

    @Before
    fun setUp() {
        mapper = HealthRecordMapper()
    }

    @Test
    fun `toDomain maps entity with all fields to domain model`() {
        val entity = createEntity(
            id = 1L,
            temperature = 36.5,
            bloodPressureHigh = 120,
            bloodPressureLow = 80,
            pulse = 72,
            weight = 65.0,
            meal = "FULL",
            excretion = "NORMAL",
            conditionNote = "体調良好"
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals(36.5, result.temperature)
        assertEquals(120, result.bloodPressureHigh)
        assertEquals(80, result.bloodPressureLow)
        assertEquals(72, result.pulse)
        assertEquals(65.0, result.weight)
        assertEquals(MealAmount.FULL, result.meal)
        assertEquals(ExcretionType.NORMAL, result.excretion)
        assertEquals("体調良好", result.conditionNote)
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 0), result.recordedAt)
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 0), result.createdAt)
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 0), result.updatedAt)
    }

    @Test
    fun `toDomain maps entity with null fields correctly`() {
        val entity = createEntity(
            temperature = null,
            bloodPressureHigh = null,
            bloodPressureLow = null,
            pulse = null,
            weight = null,
            meal = null,
            excretion = null
        )

        val result = mapper.toDomain(entity)

        assertNull(result.temperature)
        assertNull(result.bloodPressureHigh)
        assertNull(result.bloodPressureLow)
        assertNull(result.pulse)
        assertNull(result.weight)
        assertNull(result.meal)
        assertNull(result.excretion)
    }

    @Test
    fun `toDomain maps all MealAmount values correctly`() {
        val meals = listOf("FULL", "MOSTLY", "HALF", "LITTLE", "NONE")
        val expected = listOf(
            MealAmount.FULL, MealAmount.MOSTLY, MealAmount.HALF,
            MealAmount.LITTLE, MealAmount.NONE
        )

        meals.forEachIndexed { index, mealString ->
            val entity = createEntity(meal = mealString)
            val result = mapper.toDomain(entity)
            assertEquals(expected[index], result.meal)
        }
    }

    @Test
    fun `toDomain maps all ExcretionType values correctly`() {
        val types = listOf("NORMAL", "SOFT", "HARD", "DIARRHEA", "NONE")
        val expected = listOf(
            ExcretionType.NORMAL, ExcretionType.SOFT, ExcretionType.HARD,
            ExcretionType.DIARRHEA, ExcretionType.NONE
        )

        types.forEachIndexed { index, typeString ->
            val entity = createEntity(excretion = typeString)
            val result = mapper.toDomain(entity)
            assertEquals(expected[index], result.excretion)
        }
    }

    @Test
    fun `toDomain maps unknown meal value to null`() {
        val entity = createEntity(meal = "UNKNOWN_MEAL")

        val result = mapper.toDomain(entity)

        assertNull(result.meal)
    }

    @Test
    fun `toDomain maps empty meal value to null`() {
        val entity = createEntity(meal = "")

        val result = mapper.toDomain(entity)

        assertNull(result.meal)
    }

    @Test
    fun `toDomain maps unknown excretion value to null`() {
        val entity = createEntity(excretion = "UNKNOWN_TYPE")

        val result = mapper.toDomain(entity)

        assertNull(result.excretion)
    }

    @Test
    fun `toDomain maps empty excretion value to null`() {
        val entity = createEntity(excretion = "")

        val result = mapper.toDomain(entity)

        assertNull(result.excretion)
    }

    @Test
    fun `toEntity maps domain model with all fields to entity`() {
        val domain = createHealthRecord(
            id = 2L,
            temperature = 37.2,
            bloodPressureHigh = 140,
            bloodPressureLow = 90,
            pulse = 88,
            weight = 70.5,
            meal = MealAmount.HALF,
            excretion = ExcretionType.SOFT,
            conditionNote = "少し熱っぽい"
        )

        val result = mapper.toEntity(domain)

        assertEquals(2L, result.id)
        assertEquals(37.2, result.temperature)
        assertEquals(140, result.bloodPressureHigh)
        assertEquals(90, result.bloodPressureLow)
        assertEquals(88, result.pulse)
        assertEquals(70.5, result.weight)
        assertEquals("HALF", result.meal)
        assertEquals("SOFT", result.excretion)
        assertEquals("少し熱っぽい", result.conditionNote)
        assertEquals("2025-03-15T10:00:00", result.recordedAt)
        assertEquals("2025-03-15T10:00:00", result.createdAt)
        assertEquals("2025-03-15T10:00:00", result.updatedAt)
    }

    @Test
    fun `toEntity maps domain model with null fields correctly`() {
        val domain = createHealthRecord(
            temperature = null,
            bloodPressureHigh = null,
            bloodPressureLow = null,
            pulse = null,
            weight = null,
            meal = null,
            excretion = null
        )

        val result = mapper.toEntity(domain)

        assertNull(result.temperature)
        assertNull(result.bloodPressureHigh)
        assertNull(result.bloodPressureLow)
        assertNull(result.pulse)
        assertNull(result.weight)
        assertNull(result.meal)
        assertNull(result.excretion)
    }

    @Test
    fun `roundtrip entity to domain to entity preserves data`() {
        val original = createEntity(
            id = 3L,
            temperature = 36.8,
            bloodPressureHigh = 130,
            bloodPressureLow = 85,
            pulse = 75,
            weight = 62.3,
            meal = "MOSTLY",
            excretion = "HARD",
            conditionNote = "やや便秘気味"
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.temperature, roundtrip.temperature)
        assertEquals(original.bloodPressureHigh, roundtrip.bloodPressureHigh)
        assertEquals(original.bloodPressureLow, roundtrip.bloodPressureLow)
        assertEquals(original.pulse, roundtrip.pulse)
        assertEquals(original.weight, roundtrip.weight)
        assertEquals(original.meal, roundtrip.meal)
        assertEquals(original.excretion, roundtrip.excretion)
        assertEquals(original.conditionNote, roundtrip.conditionNote)
        assertEquals(original.recordedAt, roundtrip.recordedAt)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    @Test
    fun `roundtrip with null fields preserves nulls`() {
        val original = createEntity(
            temperature = null,
            bloodPressureHigh = null,
            bloodPressureLow = null,
            pulse = null,
            weight = null,
            meal = null,
            excretion = null
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertNull(roundtrip.temperature)
        assertNull(roundtrip.bloodPressureHigh)
        assertNull(roundtrip.bloodPressureLow)
        assertNull(roundtrip.pulse)
        assertNull(roundtrip.weight)
        assertNull(roundtrip.meal)
        assertNull(roundtrip.excretion)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            createEntity(id = 1L, conditionNote = "記録A"),
            createEntity(id = 2L, conditionNote = "記録B"),
            createEntity(id = 3L, conditionNote = "記録C")
        )

        val result = mapper.toDomainList(entities)

        assertEquals(3, result.size)
        assertEquals("記録A", result[0].conditionNote)
        assertEquals("記録B", result[1].conditionNote)
        assertEquals("記録C", result[2].conditionNote)
    }

    @Test
    fun `toDomainList maps empty list`() {
        val result = mapper.toDomainList(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `toEntityList maps list of domain models`() {
        val domains = listOf(
            createHealthRecord(id = 1L, conditionNote = "記録A"),
            createHealthRecord(id = 2L, conditionNote = "記録B")
        )

        val result = mapper.toEntityList(domains)

        assertEquals(2, result.size)
        assertEquals("記録A", result[0].conditionNote)
        assertEquals("記録B", result[1].conditionNote)
    }

    private fun createEntity(
        id: Long = 1L,
        temperature: Double? = null,
        bloodPressureHigh: Int? = null,
        bloodPressureLow: Int? = null,
        pulse: Int? = null,
        weight: Double? = null,
        meal: String? = null,
        excretion: String? = null,
        conditionNote: String = "",
        recordedAt: String = "2025-03-15T10:00:00",
        createdAt: String = "2025-03-15T10:00:00",
        updatedAt: String = "2025-03-15T10:00:00"
    ): HealthRecordEntity = HealthRecordEntity(
        id = id,
        temperature = temperature,
        bloodPressureHigh = bloodPressureHigh,
        bloodPressureLow = bloodPressureLow,
        pulse = pulse,
        weight = weight,
        meal = meal,
        excretion = excretion,
        conditionNote = conditionNote,
        recordedAt = recordedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun createHealthRecord(
        id: Long = 1L,
        temperature: Double? = null,
        bloodPressureHigh: Int? = null,
        bloodPressureLow: Int? = null,
        pulse: Int? = null,
        weight: Double? = null,
        meal: MealAmount? = null,
        excretion: ExcretionType? = null,
        conditionNote: String = "",
        recordedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ): HealthRecord = HealthRecord(
        id = id,
        temperature = temperature,
        bloodPressureHigh = bloodPressureHigh,
        bloodPressureLow = bloodPressureLow,
        pulse = pulse,
        weight = weight,
        meal = meal,
        excretion = excretion,
        conditionNote = conditionNote,
        recordedAt = recordedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
