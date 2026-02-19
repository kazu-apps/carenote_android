package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.data.local.ActiveCareRecipientPreferences
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.fakes.FakeCareRecipientRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ActiveCareRecipientProviderImplTest {

    private lateinit var careRecipientRepository: FakeCareRecipientRepository
    private lateinit var preferences: ActiveCareRecipientPreferences
    private lateinit var provider: ActiveCareRecipientProviderImpl
    private val preferencesFlow = MutableStateFlow(0L)

    @Before
    fun setUp() {
        careRecipientRepository = FakeCareRecipientRepository()
        preferences = mockk(relaxed = true)
        every { preferences.activeCareRecipientId } returns preferencesFlow
        provider = ActiveCareRecipientProviderImpl(
            careRecipientRepository,
            preferences
        )
    }

    @Test
    fun `activeCareRecipientId emits 0L when no care recipient`() = runTest {
        provider.activeCareRecipientId.test {
            assertEquals(0L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `activeCareRecipientId emits correct id when care recipient exists`() =
        runTest {
            preferencesFlow.value = 42L
            careRecipientRepository.setCareRecipients(
                listOf(CareRecipient(id = 42L, name = "テスト太郎"))
            )

            provider.activeCareRecipientId.test {
                assertEquals(42L, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `activeCareRecipientId updates when care recipient changes`() = runTest {
        provider.activeCareRecipientId.test {
            assertEquals(0L, awaitItem())

            // Set recipients first to avoid intermediate (10L, emptyList) -> 0L
            careRecipientRepository.setCareRecipients(
                listOf(CareRecipient(id = 10L, name = "テスト太郎"))
            )
            preferencesFlow.value = 10L
            assertEquals(10L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActiveCareRecipientId returns first value`() = runTest {
        preferencesFlow.value = 99L
        careRecipientRepository.setCareRecipients(
            listOf(CareRecipient(id = 99L, name = "テスト花子"))
        )

        val result = provider.getActiveCareRecipientId()
        assertEquals(99L, result)
    }

    @Test
    fun `getActiveFirestoreId returns null when no care recipient`() = runTest {
        val result = provider.getActiveFirestoreId()
        assertNull(result)
    }

    @Test
    fun `getActiveFirestoreId returns firestoreId when exists`() = runTest {
        preferencesFlow.value = 1L
        careRecipientRepository.setCareRecipients(
            listOf(
                CareRecipient(
                    id = 1L,
                    name = "テスト太郎",
                    firestoreId = "fs-abc-123"
                )
            )
        )
        careRecipientRepository.setCareRecipient(
            CareRecipient(
                id = 1L,
                name = "テスト太郎",
                firestoreId = "fs-abc-123"
            )
        )

        val result = provider.getActiveFirestoreId()
        assertEquals("fs-abc-123", result)
    }

    @Test
    fun `getActiveFirestoreId returns null when firestoreId not set`() = runTest {
        preferencesFlow.value = 1L
        careRecipientRepository.setCareRecipients(
            listOf(CareRecipient(id = 1L, name = "テスト太郎"))
        )
        careRecipientRepository.setCareRecipient(
            CareRecipient(id = 1L, name = "テスト太郎")
        )

        val result = provider.getActiveFirestoreId()
        assertNull(result)
    }

    @Test
    fun `saved ID selects correct recipient on startup`() = runTest {
        preferencesFlow.value = 2L
        careRecipientRepository.setCareRecipients(
            listOf(
                CareRecipient(id = 1L, name = "一郎"),
                CareRecipient(id = 2L, name = "二郎"),
                CareRecipient(id = 3L, name = "三郎")
            )
        )

        provider.activeCareRecipientId.test {
            assertEquals(2L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setActiveCareRecipientId delegates to preferences`() = runTest {
        provider.setActiveCareRecipientId(5L)

        verify { preferences.setActiveCareRecipientId(5L) }
    }

    @Test
    fun `invalid saved ID falls back to first recipient`() = runTest {
        preferencesFlow.value = 999L
        careRecipientRepository.setCareRecipients(
            listOf(
                CareRecipient(id = 1L, name = "一郎"),
                CareRecipient(id = 2L, name = "二郎")
            )
        )

        provider.activeCareRecipientId.test {
            assertEquals(1L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty recipients list with saved ID emits 0L`() = runTest {
        preferencesFlow.value = 5L

        provider.activeCareRecipientId.test {
            assertEquals(0L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
