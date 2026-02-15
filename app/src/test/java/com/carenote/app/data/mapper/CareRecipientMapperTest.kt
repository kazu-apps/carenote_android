package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.CareRecipientEntity
import com.carenote.app.domain.model.Gender
import com.carenote.app.testing.TestDataFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CareRecipientMapperTest {

    private lateinit var mapper: CareRecipientMapper

    @Before
    fun setUp() {
        mapper = CareRecipientMapper()
    }

    private fun createEntity(
        id: Long = 1L,
        name: String = "テスト太郎",
        birthDate: String? = "1940-05-15",
        gender: String = "MALE",
        nickname: String = "太郎",
        careLevel: String = "要介護2",
        medicalHistory: String = "高血圧",
        allergies: String = "なし",
        memo: String = "テストメモ",
        createdAt: String = TestDataFixtures.NOW_STRING,
        updatedAt: String = TestDataFixtures.NOW_STRING,
        firestoreId: String? = null
    ) = CareRecipientEntity(
        id = id,
        name = name,
        birthDate = birthDate,
        gender = gender,
        nickname = nickname,
        careLevel = careLevel,
        medicalHistory = medicalHistory,
        allergies = allergies,
        memo = memo,
        createdAt = createdAt,
        updatedAt = updatedAt,
        firestoreId = firestoreId
    )

    @Test
    fun `toDomain maps all fields correctly`() {
        val entity = createEntity(firestoreId = "fs-123")
        val domain = mapper.toDomain(entity)

        assertEquals(1L, domain.id)
        assertEquals("テスト太郎", domain.name)
        assertEquals("1940-05-15", domain.birthDate.toString())
        assertEquals(Gender.MALE, domain.gender)
        assertEquals("太郎", domain.nickname)
        assertEquals("要介護2", domain.careLevel)
        assertEquals("高血圧", domain.medicalHistory)
        assertEquals("なし", domain.allergies)
        assertEquals("テストメモ", domain.memo)
        assertEquals(TestDataFixtures.NOW, domain.createdAt)
        assertEquals(TestDataFixtures.NOW, domain.updatedAt)
        assertEquals("fs-123", domain.firestoreId)
    }

    @Test
    fun `toDomain maps null birthDate`() {
        val entity = createEntity(birthDate = null)
        val domain = mapper.toDomain(entity)

        assertNull(domain.birthDate)
    }

    @Test
    fun `toDomain maps null firestoreId`() {
        val entity = createEntity(firestoreId = null)
        val domain = mapper.toDomain(entity)

        assertNull(domain.firestoreId)
    }

    @Test
    fun `toDomain maps non-null firestoreId`() {
        val entity = createEntity(firestoreId = "abc-def-123")
        val domain = mapper.toDomain(entity)

        assertEquals("abc-def-123", domain.firestoreId)
    }

    @Test
    fun `toDomain falls back to UNSPECIFIED for invalid gender`() {
        val entity = createEntity(gender = "INVALID_GENDER")
        val domain = mapper.toDomain(entity)

        assertEquals(Gender.UNSPECIFIED, domain.gender)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val entity = createEntity(firestoreId = "fs-456")
        val domain = mapper.toDomain(entity)
        val result = mapper.toEntity(domain)

        assertEquals(1L, result.id)
        assertEquals("テスト太郎", result.name)
        assertEquals("1940-05-15", result.birthDate)
        assertEquals("MALE", result.gender)
        assertEquals("太郎", result.nickname)
        assertEquals("要介護2", result.careLevel)
        assertEquals("高血圧", result.medicalHistory)
        assertEquals("なし", result.allergies)
        assertEquals("テストメモ", result.memo)
        assertEquals(TestDataFixtures.NOW_STRING, result.createdAt)
        assertEquals(TestDataFixtures.NOW_STRING, result.updatedAt)
        assertEquals("fs-456", result.firestoreId)
    }

    @Test
    fun `toEntity maps null birthDate`() {
        val entity = createEntity(birthDate = null)
        val domain = mapper.toDomain(entity)
        val result = mapper.toEntity(domain)

        assertNull(result.birthDate)
    }

    @Test
    fun `toEntity maps null firestoreId`() {
        val entity = createEntity(firestoreId = null)
        val domain = mapper.toDomain(entity)
        val result = mapper.toEntity(domain)

        assertNull(result.firestoreId)
    }

    @Test
    fun `toEntity maps non-null firestoreId`() {
        val entity = createEntity(firestoreId = "xyz-789")
        val domain = mapper.toDomain(entity)
        val result = mapper.toEntity(domain)

        assertEquals("xyz-789", result.firestoreId)
    }

    @Test
    fun `roundtrip preserves all fields`() {
        val original = createEntity(
            id = 42L,
            name = "山田花子",
            birthDate = "1935-12-25",
            gender = "FEMALE",
            nickname = "花子",
            careLevel = "要介護3",
            medicalHistory = "糖尿病、骨粗鬆症",
            allergies = "ペニシリン",
            memo = "週3回デイサービス利用",
            firestoreId = "roundtrip-test-id"
        )

        val domain = mapper.toDomain(original)
        val result = mapper.toEntity(domain)

        assertEquals(original.id, result.id)
        assertEquals(original.name, result.name)
        assertEquals(original.birthDate, result.birthDate)
        assertEquals(original.gender, result.gender)
        assertEquals(original.nickname, result.nickname)
        assertEquals(original.careLevel, result.careLevel)
        assertEquals(original.medicalHistory, result.medicalHistory)
        assertEquals(original.allergies, result.allergies)
        assertEquals(original.memo, result.memo)
        assertEquals(original.createdAt, result.createdAt)
        assertEquals(original.updatedAt, result.updatedAt)
        assertEquals(original.firestoreId, result.firestoreId)
    }
}
