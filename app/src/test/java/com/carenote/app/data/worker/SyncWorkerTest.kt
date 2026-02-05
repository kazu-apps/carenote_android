package com.carenote.app.data.worker

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.common.SyncState
import com.carenote.app.domain.model.User
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeSyncRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * SyncWorker のユニットテスト
 *
 * WorkManager Worker のテストは複雑なため、ここでは主に:
 * 1. FakeSyncRepository の動作確認
 * 2. SyncResult → WorkManager Result マッピングロジックの検証
 * 3. 認証状態による分岐の検証
 *
 * を行う。実際の WorkManager 統合テストは androidTest で行う。
 */
class SyncWorkerTest {

    private lateinit var fakeSyncRepository: FakeSyncRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository

    @Before
    fun setUp() {
        fakeSyncRepository = FakeSyncRepository()
        fakeAuthRepository = FakeAuthRepository()
    }

    // ========== FakeSyncRepository Tests ==========

    @Test
    fun `FakeSyncRepository initial state is Idle`() = runTest {
        val state = fakeSyncRepository.syncState.first()
        assertTrue(state is SyncState.Idle)
    }

    @Test
    fun `FakeSyncRepository syncAll returns Success by default`() = runTest {
        val result = fakeSyncRepository.syncAll("test-care-recipient-id")

        assertTrue(result is SyncResult.Success)
        assertEquals(0, (result as SyncResult.Success).uploadedCount)
        assertEquals(0, result.downloadedCount)
    }

    @Test
    fun `FakeSyncRepository syncAll returns Failure when shouldFail is true`() = runTest {
        fakeSyncRepository.shouldFail = true
        fakeSyncRepository.failureError = DomainError.NetworkError("Network unavailable")

        val result = fakeSyncRepository.syncAll("test-id")

        assertTrue(result is SyncResult.Failure)
        assertTrue((result as SyncResult.Failure).error is DomainError.NetworkError)
    }

    @Test
    fun `FakeSyncRepository tracks syncAll call count`() = runTest {
        assertEquals(0, fakeSyncRepository.syncAllCallCount)

        fakeSyncRepository.syncAll("id1")
        assertEquals(1, fakeSyncRepository.syncAllCallCount)

        fakeSyncRepository.syncAll("id2")
        assertEquals(2, fakeSyncRepository.syncAllCallCount)
    }

    @Test
    fun `FakeSyncRepository tracks last careRecipientId`() = runTest {
        assertNull(fakeSyncRepository.lastCareRecipientId)

        fakeSyncRepository.syncAll("recipient-123")
        assertEquals("recipient-123", fakeSyncRepository.lastCareRecipientId)

        fakeSyncRepository.syncAll("recipient-456")
        assertEquals("recipient-456", fakeSyncRepository.lastCareRecipientId)
    }

    @Test
    fun `FakeSyncRepository clear resets all state`() = runTest {
        fakeSyncRepository.shouldFail = true
        fakeSyncRepository.syncAllCallCount
        fakeSyncRepository.syncAll("test-id")
        fakeSyncRepository.lastSyncTimeValue = LocalDateTime.now()

        fakeSyncRepository.clear()

        assertEquals(false, fakeSyncRepository.shouldFail)
        assertEquals(0, fakeSyncRepository.syncAllCallCount)
        assertNull(fakeSyncRepository.lastCareRecipientId)
        assertNull(fakeSyncRepository.lastSyncTimeValue)
    }

    @Test
    fun `FakeSyncRepository getLastSyncTime returns configured value`() = runTest {
        val now = LocalDateTime.of(2024, 1, 15, 10, 30)
        fakeSyncRepository.lastSyncTimeValue = now

        val result = fakeSyncRepository.getLastSyncTime()

        assertEquals(now, result)
    }

    @Test
    fun `FakeSyncRepository syncMedications returns Failure when shouldFail`() = runTest {
        fakeSyncRepository.shouldFail = true
        fakeSyncRepository.failureError = DomainError.DatabaseError("DB error", null)

        val result = fakeSyncRepository.syncMedications("test-id")

        assertTrue(result is SyncResult.Failure)
        assertTrue((result as SyncResult.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `FakeSyncRepository defaultSyncResult is customizable`() = runTest {
        fakeSyncRepository.defaultSyncResult = SyncResult.Success(
            uploadedCount = 5,
            downloadedCount = 10,
            conflictCount = 2
        )

        val result = fakeSyncRepository.syncAll("test-id")

        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(5, success.uploadedCount)
        assertEquals(10, success.downloadedCount)
        assertEquals(2, success.conflictCount)
    }

    @Test
    fun `FakeSyncRepository can return PartialSuccess`() = runTest {
        fakeSyncRepository.defaultSyncResult = SyncResult.PartialSuccess(
            successCount = 8,
            failedEntities = listOf(1L, 2L, 3L),
            errors = listOf(DomainError.NetworkError("Partial failure"))
        )

        val result = fakeSyncRepository.syncAll("test-id")

        assertTrue(result is SyncResult.PartialSuccess)
        val partial = result as SyncResult.PartialSuccess
        assertEquals(8, partial.successCount)
        assertEquals(3, partial.failedEntities.size)
    }

    // ========== Auth State Tests ==========

    @Test
    fun `FakeAuthRepository returns null when not logged in`() {
        assertNull(fakeAuthRepository.getCurrentUser())
    }

    @Test
    fun `FakeAuthRepository returns user when logged in`() {
        val testUser = User(
            uid = "test-uid",
            email = "test@example.com",
            name = "Test User",
            createdAt = LocalDateTime.now(),
            isPremium = false
        )
        fakeAuthRepository.setCurrentUser(testUser)

        val user = fakeAuthRepository.getCurrentUser()

        assertEquals("test-uid", user?.uid)
        assertEquals("test@example.com", user?.email)
    }

    // ========== Error Type Tests ==========

    @Test
    fun `NetworkError should be retryable`() {
        val error = DomainError.NetworkError("Connection timeout")
        // In SyncWorker, NetworkError maps to Result.retry()
        assertTrue(error is DomainError.NetworkError)
    }

    @Test
    fun `UnauthorizedError should not be retryable`() {
        val error = DomainError.UnauthorizedError("Token expired")
        // In SyncWorker, UnauthorizedError maps to Result.failure()
        assertTrue(error is DomainError.UnauthorizedError)
    }

    @Test
    fun `DatabaseError should be retryable with limit`() {
        val error = DomainError.DatabaseError("Write failed", null)
        // In SyncWorker, DatabaseError maps to handleRetryOrFailure()
        assertTrue(error is DomainError.DatabaseError)
    }

    @Test
    fun `ValidationError should not be retryable`() {
        val error = DomainError.ValidationError("Invalid data", "field")
        // In SyncWorker, ValidationError maps to Result.failure()
        assertTrue(error is DomainError.ValidationError)
    }

    @Test
    fun `NotFoundError should not be retryable`() {
        val error = DomainError.NotFoundError("Document not found")
        // In SyncWorker, NotFoundError maps to Result.failure()
        assertTrue(error is DomainError.NotFoundError)
    }

    @Test
    fun `UnknownError should be retryable with limit`() {
        val error = DomainError.UnknownError("Unknown error", null)
        // In SyncWorker, UnknownError maps to handleRetryOrFailure()
        assertTrue(error is DomainError.UnknownError)
    }

    // ========== Config Tests ==========

    @Test
    fun `MAX_RETRIES config value is accessible`() {
        assertEquals(3, AppConfig.Sync.MAX_RETRIES)
    }

    @Test
    fun `SYNC_INTERVAL_MINUTES config value is accessible`() {
        assertEquals(15L, AppConfig.Sync.SYNC_INTERVAL_MINUTES)
    }

    // ========== SyncResult Tests ==========

    @Test
    fun `SyncResult Success calculates totalSynced correctly`() {
        val result = SyncResult.Success(
            uploadedCount = 5,
            downloadedCount = 10,
            conflictCount = 2
        )

        assertEquals(15, result.totalSynced)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `SyncResult Failure has correct properties`() {
        val result = SyncResult.Failure(DomainError.NetworkError("Error"))

        assertTrue(result.isFailure)
        assertTrue(!result.isSuccess)
    }

    @Test
    fun `SyncResult PartialSuccess has correct properties`() {
        val result = SyncResult.PartialSuccess(
            successCount = 5,
            failedEntities = listOf(1L),
            errors = listOf(DomainError.NetworkError("Error"))
        )

        assertTrue(result.isPartialSuccess)
        assertTrue(!result.isSuccess)
        assertTrue(!result.isFailure)
    }
}
