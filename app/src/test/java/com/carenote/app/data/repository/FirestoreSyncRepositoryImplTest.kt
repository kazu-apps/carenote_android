package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.data.local.entity.SyncMappingEntity
import com.carenote.app.data.repository.sync.EntitySyncer
import com.carenote.app.data.repository.sync.MedicationLogSyncer
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.common.SyncState
import com.carenote.app.fakes.FakeSyncMappingDao
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import com.carenote.app.testing.assertSyncFailure
import com.carenote.app.testing.assertSyncPartialSuccess
import com.carenote.app.testing.assertSyncSuccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * FirestoreSyncRepositoryImpl のユニットテスト
 *
 * 5つの EntitySyncer を MockK でモック化し、同期オーケストレーションをテストする。
 *
 * テスト対象:
 * - syncAll(): 全エンティティ同期のオーケストレーション
 * - syncMedications(), syncNotes() 等: 個別同期
 * - syncMedicationLogs(): 服薬ログ同期（マッピング必須）
 * - pushLocalChanges(), pullRemoteChanges(): 方向別同期
 * - syncState: 同期状態 Flow
 */
class FirestoreSyncRepositoryImplTest {

    private lateinit var settingsDataSource: SettingsDataSource
    private lateinit var syncMappingDao: FakeSyncMappingDao
    private lateinit var medicationSyncer: EntitySyncer<*, *>
    private lateinit var medicationLogSyncer: MedicationLogSyncer
    private lateinit var noteSyncer: EntitySyncer<*, *>
    private lateinit var healthRecordSyncer: EntitySyncer<*, *>
    private lateinit var calendarEventSyncer: EntitySyncer<*, *>
    private lateinit var noteCommentSyncer: EntitySyncer<*, *>

    private lateinit var repository: FirestoreSyncRepositoryImpl

    private val careRecipientId = "test-care-recipient-id"

    // Mutable state for SettingsDataSource mock
    private var lastSyncTime: LocalDateTime? = null

    @Before
    fun setUp() {
        lastSyncTime = null

        settingsDataSource = mockk(relaxed = true)
        syncMappingDao = FakeSyncMappingDao()

        // Setup SettingsDataSource mock
        every { settingsDataSource.getLastSyncTime() } answers { lastSyncTime }
        coEvery { settingsDataSource.updateLastSyncTime(any()) } answers {
            lastSyncTime = firstArg()
        }

        medicationSyncer = mockk(relaxed = true)
        medicationLogSyncer = mockk(relaxed = true)
        noteSyncer = mockk(relaxed = true)
        healthRecordSyncer = mockk(relaxed = true)
        calendarEventSyncer = mockk(relaxed = true)
        noteCommentSyncer = mockk(relaxed = true)

        // Default mock behavior: all syncers return success
        coEvery { medicationSyncer.sync(any(), any()) } returns SyncResult.Success(0, 0)
        coEvery { noteSyncer.sync(any(), any()) } returns SyncResult.Success(0, 0)
        coEvery { healthRecordSyncer.sync(any(), any()) } returns SyncResult.Success(0, 0)
        coEvery { calendarEventSyncer.sync(any(), any()) } returns SyncResult.Success(0, 0)
        coEvery { noteCommentSyncer.sync(any(), any()) } returns SyncResult.Success(0, 0)
        coEvery { medicationLogSyncer.syncForMedication(any(), any(), any(), any()) } returns
            SyncResult.Success(0, 0)

        // Set entityType for each syncer (property, not suspending)
        every { medicationSyncer.entityType } returns "medication"
        every { noteSyncer.entityType } returns "note"
        every { healthRecordSyncer.entityType } returns "healthRecord"
        every { calendarEventSyncer.entityType } returns "calendarEvent"
        every { medicationLogSyncer.entityType } returns "medicationLog"
        every { noteCommentSyncer.entityType } returns "noteComment"

        val lazyFirestore = dagger.Lazy { mockk<FirebaseFirestore>(relaxed = true) }

        repository = FirestoreSyncRepositoryImpl(
            firestore = lazyFirestore,
            settingsDataSource = settingsDataSource,
            syncMappingDao = syncMappingDao,
            medicationSyncer = medicationSyncer,
            medicationLogSyncer = medicationLogSyncer,
            noteSyncer = noteSyncer,
            healthRecordSyncer = healthRecordSyncer,
            calendarEventSyncer = calendarEventSyncer,
            noteCommentSyncer = noteCommentSyncer
        )
    }

    // ========== syncAll() テスト ==========

    @Test
    fun `syncAll returns Success when all syncers succeed`() = runTest {
        // Given: 全 Syncer が成功
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 2, downloadedCount = 1)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 1, downloadedCount = 2)
        coEvery { healthRecordSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 3)
        coEvery { calendarEventSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 1, downloadedCount = 0)

        // When
        val result = repository.syncAll(careRecipientId)

        // Then
        val success = result.assertSyncSuccess()
        assertEquals(4, success.uploadedCount) // 2 + 1 + 0 + 1
        assertEquals(6, success.downloadedCount) // 1 + 2 + 3 + 0
        assertEquals(0, success.conflictCount)
    }

    @Test
    fun `syncAll returns PartialSuccess when some syncers have partial failures`() = runTest {
        // Given: medicationSyncer が PartialSuccess
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.PartialSuccess(
                successCount = 2,
                failedEntities = listOf(1L, 2L),
                errors = listOf(DomainError.NetworkError("Upload failed"))
            )
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 3, downloadedCount = 0)

        // When
        val result = repository.syncAll(careRecipientId)

        // Then
        val partial = result.assertSyncPartialSuccess()
        assertEquals(listOf(1L, 2L), partial.failedEntities)
        assertEquals(1, partial.errors.size)
    }

    @Test
    fun `syncAll returns Failure immediately when any syncer fails completely`() = runTest {
        // Given: noteSyncer が Failure
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 1, downloadedCount = 0)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.Failure(DomainError.NetworkError("Connection timeout"))

        // When
        val result = repository.syncAll(careRecipientId)

        // Then
        val failure = result.assertSyncFailure()
        assertTrue(failure.error is DomainError.NetworkError)
        assertEquals("Connection timeout", failure.error.message)

        // healthRecordSyncer 以降は呼ばれない（即時リターン）
        coVerify(exactly = 1) { medicationSyncer.sync(any(), any()) }
        coVerify(exactly = 1) { noteSyncer.sync(any(), any()) }
        coVerify(exactly = 0) { healthRecordSyncer.sync(any(), any()) }
    }

    @Test
    fun `syncAll updates lastSyncTime on success`() = runTest {
        // Given: lastSyncTime が null
        assertNull(lastSyncTime)

        // When
        val result = repository.syncAll(careRecipientId)

        // Then
        result.assertSyncSuccess()
        assertTrue(lastSyncTime != null)
    }

    @Test
    fun `syncAll calls syncAllMedicationLogs after entity syncs`() = runTest {
        // Given: medication mapping が存在する
        syncMappingDao.upsert(
            SyncMappingEntity(
                entityType = "medication",
                localId = 1L,
                remoteId = "remote-med-1",
                lastSyncedAt = LocalDateTime.now().toString()
            )
        )

        // When
        repository.syncAll(careRecipientId)

        // Then: medicationLogSyncer.syncForMedication が呼ばれる
        coVerify(atLeast = 1) { medicationLogSyncer.syncForMedication(any(), eq(1L), eq("remote-med-1"), any()) }
    }

    @Test
    fun `syncAll updates syncState with progress`() = runTest {
        // Given
        val capturedStates = mutableListOf<SyncState>()

        repository.syncState.test {
            // Initial state
            val initialState = awaitItem()
            assertTrue(initialState is SyncState.Idle)

            // When: trigger syncAll in background
            val result = repository.syncAll(careRecipientId)

            // Then: expect Syncing states with progress and finally Success
            while (true) {
                val state = awaitItem()
                capturedStates.add(state)
                if (state is SyncState.Success) break
            }

            // Verify we got Syncing states
            assertTrue(capturedStates.any { it is SyncState.Syncing })
            assertTrue(capturedStates.last() is SyncState.Success)
        }
    }

    // ========== Individual sync methods テスト ==========

    @Test
    fun `syncMedications delegates to medicationSyncer`() = runTest {
        // Given
        coEvery { medicationSyncer.sync(careRecipientId, any()) } returns
            SyncResult.Success(uploadedCount = 5, downloadedCount = 3)

        // When
        val result = repository.syncMedications(careRecipientId)

        // Then
        val success = result.assertSyncSuccess()
        assertEquals(5, success.uploadedCount)
        assertEquals(3, success.downloadedCount)

        coVerify(exactly = 1) { medicationSyncer.sync(careRecipientId, any()) }
    }

    @Test
    fun `syncNotes delegates to noteSyncer`() = runTest {
        // Given
        coEvery { noteSyncer.sync(careRecipientId, any()) } returns
            SyncResult.Success(uploadedCount = 2, downloadedCount = 1)

        // When
        val result = repository.syncNotes(careRecipientId)

        // Then
        result.assertSyncSuccess()
        coVerify(exactly = 1) { noteSyncer.sync(careRecipientId, any()) }
    }

    @Test
    fun `syncHealthRecords delegates to healthRecordSyncer`() = runTest {
        // Given
        coEvery { healthRecordSyncer.sync(careRecipientId, any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 10)

        // When
        val result = repository.syncHealthRecords(careRecipientId)

        // Then
        result.assertSyncSuccess()
        coVerify(exactly = 1) { healthRecordSyncer.sync(careRecipientId, any()) }
    }

    @Test
    fun `syncCalendarEvents delegates to calendarEventSyncer`() = runTest {
        // Given
        coEvery { calendarEventSyncer.sync(careRecipientId, any()) } returns
            SyncResult.Success(uploadedCount = 3, downloadedCount = 7)

        // When
        val result = repository.syncCalendarEvents(careRecipientId)

        // Then
        result.assertSyncSuccess()
        coVerify(exactly = 1) { calendarEventSyncer.sync(careRecipientId, any()) }
    }

    // ========== syncMedicationLogs() テスト ==========

    @Test
    fun `syncMedicationLogs returns NotFoundError when mapping not found`() = runTest {
        // Given: マッピングが存在しない
        val medicationId = 999L

        // When
        val result = repository.syncMedicationLogs(careRecipientId, medicationId)

        // Then
        val failure = result.assertSyncFailure()
        assertTrue(failure.error is DomainError.NotFoundError)
        assertTrue(failure.error.message.contains("999"))
    }

    @Test
    fun `syncMedicationLogs calls syncer with remoteId from mapping`() = runTest {
        // Given: マッピングが存在する
        val medicationLocalId = 42L
        val medicationRemoteId = "remote-medication-42"

        syncMappingDao.upsert(
            SyncMappingEntity(
                entityType = "medication",
                localId = medicationLocalId,
                remoteId = medicationRemoteId,
                lastSyncedAt = LocalDateTime.now().toString()
            )
        )

        coEvery {
            medicationLogSyncer.syncForMedication(
                careRecipientId,
                medicationLocalId,
                medicationRemoteId,
                any()
            )
        } returns SyncResult.Success(uploadedCount = 2, downloadedCount = 1)

        // When
        val result = repository.syncMedicationLogs(careRecipientId, medicationLocalId)

        // Then
        result.assertSyncSuccess()
        coVerify(exactly = 1) {
            medicationLogSyncer.syncForMedication(
                careRecipientId,
                medicationLocalId,
                medicationRemoteId,
                any()
            )
        }
    }

    // ========== pushLocalChanges() / pullRemoteChanges() テスト ==========

    @Test
    fun `pushLocalChanges calls all syncers and updates lastSyncTime`() = runTest {
        // Given
        assertNull(lastSyncTime)

        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 1, downloadedCount = 0)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 2, downloadedCount = 0)
        coEvery { healthRecordSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 0)
        coEvery { calendarEventSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 1, downloadedCount = 0)

        // When
        val result = repository.pushLocalChanges(careRecipientId)

        // Then
        val success = result.assertSyncSuccess()
        assertEquals(4, success.uploadedCount) // 1 + 2 + 0 + 1
        assertEquals(0, success.downloadedCount)

        // lastSyncTime が更新される
        assertTrue(lastSyncTime != null)

        // 全 Syncer が呼ばれる
        coVerify(exactly = 1) { medicationSyncer.sync(any(), any()) }
        coVerify(exactly = 1) { noteSyncer.sync(any(), any()) }
        coVerify(exactly = 1) { healthRecordSyncer.sync(any(), any()) }
        coVerify(exactly = 1) { calendarEventSyncer.sync(any(), any()) }
    }

    @Test
    fun `pullRemoteChanges calls all syncers and updates lastSyncTime`() = runTest {
        // Given
        assertNull(lastSyncTime)

        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 3)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 2)
        coEvery { healthRecordSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 5, conflictCount = 2)
        coEvery { calendarEventSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 1)

        // When
        val result = repository.pullRemoteChanges(careRecipientId)

        // Then
        val success = result.assertSyncSuccess()
        assertEquals(0, success.uploadedCount)
        assertEquals(11, success.downloadedCount) // 3 + 2 + 5 + 1
        assertEquals(2, success.conflictCount)

        // lastSyncTime が更新される
        assertTrue(lastSyncTime != null)
    }

    @Test
    fun `pushLocalChanges returns Failure when any syncer fails`() = runTest {
        // Given: noteSyncer が失敗
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 1, downloadedCount = 0)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.Failure(DomainError.UnauthorizedError("Token expired"))

        // When
        val result = repository.pushLocalChanges(careRecipientId)

        // Then
        val failure = result.assertSyncFailure()
        assertTrue(failure.error is DomainError.UnauthorizedError)

        // 後続の Syncer は呼ばれない
        coVerify(exactly = 0) { healthRecordSyncer.sync(any(), any()) }
    }

    @Test
    fun `pullRemoteChanges returns PartialSuccess when some syncers fail partially`() = runTest {
        // Given
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 2)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.PartialSuccess(
                successCount = 1,
                failedEntities = listOf(5L),
                errors = listOf(DomainError.ValidationError("Invalid note data"))
            )

        // When
        val result = repository.pullRemoteChanges(careRecipientId)

        // Then
        val partial = result.assertSyncPartialSuccess()
        assertEquals(listOf(5L), partial.failedEntities)
    }

    // ========== syncState テスト ==========

    @Test
    fun `syncState starts as Idle`() = runTest {
        repository.syncState.test {
            val state = awaitItem()
            assertTrue(state is SyncState.Idle)
        }
    }

    @Test
    fun `syncState transitions to Syncing during sync`() = runTest {
        repository.syncState.test {
            // Skip initial Idle
            awaitItem()

            // Trigger sync
            repository.syncAll(careRecipientId)

            // Collect states until Success
            val states = mutableListOf<SyncState>()
            while (true) {
                val state = awaitItem()
                states.add(state)
                if (state is SyncState.Success) break
            }

            // Verify Syncing states occurred
            val syncingStates = states.filterIsInstance<SyncState.Syncing>()
            assertTrue(syncingStates.isNotEmpty())

            // Verify progress increases
            val progressValues = syncingStates.map { it.progress }
            assertTrue(progressValues.isNotEmpty())
        }
    }

    @Test
    fun `syncState transitions to Success on completion`() = runTest {
        repository.syncState.test {
            // Skip initial Idle
            awaitItem()

            // Trigger sync
            repository.syncAll(careRecipientId)

            // Skip Syncing states and get final Success
            var lastState: SyncState = SyncState.Idle
            while (true) {
                lastState = awaitItem()
                if (lastState is SyncState.Success) break
            }

            assertTrue(lastState is SyncState.Success)
            val success = lastState as SyncState.Success
            assertTrue(success.lastSyncedAt != null)
        }
    }

    @Test
    fun `syncState transitions to Error on failure`() = runTest {
        // Given: 失敗するように設定
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Failure(DomainError.NetworkError("Network error"))

        repository.syncState.test {
            // Skip initial Idle
            awaitItem()

            // Trigger sync
            repository.syncAll(careRecipientId)

            // Skip Syncing states and get final Error
            var lastState: SyncState = SyncState.Idle
            while (true) {
                lastState = awaitItem()
                if (lastState is SyncState.Error) break
            }

            assertTrue(lastState is SyncState.Error)
            val error = lastState as SyncState.Error
            assertTrue(error.error is DomainError.NetworkError)
            assertTrue(error.isRetryable)
        }
    }

    // ========== getLastSyncTime() テスト ==========

    @Test
    fun `getLastSyncTime returns null initially`() = runTest {
        // Given: lastSyncTime が未設定（setUp で null に初期化）

        // When
        val result = repository.getLastSyncTime()

        // Then
        assertNull(result)
    }

    @Test
    fun `getLastSyncTime returns value after sync`() = runTest {
        // Given: 同期を実行
        repository.syncAll(careRecipientId)

        // When
        val result = repository.getLastSyncTime()

        // Then: updateLastSyncTime が呼ばれ、lastSyncTime が更新される
        assertTrue(result != null)
    }

    // ========== 複合シナリオ テスト ==========

    @Test
    fun `syncAll aggregates results from all syncers correctly`() = runTest {
        // Given: 各 Syncer が異なる結果を返す
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 10, downloadedCount = 5, conflictCount = 2)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.PartialSuccess(
                successCount = 3,
                failedEntities = listOf(1L),
                errors = listOf(DomainError.NetworkError("Error"))
            )
        coEvery { healthRecordSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 2, downloadedCount = 8, conflictCount = 1)
        coEvery { calendarEventSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 0)

        // When
        val result = repository.syncAll(careRecipientId)

        // Then: PartialSuccess（一部失敗があるため）
        val partial = result.assertSyncPartialSuccess()
        assertEquals(listOf(1L), partial.failedEntities)
        assertEquals(1, partial.errors.size)
    }

    @Test
    fun `syncAll processes medicationLogs for all medication mappings`() = runTest {
        // Given: 複数の medication mapping が存在する
        syncMappingDao.upsert(
            SyncMappingEntity(
                entityType = "medication",
                localId = 1L,
                remoteId = "remote-1",
                lastSyncedAt = LocalDateTime.now().toString()
            )
        )
        syncMappingDao.upsert(
            SyncMappingEntity(
                entityType = "medication",
                localId = 2L,
                remoteId = "remote-2",
                lastSyncedAt = LocalDateTime.now().toString()
            )
        )
        syncMappingDao.upsert(
            SyncMappingEntity(
                entityType = "medication",
                localId = 3L,
                remoteId = "remote-3",
                lastSyncedAt = LocalDateTime.now().toString()
            )
        )

        // When
        repository.syncAll(careRecipientId)

        // Then: 各 medication の logs が同期される
        coVerify(exactly = 1) {
            medicationLogSyncer.syncForMedication(any(), eq(1L), eq("remote-1"), any())
        }
        coVerify(exactly = 1) {
            medicationLogSyncer.syncForMedication(any(), eq(2L), eq("remote-2"), any())
        }
        coVerify(exactly = 1) {
            medicationLogSyncer.syncForMedication(any(), eq(3L), eq("remote-3"), any())
        }
    }

    @Test
    fun `syncAll passes lastSyncTime to all syncers`() = runTest {
        // Given: lastSyncTime を設定
        val lastSync = LocalDateTime.of(2025, 1, 20, 10, 30)
        lastSyncTime = lastSync

        // When
        repository.syncAll(careRecipientId)

        // Then: 全 Syncer に lastSyncTime が渡される
        coVerify { medicationSyncer.sync(careRecipientId, lastSync) }
        coVerify { noteSyncer.sync(careRecipientId, lastSync) }
        coVerify { healthRecordSyncer.sync(careRecipientId, lastSync) }
        coVerify { calendarEventSyncer.sync(careRecipientId, lastSync) }
    }

    // ========== lastSyncTime PartialSuccess バグ修正テスト ==========

    @Test
    fun `syncAll does NOT update lastSyncTime on PartialSuccess`() = runTest {
        // Given: PartialSuccess を返すように設定
        assertNull(lastSyncTime)
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.PartialSuccess(
                successCount = 1,
                failedEntities = listOf(1L),
                errors = listOf(DomainError.NetworkError("Upload failed"))
            )

        // When
        val result = repository.syncAll(careRecipientId)

        // Then: PartialSuccess の場合、lastSyncTime は更新されない
        result.assertSyncPartialSuccess()
        assertNull(lastSyncTime)
    }

    @Test
    fun `pushLocalChanges does NOT update lastSyncTime on PartialSuccess`() = runTest {
        // Given: PartialSuccess を返すように設定
        assertNull(lastSyncTime)
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 1, downloadedCount = 0)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.PartialSuccess(
                successCount = 1,
                failedEntities = listOf(5L),
                errors = listOf(DomainError.NetworkError("Push failed"))
            )

        // When
        val result = repository.pushLocalChanges(careRecipientId)

        // Then
        result.assertSyncPartialSuccess()
        assertNull(lastSyncTime)
    }

    @Test
    fun `pullRemoteChanges does NOT update lastSyncTime on PartialSuccess`() = runTest {
        // Given: PartialSuccess を返すように設定
        assertNull(lastSyncTime)
        coEvery { medicationSyncer.sync(any(), any()) } returns
            SyncResult.Success(uploadedCount = 0, downloadedCount = 2)
        coEvery { noteSyncer.sync(any(), any()) } returns
            SyncResult.PartialSuccess(
                successCount = 1,
                failedEntities = listOf(10L),
                errors = listOf(DomainError.NetworkError("Pull failed"))
            )

        // When
        val result = repository.pullRemoteChanges(careRecipientId)

        // Then
        result.assertSyncPartialSuccess()
        assertNull(lastSyncTime)
    }
}
