package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.data.local.entity.SyncMappingEntity
import com.carenote.app.data.mapper.MedicationLogMapper
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.mapper.remote.MedicationLogRemoteMapper
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.fakes.FakeSyncMappingDao
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import dagger.Lazy as DaggerLazy

/**
 * MedicationLogSyncer のユニットテスト
 *
 * MedicationLogSyncer はサブコレクション構造（medications/{id}/logs）のため、
 * EntitySyncer の基底 sync() ではなく、独自の syncForMedication() をテストする。
 */
class MedicationLogSyncerTest {

    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var syncMappingDao: FakeSyncMappingDao
    private lateinit var timestampConverter: FirestoreTimestampConverter
    private lateinit var mockMedicationLogDao: MedicationLogDao
    private lateinit var entityMapper: MedicationLogMapper
    private lateinit var remoteMapper: MedicationLogRemoteMapper
    private lateinit var syncer: MedicationLogSyncer

    private val careRecipientId = "test-care-recipient"
    private val medicationLocalId = 1L
    private val medicationRemoteId = "remote-med-1"

    private val now = LocalDateTime.of(2025, 6, 15, 10, 0, 0)
    private val nowString = "2025-06-15T10:00:00"

    @Before
    fun setUp() {
        mockFirestore = mockk(relaxed = true)
        syncMappingDao = FakeSyncMappingDao()
        timestampConverter = FirestoreTimestampConverter()
        mockMedicationLogDao = mockk(relaxed = true)
        entityMapper = MedicationLogMapper()
        remoteMapper = MedicationLogRemoteMapper(timestampConverter)

        syncer = MedicationLogSyncer(
            firestore = DaggerLazy { mockFirestore },
            syncMappingDao = syncMappingDao,
            timestampConverter = timestampConverter,
            medicationLogDao = mockMedicationLogDao,
            entityMapper = entityMapper,
            remoteMapper = remoteMapper
        )

        // Default: empty DAO results
        every { mockMedicationLogDao.getLogsForMedication(any()) } returns flowOf(emptyList())
    }

    // ========== entityType テスト ==========

    @Test
    fun `entityType is medicationLog`() {
        assertEquals("medicationLog", syncer.entityType)
    }

    // ========== collectionPath テスト ==========

    @Test
    fun `collectionPath with careRecipientId and medicationRemoteId returns correct path`() {
        val path = syncer.collectionPath(careRecipientId, medicationRemoteId)
        assertEquals("careRecipients/$careRecipientId/medications/$medicationRemoteId/logs", path)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `collectionPath with single arg throws UnsupportedOperationException`() {
        syncer.collectionPath(careRecipientId)
    }

    // ========== getAllLocal テスト ==========

    @Test(expected = UnsupportedOperationException::class)
    fun `getAllLocal throws UnsupportedOperationException`() = runTest {
        syncer.getAllLocal()
    }

    @Test
    fun `getAllLocalForMedication returns logs for specific medication`() = runTest {
        val entity = createLogEntity(id = 1, medicationId = medicationLocalId)
        every { mockMedicationLogDao.getLogsForMedication(medicationLocalId) } returns
            flowOf(listOf(entity))

        val result = syncer.getAllLocalForMedication(medicationLocalId)

        assertEquals(1, result.size)
        assertEquals(entity, result[0])
    }

    // ========== syncForMedication テスト ==========

    @Test
    fun `syncForMedication returns Success when no local logs and no remote logs`() = runTest {
        // Given: empty local, empty remote
        every { mockMedicationLogDao.getLogsForMedication(any()) } returns flowOf(emptyList())
        setupEmptyPullMock()

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, now
        )

        // Then
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(0, success.uploadedCount)
        assertEquals(0, success.downloadedCount)
    }

    @Test
    fun `syncForMedication returns Failure on exception`() = runTest {
        // Given: DAO throws
        every { mockMedicationLogDao.getLogsForMedication(any()) } throws
            RuntimeException("Database error")

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `syncForMedication with local logs pushes them to Firestore`() = runTest {
        // Given: 2 local entities, no mappings, empty pull
        val log1 = createLogEntity(id = 1, medicationId = medicationLocalId, recordedAt = nowString)
        val log2 = createLogEntity(id = 2, medicationId = medicationLocalId, recordedAt = nowString)
        every { mockMedicationLogDao.getLogsForMedication(medicationLocalId) } returns
            flowOf(listOf(log1, log2))

        setupPushMock()
        setupEmptyPullMock()

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(2, success.uploadedCount)
    }

    @Test
    fun `syncForMedication with null lastSyncTime uploads all logs`() = runTest {
        // Given: 2 local entities with old recordedAt, no mappings
        val oldTime = "2024-01-01T08:00:00"
        val log1 = createLogEntity(id = 1, medicationId = medicationLocalId, recordedAt = oldTime)
        val log2 = createLogEntity(id = 2, medicationId = medicationLocalId, recordedAt = oldTime)
        every { mockMedicationLogDao.getLogsForMedication(medicationLocalId) } returns
            flowOf(listOf(log1, log2))

        setupPushMock()
        setupEmptyPullMock()

        // When: lastSyncTime = null (initial sync)
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then: all logs uploaded regardless of recordedAt
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(2, success.uploadedCount)
    }

    // ========== pushLogsForMedication テスト ==========

    @Test
    fun `push uploads new log without existing mapping`() = runTest {
        // Given: 1 new entity, no mapping
        val log = createLogEntity(id = 10, medicationId = medicationLocalId, recordedAt = nowString)
        every { mockMedicationLogDao.getLogsForMedication(medicationLocalId) } returns
            flowOf(listOf(log))

        setupPushMock()
        setupEmptyPullMock()

        assertEquals(0, syncMappingDao.size)

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then: uploaded and mapping created
        assertTrue(result is SyncResult.Success)
        assertEquals(1, (result as SyncResult.Success).uploadedCount)
        assertEquals(1, syncMappingDao.size)

        val mapping = syncMappingDao.getByLocalId("medicationLog", 10)
        assertEquals(10L, mapping?.localId)
        assertEquals("medicationLog", mapping?.entityType)
    }

    @Test
    fun `push uploads modified log after lastSyncTime`() = runTest {
        // Given: entity with mapping, recordedAt > lastSyncTime
        val lastSync = LocalDateTime.of(2025, 6, 1, 0, 0)
        val newRecordedAt = "2025-06-10T10:00:00" // after lastSync
        val log = createLogEntity(id = 5, medicationId = medicationLocalId, recordedAt = newRecordedAt)
        every { mockMedicationLogDao.getLogsForMedication(medicationLocalId) } returns
            flowOf(listOf(log))

        // Existing mapping
        syncMappingDao.upsert(
            SyncMappingEntity(
                entityType = "medicationLog",
                localId = 5,
                remoteId = "existing-remote-5",
                lastSyncedAt = "2025-06-01T00:00:00"
            )
        )

        setupPushMock()
        setupEmptyPullMock()

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, lastSync
        )

        // Then: modified entity is uploaded
        assertTrue(result is SyncResult.Success)
        assertEquals(1, (result as SyncResult.Success).uploadedCount)
    }

    @Test
    fun `push skips unmodified log before lastSyncTime`() = runTest {
        // Given: entity with mapping, recordedAt < lastSyncTime
        val lastSync = LocalDateTime.of(2025, 6, 15, 0, 0)
        val oldRecordedAt = "2025-06-01T08:00:00" // before lastSync
        val log = createLogEntity(id = 5, medicationId = medicationLocalId, recordedAt = oldRecordedAt)
        every { mockMedicationLogDao.getLogsForMedication(medicationLocalId) } returns
            flowOf(listOf(log))

        // Existing mapping
        syncMappingDao.upsert(
            SyncMappingEntity(
                entityType = "medicationLog",
                localId = 5,
                remoteId = "existing-remote-5",
                lastSyncedAt = "2025-06-01T00:00:00"
            )
        )

        setupPushMock()
        setupEmptyPullMock()

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, lastSync
        )

        // Then: unmodified entity is skipped
        assertTrue(result is SyncResult.Success)
        assertEquals(0, (result as SyncResult.Success).uploadedCount)
    }

    @Test
    fun `push returns PartialSuccess when some uploads fail`() = runTest {
        // Given: 2 entities, first succeeds, second Firestore call throws
        val log1 = createLogEntity(id = 1, medicationId = medicationLocalId, recordedAt = nowString)
        val log2 = createLogEntity(id = 2, medicationId = medicationLocalId, recordedAt = nowString)
        every { mockMedicationLogDao.getLogsForMedication(medicationLocalId) } returns
            flowOf(listOf(log1, log2))

        // Set up pull mock first (so push overrides take priority)
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockQuery = mockk<Query>(relaxed = true)
        val mockQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)

        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } returns mockQuery
        every { mockQuery.whereGreaterThan(any<String>(), any()) } returns mockQuery
        every { mockQuery.get() } returns Tasks.forResult(mockQuerySnapshot)
        every { mockQuerySnapshot.documents } returns emptyList()

        // Push mocks: first call succeeds, second call fails
        val mockDocRef1 = mockk<DocumentReference>(relaxed = true)
        val mockDocRef2 = mockk<DocumentReference>(relaxed = true)
        val successTask = Tasks.forResult<Void>(null)
        val failTask = Tasks.forException<Void>(RuntimeException("Firestore write failed"))

        every { mockCollection.document(any()) } returnsMany listOf(mockDocRef1, mockDocRef2)
        every { mockDocRef1.set(any(), any<SetOptions>()) } returns successTask
        every { mockDocRef2.set(any(), any<SetOptions>()) } returns failTask

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then
        assertTrue(result is SyncResult.PartialSuccess)
        val partial = result as SyncResult.PartialSuccess
        assertEquals(1, partial.successCount)
        assertEquals(1, partial.failedEntities.size)
        assertEquals(1, partial.errors.size)
    }

    // ========== pullLogsForMedication テスト ==========

    @Test
    fun `pull downloads new remote log and creates mapping`() = runTest {
        // Given: empty local, 1 remote doc with no existing mapping
        every { mockMedicationLogDao.getLogsForMedication(any()) } returns flowOf(emptyList())

        val remoteDocId = "remote-log-1"
        val remoteData = createRemoteLogData(localId = 100, medicationLocalId = 5)

        val mockDocSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        every { mockDocSnapshot.data } returns remoteData
        every { mockDocSnapshot.id } returns remoteDocId

        setupPushMock()
        setupPullMock(listOf(mockDocSnapshot))

        coEvery { mockMedicationLogDao.insertLog(any()) } returns 50L

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(1, success.downloadedCount)

        // Mapping created
        val mapping = syncMappingDao.getByRemoteId("medicationLog", remoteDocId)
        assertEquals(50L, mapping?.localId)
        assertEquals(remoteDocId, mapping?.remoteId)
    }

    @Test
    fun `pull skips log with existing mapping`() = runTest {
        // Given: remote doc already mapped
        every { mockMedicationLogDao.getLogsForMedication(any()) } returns flowOf(emptyList())

        val remoteDocId = "remote-log-existing"
        val remoteData = createRemoteLogData(localId = 100, medicationLocalId = 5)

        val mockDocSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        every { mockDocSnapshot.data } returns remoteData
        every { mockDocSnapshot.id } returns remoteDocId

        // Pre-existing mapping
        syncMappingDao.upsert(
            SyncMappingEntity(
                entityType = "medicationLog",
                localId = 100,
                remoteId = remoteDocId,
                lastSyncedAt = "2025-06-01T00:00:00"
            )
        )

        setupPushMock()
        setupPullMock(listOf(mockDocSnapshot))

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then: skipped (downloadedCount = 0)
        assertTrue(result is SyncResult.Success)
        assertEquals(0, (result as SyncResult.Success).downloadedCount)
    }

    @Test
    fun `pull corrects medicationId on downloaded log`() = runTest {
        // Given: remote doc with different medicationLocalId in data
        every { mockMedicationLogDao.getLogsForMedication(any()) } returns flowOf(emptyList())

        val remoteDocId = "remote-log-correct-id"
        // Remote data has medicationLocalId=99 but we pass medicationLocalId=1
        val remoteData = createRemoteLogData(localId = 50, medicationLocalId = 99)

        val mockDocSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        every { mockDocSnapshot.data } returns remoteData
        every { mockDocSnapshot.id } returns remoteDocId

        setupPushMock()
        setupPullMock(listOf(mockDocSnapshot))

        // Capture the entity passed to insertLog
        var capturedEntity: MedicationLogEntity? = null
        coEvery { mockMedicationLogDao.insertLog(any()) } answers {
            capturedEntity = firstArg()
            60L
        }

        // When: medicationLocalId = 1
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then: the saved entity should have medicationId = medicationLocalId (1), not 99
        assertTrue(result is SyncResult.Success)
        assertEquals(1, (result as SyncResult.Success).downloadedCount)
        assertEquals(medicationLocalId, capturedEntity?.medicationId)
    }

    // ========== mergeResults テスト ==========

    @Test
    fun `mergeResults combines two Success results`() = runTest {
        // Given: push uploads 2, pull downloads 3
        val log1 = createLogEntity(id = 1, medicationId = medicationLocalId, recordedAt = nowString)
        val log2 = createLogEntity(id = 2, medicationId = medicationLocalId, recordedAt = nowString)
        every { mockMedicationLogDao.getLogsForMedication(medicationLocalId) } returns
            flowOf(listOf(log1, log2))

        setupPushMock()

        // 3 remote docs
        val remoteDocs = (1..3).map { i ->
            val docId = "remote-pull-$i"
            val data = createRemoteLogData(localId = (100 + i).toLong(), medicationLocalId = 5)
            mockk<DocumentSnapshot>(relaxed = true).also { doc ->
                every { doc.data } returns data
                every { doc.id } returns docId
            }
        }
        setupPullMock(remoteDocs)
        coEvery { mockMedicationLogDao.insertLog(any()) } returnsMany listOf(200L, 201L, 202L)

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(2, success.uploadedCount)
        assertEquals(3, success.downloadedCount)
    }

    @Test
    fun `mergeResults returns Failure when push fails`() = runTest {
        // Given: DAO throws on getLogsForMedication → Failure
        every { mockMedicationLogDao.getLogsForMedication(any()) } throws
            RuntimeException("Critical failure")

        // When
        val result = syncer.syncForMedication(
            careRecipientId, medicationLocalId, medicationRemoteId, null
        )

        // Then: top-level catch returns Failure
        assertTrue(result is SyncResult.Failure)
    }

    // ========== ヘルパーメソッド ==========

    private fun createLogEntity(
        id: Long = 0,
        medicationId: Long = medicationLocalId,
        careRecipientId: Long = 0,
        status: String = "TAKEN",
        scheduledAt: String = nowString,
        recordedAt: String = nowString,
        memo: String = "",
        timing: String? = null
    ) = MedicationLogEntity(
        id = id,
        careRecipientId = careRecipientId,
        medicationId = medicationId,
        status = status,
        scheduledAt = scheduledAt,
        recordedAt = recordedAt,
        memo = memo,
        timing = timing
    )

    private fun createRemoteLogData(
        localId: Long = 0,
        medicationLocalId: Long = 1,
        status: String = "TAKEN",
        memo: String = ""
    ): Map<String, Any?> {
        val scheduledTimestamp = timestampConverter.toTimestamp(now)
        val recordedTimestamp = timestampConverter.toTimestamp(now)
        return mapOf(
            "localId" to localId,
            "medicationLocalId" to medicationLocalId,
            "status" to status,
            "scheduledAt" to scheduledTimestamp,
            "recordedAt" to recordedTimestamp,
            "memo" to memo,
            "timing" to null,
            "syncedAt" to timestampConverter.toTimestamp(now),
            "deletedAt" to null
        )
    }

    /**
     * Push 用 Firestore mock: collection → document → set → await 成功
     */
    private fun setupPushMock() {
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockDocRef = mockk<DocumentReference>(relaxed = true)
        val successTask = Tasks.forResult<Void>(null)

        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocRef
        every { mockDocRef.set(any(), any<SetOptions>()) } returns successTask
    }

    /**
     * Pull 用 Firestore mock: 空の QuerySnapshot を返す
     */
    private fun setupEmptyPullMock() {
        setupPullMock(emptyList())
    }

    /**
     * Pull 用 Firestore mock: 指定ドキュメントリストを返す
     */
    private fun setupPullMock(documents: List<DocumentSnapshot>) {
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockQuery = mockk<Query>(relaxed = true)
        val mockQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)

        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.document(any()) } returns mockk(relaxed = true) {
            val successTask = Tasks.forResult<Void>(null)
            every { set(any(), any<SetOptions>()) } returns successTask
        }
        every { mockCollection.whereEqualTo(any<String>(), any()) } returns mockQuery
        every { mockQuery.whereGreaterThan(any<String>(), any()) } returns mockQuery
        every { mockQuery.get() } returns Tasks.forResult(mockQuerySnapshot)
        every { mockQuerySnapshot.documents } returns documents
    }
}
