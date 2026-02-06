package com.carenote.app.data.mapper

import com.carenote.app.domain.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class UserMapperTest {

    private lateinit var mapper: UserMapper

    @Before
    fun setUp() {
        mapper = UserMapper()
    }

    private fun createMockFirebaseUser(
        uid: String = "test-uid-123",
        displayName: String? = "テストユーザー",
        email: String? = "test@example.com",
        isEmailVerified: Boolean = false,
        creationTimestamp: Long? = 1710500400000L // 2025-03-15T10:00:00 approx
    ): FirebaseUser {
        val metadata = if (creationTimestamp != null) {
            mockk<FirebaseUserMetadata> {
                every { this@mockk.creationTimestamp } returns creationTimestamp
            }
        } else {
            null
        }
        return mockk<FirebaseUser> {
            every { this@mockk.uid } returns uid
            every { this@mockk.displayName } returns displayName
            every { this@mockk.email } returns email
            every { this@mockk.isEmailVerified } returns isEmailVerified
            every { this@mockk.metadata } returns metadata
        }
    }

    @Test
    fun `toDomain maps FirebaseUser to User correctly`() {
        val timestamp = 1710500400000L
        val firebaseUser = createMockFirebaseUser(
            uid = "uid-abc",
            displayName = "山田太郎",
            email = "yamada@example.com",
            isEmailVerified = true,
            creationTimestamp = timestamp
        )

        val result = mapper.toDomain(firebaseUser)

        assertEquals("uid-abc", result.uid)
        assertEquals("山田太郎", result.name)
        assertEquals("yamada@example.com", result.email)
        assertTrue(result.isEmailVerified)
        assertFalse(result.isPremium)
        val expectedDateTime = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        assertEquals(expectedDateTime, result.createdAt)
    }

    @Test
    fun `toDomain handles null displayName`() {
        val firebaseUser = createMockFirebaseUser(displayName = null)

        val result = mapper.toDomain(firebaseUser)

        assertEquals("", result.name)
    }

    @Test
    fun `toDomain handles null email`() {
        val firebaseUser = createMockFirebaseUser(email = null)

        val result = mapper.toDomain(firebaseUser)

        assertEquals("", result.email)
    }

    @Test
    fun `toDomain handles null metadata`() {
        val firebaseUser = createMockFirebaseUser(creationTimestamp = null)

        val result = mapper.toDomain(firebaseUser)

        val now = LocalDateTime.now()
        val diff = ChronoUnit.SECONDS.between(result.createdAt, now)
        assertTrue("createdAt should be close to now", diff in -5..5)
    }

    @Test
    fun `toDomain maps isEmailVerified correctly`() {
        val verifiedUser = createMockFirebaseUser(isEmailVerified = true)
        val unverifiedUser = createMockFirebaseUser(isEmailVerified = false)

        val verifiedResult = mapper.toDomain(verifiedUser)
        val unverifiedResult = mapper.toDomain(unverifiedUser)

        assertTrue(verifiedResult.isEmailVerified)
        assertFalse(unverifiedResult.isEmailVerified)
    }
}
