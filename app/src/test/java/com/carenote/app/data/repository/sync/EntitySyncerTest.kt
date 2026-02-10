package com.carenote.app.data.repository.sync

import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.fakes.FakeSyncMappingDao
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import dagger.Lazy as DaggerLazy

/**
 * EntitySyncer のユニットテスト
 *
 * TestEntitySyncer を使用して抽象クラスのテンプレートメソッドをテストする。
 * - sync(): メインの同期エントリポイント
 * - pushLocalChanges(): ローカル変更をリモートにプッシュ
 * - pullRemoteChanges(): リモート変更をローカルにプル
 * - mergeResults(): プッシュ/プル結果のマージ
 * - mapException(): 例外を DomainError に変換
 */
class EntitySyncerTest {

    private lateinit var firestore: DaggerLazy<FirebaseFirestore>
    private lateinit var syncMappingDao: FakeSyncMappingDao
    private lateinit var timestampConverter: FirestoreTimestampConverter
    private lateinit var syncer: TestEntitySyncer

    private val careRecipientId = "test-care-recipient-id"

    @Before
    fun setUp() {
        val mockFirestore: FirebaseFirestore = mockk(relaxed = true)
        firestore = DaggerLazy<FirebaseFirestore> { mockFirestore }
        syncMappingDao = FakeSyncMappingDao()
        timestampConverter = FirestoreTimestampConverter()
        syncer = TestEntitySyncer(firestore, syncMappingDao, timestampConverter)
    }

    // ========== sync() テスト ==========

    @Test
    fun `sync returns Success when both push and pull succeed`() = runTest {
        // Given: 空のローカルストレージ、push/pull両方成功
        syncer.overridePushResult = SyncResult.Success(uploadedCount = 2, downloadedCount = 0)
        syncer.overridePullResult = SyncResult.Success(uploadedCount = 0, downloadedCount = 3, conflictCount = 1)

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(2, success.uploadedCount)
        assertEquals(3, success.downloadedCount)
        assertEquals(1, success.conflictCount)
    }

    @Test
    fun `sync returns PartialSuccess when push has partial failure`() = runTest {
        // Given: push が PartialSuccess、pull は Success
        syncer.overridePushResult = SyncResult.PartialSuccess(
            successCount = 1,
            failedEntities = listOf(2L),
            errors = listOf(DomainError.NetworkError("Upload failed"))
        )
        syncer.overridePullResult = SyncResult.Success(uploadedCount = 0, downloadedCount = 2)

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.PartialSuccess)
        val partial = result as SyncResult.PartialSuccess
        assertEquals(3, partial.successCount) // 1 (push) + 2 (pull)
        assertEquals(listOf(2L), partial.failedEntities)
        assertEquals(1, partial.errors.size)
    }

    @Test
    fun `sync returns PartialSuccess when pull has partial failure`() = runTest {
        // Given: push は Success、pull が PartialSuccess
        syncer.overridePushResult = SyncResult.Success(uploadedCount = 3, downloadedCount = 0)
        syncer.overridePullResult = SyncResult.PartialSuccess(
            successCount = 1,
            failedEntities = listOf(5L, 6L),
            errors = listOf(
                DomainError.ValidationError("Invalid data"),
                DomainError.NetworkError("Download failed")
            )
        )

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.PartialSuccess)
        val partial = result as SyncResult.PartialSuccess
        assertEquals(4, partial.successCount) // 3 (push) + 1 (pull)
        assertEquals(listOf(5L, 6L), partial.failedEntities)
        assertEquals(2, partial.errors.size)
    }

    @Test
    fun `sync returns Failure when push fails completely`() = runTest {
        // Given: push で例外発生
        syncer.throwOnPush = RuntimeException("Critical push error")

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Failure)
        val failure = result as SyncResult.Failure
        assertTrue(failure.error is DomainError.UnknownError)
        assertTrue(failure.error.message.contains("Critical push error"))
    }

    @Test
    fun `sync returns Failure when pull fails completely`() = runTest {
        // Given: push は成功、pull で例外発生
        syncer.overridePushResult = SyncResult.Success(uploadedCount = 1, downloadedCount = 0)
        syncer.throwOnPull = RuntimeException("Critical pull error")

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Failure)
        val failure = result as SyncResult.Failure
        assertTrue(failure.error is DomainError.UnknownError)
    }

    // ========== mergeResults() テスト ==========

    @Test
    fun `sync merges Success and Success correctly`() = runTest {
        // Given
        syncer.overridePushResult = SyncResult.Success(uploadedCount = 5, downloadedCount = 0, conflictCount = 0)
        syncer.overridePullResult = SyncResult.Success(uploadedCount = 0, downloadedCount = 10, conflictCount = 3)

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(5, success.uploadedCount)
        assertEquals(10, success.downloadedCount)
        assertEquals(3, success.conflictCount)
        assertEquals(15, success.totalSynced)
    }

    @Test
    fun `sync merges PartialSuccess and PartialSuccess correctly`() = runTest {
        // Given
        syncer.overridePushResult = SyncResult.PartialSuccess(
            successCount = 2,
            failedEntities = listOf(1L),
            errors = listOf(DomainError.NetworkError("Error 1"))
        )
        syncer.overridePullResult = SyncResult.PartialSuccess(
            successCount = 3,
            failedEntities = listOf(2L, 3L),
            errors = listOf(
                DomainError.ValidationError("Error 2"),
                DomainError.DatabaseError("Error 3")
            )
        )

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.PartialSuccess)
        val partial = result as SyncResult.PartialSuccess
        assertEquals(5, partial.successCount) // 2 + 3
        assertEquals(listOf(1L, 2L, 3L), partial.failedEntities)
        assertEquals(3, partial.errors.size)
    }

    @Test
    fun `sync prioritizes Failure over PartialSuccess - push failure`() = runTest {
        // Given: push が Failure、pull が PartialSuccess
        syncer.overridePushResult = SyncResult.Failure(
            DomainError.UnauthorizedError("Auth expired")
        )
        syncer.overridePullResult = SyncResult.PartialSuccess(
            successCount = 5,
            failedEntities = listOf(1L),
            errors = listOf(DomainError.NetworkError("Error"))
        )

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Failure)
        val failure = result as SyncResult.Failure
        assertTrue(failure.error is DomainError.UnauthorizedError)
        assertEquals("Auth expired", failure.error.message)
    }

    @Test
    fun `sync prioritizes Failure over PartialSuccess - pull failure`() = runTest {
        // Given: push が Success、pull が Failure
        syncer.overridePushResult = SyncResult.Success(uploadedCount = 3, downloadedCount = 0)
        syncer.overridePullResult = SyncResult.Failure(
            DomainError.NetworkError("Connection lost")
        )

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Failure)
        val failure = result as SyncResult.Failure
        assertTrue(failure.error is DomainError.NetworkError)
    }

    // ========== pushLocalChanges() テスト ==========

    @Test
    fun `pushLocalChanges returns Success with zero count when no local entities`() = runTest {
        // Given: ローカルエンティティなし
        syncer.clear()

        // When: overridePushResult を null にして実際の pushLocalChanges を実行
        // pullRemoteChanges は skip してテスト対象のみを検証
        syncer.overridePullResult = SyncResult.Success(0, 0)
        val result = syncer.sync(careRecipientId, null)

        // Then: push は 0 件成功、pull も 0 件
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(0, success.uploadedCount)
    }

    @Test
    fun `pushLocalChanges uploads new entities without mapping`() = runTest {
        // Given: マッピングなしの新規エンティティ
        val now = LocalDateTime.now()
        syncer.addLocalEntity(TestEntity(id = 1, name = "Entity1", updatedAt = now))
        syncer.addLocalEntity(TestEntity(id = 2, name = "Entity2", updatedAt = now))

        // Firestore を呼ばずにシミュレート実行
        syncer.simulatePushWithoutFirestore = true
        // overridePullResult で pull をスキップ
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then: 2件アップロードが試みられる
        // uploadCount で domainToRemote が呼ばれた回数を確認
        assertEquals(2, syncer.uploadCount)
    }

    @Test
    fun `pushLocalChanges skips entities not modified since lastSyncTime`() = runTest {
        // Given: lastSyncTime より前に更新されたエンティティ（マッピングあり）
        val lastSync = LocalDateTime.of(2025, 1, 15, 10, 0)
        val oldUpdatedAt = LocalDateTime.of(2025, 1, 10, 10, 0) // lastSync より前

        syncer.addLocalEntity(TestEntity(id = 1, name = "OldEntity", updatedAt = oldUpdatedAt))

        // 既存マッピングを追加
        syncMappingDao.upsert(
            com.carenote.app.data.local.entity.SyncMappingEntity(
                entityType = "test_entity",
                localId = 1,
                remoteId = "remote-1",
                lastSyncedAt = "2025-01-10T10:00:00"
            )
        )

        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When
        val result = syncer.sync(careRecipientId, lastSync)

        // Then: lastSyncTime 以降の更新がないためアップロードされない
        assertEquals(0, syncer.uploadCount)
    }

    @Test
    fun `pushLocalChanges uploads entities modified after lastSyncTime`() = runTest {
        // Given: lastSyncTime より後に更新されたエンティティ
        val lastSync = LocalDateTime.of(2025, 1, 15, 10, 0)
        val newUpdatedAt = LocalDateTime.of(2025, 1, 20, 10, 0) // lastSync より後

        syncer.addLocalEntity(TestEntity(id = 1, name = "NewEntity", updatedAt = newUpdatedAt))

        // 既存マッピングを追加
        syncMappingDao.upsert(
            com.carenote.app.data.local.entity.SyncMappingEntity(
                entityType = "test_entity",
                localId = 1,
                remoteId = "remote-1",
                lastSyncedAt = "2025-01-10T10:00:00"
            )
        )

        // Firestore を呼ばずにシミュレート実行
        syncer.simulatePushWithoutFirestore = true
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When
        val result = syncer.sync(careRecipientId, lastSync)

        // Then: lastSyncTime 以降の更新があるためアップロードされる
        assertEquals(1, syncer.uploadCount)
    }

    // ========== pullRemoteChanges() テスト ==========

    @Test
    fun `pullRemoteChanges returns Success with zero count when no remote documents`() = runTest {
        // Given: リモートドキュメントなし
        syncer.overridePushResult = SyncResult.Success(0, 0)
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(0, success.downloadedCount)
    }

    // ========== mapException() テスト ==========

    @Test
    fun `sync maps IllegalArgumentException to ValidationError`() = runTest {
        // Given
        syncer.throwOnPush = IllegalArgumentException("Invalid field value")

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Failure)
        val failure = result as SyncResult.Failure
        assertTrue(failure.error is DomainError.ValidationError)
        assertTrue(failure.error.message.contains("Invalid field value"))
    }

    @Test
    fun `sync maps RuntimeException to UnknownError`() = runTest {
        // Given
        syncer.throwOnPush = RuntimeException("Unexpected error")

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Failure)
        val failure = result as SyncResult.Failure
        assertTrue(failure.error is DomainError.UnknownError)
    }

    // ========== 初回同期（lastSyncTime = null） テスト ==========

    @Test
    fun `sync uploads all entities on initial sync when lastSyncTime is null`() = runTest {
        // Given: 初回同期（lastSyncTime = null）、複数の新規エンティティ
        val now = LocalDateTime.now()
        syncer.addLocalEntity(TestEntity(id = 1, name = "E1", updatedAt = now.minusDays(5)))
        syncer.addLocalEntity(TestEntity(id = 2, name = "E2", updatedAt = now.minusDays(3)))
        syncer.addLocalEntity(TestEntity(id = 3, name = "E3", updatedAt = now))

        // Firestore を呼ばずにシミュレート実行
        syncer.simulatePushWithoutFirestore = true
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When: lastSyncTime = null で同期
        val result = syncer.sync(careRecipientId, null)

        // Then: 全エンティティがアップロードされる
        assertEquals(3, syncer.uploadCount)
    }

    // ========== SyncMapping 作成テスト ==========

    @Test
    fun `pushLocalChanges creates mapping for new entity`() = runTest {
        // Given
        val now = LocalDateTime.now()
        syncer.addLocalEntity(TestEntity(id = 1, name = "NewEntity", updatedAt = now))

        // Firestore を呼ばずにシミュレート実行
        syncer.simulatePushWithoutFirestore = true
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // 初期状態：マッピングなし
        assertEquals(0, syncMappingDao.size)

        // When
        syncer.sync(careRecipientId, null)

        // Then: マッピングが作成される
        assertEquals(1, syncer.uploadCount)
        assertEquals(1, syncMappingDao.size)

        // マッピングの内容を検証
        val mapping = syncMappingDao.getByLocalId("test_entity", 1)
        assertEquals(1L, mapping?.localId)
        assertEquals("test_entity", mapping?.entityType)
        assertEquals(false, mapping?.isDeleted)
    }

    // ========== エッジケース テスト ==========

    @Test
    fun `sync handles empty local and remote state`() = runTest {
        // Given: ローカルもリモートも空
        syncer.clear()
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When
        val result = syncer.sync(careRecipientId, null)

        // Then
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(0, success.uploadedCount)
        assertEquals(0, success.downloadedCount)
        assertEquals(0, success.conflictCount)
        assertEquals(0, success.totalSynced)
    }

    @Test
    fun `sync uses correct collection path`() = runTest {
        // Given
        val expectedPath = "careRecipients/$careRecipientId/test_entities"

        // When
        val actualPath = syncer.collectionPath(careRecipientId)

        // Then
        assertEquals(expectedPath, actualPath)
    }

    // ========== getModifiedSince() テスト ==========

    @Test
    fun `pushLocalChanges uses getModifiedSince when lastSyncTime is not null`() = runTest {
        // Given: 3 エンティティ、getModifiedSince は 1 件のみ返す
        val lastSync = LocalDateTime.of(2025, 1, 15, 10, 0)
        val oldUpdatedAt = LocalDateTime.of(2025, 1, 10, 10, 0)
        val newUpdatedAt = LocalDateTime.of(2025, 1, 20, 10, 0)

        syncer.addLocalEntity(TestEntity(id = 1, name = "Old1", updatedAt = oldUpdatedAt))
        syncer.addLocalEntity(TestEntity(id = 2, name = "Old2", updatedAt = oldUpdatedAt))
        syncer.addLocalEntity(TestEntity(id = 3, name = "New1", updatedAt = newUpdatedAt))

        // getModifiedSince は変更された 1 件のみ返す
        syncer.overrideGetModifiedSince = listOf(
            TestEntity(id = 3, name = "New1", updatedAt = newUpdatedAt)
        )

        syncer.simulatePushWithoutFirestore = true
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When
        val result = syncer.sync(careRecipientId, lastSync)

        // Then: getModifiedSince が使われ、1 件のみアップロード
        assertEquals(1, syncer.uploadCount)
    }

    @Test
    fun `pushLocalChanges falls back to getAllLocal when getModifiedSince returns null`() = runTest {
        // Given: getModifiedSince = null（デフォルト）
        val lastSync = LocalDateTime.of(2025, 1, 15, 10, 0)
        val newUpdatedAt = LocalDateTime.of(2025, 1, 20, 10, 0)

        syncer.addLocalEntity(TestEntity(id = 1, name = "E1", updatedAt = newUpdatedAt))
        syncer.addLocalEntity(TestEntity(id = 2, name = "E2", updatedAt = newUpdatedAt))

        // overrideGetModifiedSince = null (default) → getAllLocal にフォールバック
        syncer.simulatePushWithoutFirestore = true
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When
        val result = syncer.sync(careRecipientId, lastSync)

        // Then: getAllLocal が使われ、全エンティティが処理される（新しいのでアップロード）
        assertEquals(2, syncer.uploadCount)
    }

    @Test
    fun `pushLocalChanges uses getAllLocal when lastSyncTime is null`() = runTest {
        // Given: 初回同期、getModifiedSince は設定されているが使われない
        val now = LocalDateTime.now()

        syncer.addLocalEntity(TestEntity(id = 1, name = "E1", updatedAt = now))
        syncer.addLocalEntity(TestEntity(id = 2, name = "E2", updatedAt = now))

        // getModifiedSince を設定しても、lastSyncTime=null なら使われない
        syncer.overrideGetModifiedSince = emptyList()

        syncer.simulatePushWithoutFirestore = true
        syncer.overridePullResult = SyncResult.Success(0, 0)

        // When: lastSyncTime = null で同期
        val result = syncer.sync(careRecipientId, null)

        // Then: getAllLocal が使われ、全 2 件がアップロード
        assertEquals(2, syncer.uploadCount)
    }
}
