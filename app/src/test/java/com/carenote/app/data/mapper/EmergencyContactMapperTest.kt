package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.EmergencyContactEntity
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.model.RelationshipType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class EmergencyContactMapperTest {

    private lateinit var mapper: EmergencyContactMapper

    @Before
    fun setup() {
        mapper = EmergencyContactMapper()
    }

    @Test
    fun `toDomain maps entity to domain model correctly`() {
        val entity = EmergencyContactEntity(
            id = 1,
            name = "田中太郎",
            phoneNumber = "090-1234-5678",
            relationship = "FAMILY",
            memo = "父",
            createdAt = "2026-01-01T10:00:00",
            updatedAt = "2026-01-01T10:00:00"
        )

        val domain = mapper.toDomain(entity)

        assertEquals(1L, domain.id)
        assertEquals("田中太郎", domain.name)
        assertEquals("090-1234-5678", domain.phoneNumber)
        assertEquals(RelationshipType.FAMILY, domain.relationship)
        assertEquals("父", domain.memo)
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0, 0), domain.createdAt)
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0, 0), domain.updatedAt)
    }

    @Test
    fun `toEntity maps domain model to entity correctly`() {
        val domain = EmergencyContact(
            id = 2,
            name = "山田花子",
            phoneNumber = "080-9876-5432",
            relationship = RelationshipType.DOCTOR,
            memo = "かかりつけ医",
            createdAt = LocalDateTime.of(2026, 2, 1, 9, 0, 0),
            updatedAt = LocalDateTime.of(2026, 2, 1, 9, 0, 0)
        )

        val entity = mapper.toEntity(domain)

        assertEquals(2L, entity.id)
        assertEquals("山田花子", entity.name)
        assertEquals("080-9876-5432", entity.phoneNumber)
        assertEquals("DOCTOR", entity.relationship)
        assertEquals("かかりつけ医", entity.memo)
        assertEquals("2026-02-01T09:00:00", entity.createdAt)
        assertEquals("2026-02-01T09:00:00", entity.updatedAt)
    }

    @Test
    fun `toDomain maps all relationship types correctly`() {
        RelationshipType.entries.forEach { type ->
            val entity = createEntity(relationship = type.name)
            val domain = mapper.toDomain(entity)
            assertEquals(type, domain.relationship)
        }
    }

    @Test
    fun `toDomain falls back to OTHER for unknown relationship type`() {
        val entity = createEntity(relationship = "UNKNOWN_TYPE")
        val domain = mapper.toDomain(entity)
        assertEquals(RelationshipType.OTHER, domain.relationship)
    }

    @Test
    fun `toDomain falls back to OTHER for empty relationship`() {
        val entity = createEntity(relationship = "")
        val domain = mapper.toDomain(entity)
        assertEquals(RelationshipType.OTHER, domain.relationship)
    }

    @Test
    fun `toDomainList maps list of entities correctly`() {
        val entities = listOf(
            createEntity(id = 1, name = "A"),
            createEntity(id = 2, name = "B"),
            createEntity(id = 3, name = "C")
        )

        val domainList = mapper.toDomainList(entities)

        assertEquals(3, domainList.size)
        assertEquals("A", domainList[0].name)
        assertEquals("B", domainList[1].name)
        assertEquals("C", domainList[2].name)
    }

    @Test
    fun `toDomainList returns empty list for empty input`() {
        val result = mapper.toDomainList(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun `roundtrip domain to entity and back preserves data`() {
        val original = EmergencyContact(
            id = 5,
            name = "テスト太郎",
            phoneNumber = "03-1234-5678",
            relationship = RelationshipType.HOSPITAL,
            memo = "A病院 外来",
            createdAt = LocalDateTime.of(2026, 3, 15, 14, 30, 0),
            updatedAt = LocalDateTime.of(2026, 3, 15, 14, 30, 0)
        )

        val roundtripped = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original, roundtripped)
    }

    @Test
    fun `careRecipientId maps correctly in roundtrip`() {
        val entity = EmergencyContactEntity(
            id = 1,
            name = "テスト太郎",
            phoneNumber = "090-1234-5678",
            relationship = "FAMILY",
            memo = "",
            createdAt = "2026-01-01T10:00:00",
            updatedAt = "2026-01-01T10:00:00",
            careRecipientId = 42L
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    private fun createEntity(
        id: Long = 1,
        name: String = "テスト",
        relationship: String = "FAMILY"
    ): EmergencyContactEntity {
        return EmergencyContactEntity(
            id = id,
            name = name,
            phoneNumber = "090-0000-0000",
            relationship = relationship,
            memo = "",
            createdAt = "2026-01-01T10:00:00",
            updatedAt = "2026-01-01T10:00:00"
        )
    }
}
