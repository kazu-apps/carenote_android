package com.carenote.app.data.repository.sync

import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.fakes.FakeSyncMappingDao
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * SyncerConfig / ConfigDrivenEntitySyncer のユニットテスト (TDD RED)
 *
 * SyncerConfig: 標準 Syncer の全委譲パターンを設定で表現する data class
 * ConfigDrivenEntitySyncer: SyncerConfig に基づいて EntitySyncer を実装するクラス
 *
 * このテストは RED フェーズのため、SyncerConfig / ConfigDrivenEntitySyncer が
 * 存在しない状態ではコンパイルエラーで失敗する。
 */
class SyncerConfigTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var syncMappingDao: FakeSyncMappingDao
    private lateinit var timestampConverter: FirestoreTimestampConverter

    /** テスト用インメモリストレージ */
    private val localStorage = mutableMapOf<Long, TestEntity>()
    private var nextId = 100L

    private val now = LocalDateTime.of(2025, 6, 1, 12, 0, 0)

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        syncMappingDao = FakeSyncMappingDao()
        timestampConverter = FirestoreTimestampConverter()
        localStorage.clear()
        nextId = 100L
    }

    // ========== テスト用ヘルパー ==========

    private fun createTestConfig(
        entityType: String = "test_entity",
        collectionPathPrefix: String = "test_entities"
    ): SyncerConfig<TestEntity, TestDomain> {
        return SyncerConfig(
            entityType = entityType,
            collectionPath = { careRecipientId ->
                "careRecipients/$careRecipientId/$collectionPathPrefix"
            },
            getAllLocal = { localStorage.values.toList() },
            getLocalById = { id -> localStorage[id] },
            saveLocal = { entity ->
                val id = if (entity.id == 0L) nextId++ else entity.id
                localStorage[id] = entity.copy(id = id)
                id
            },
            deleteLocal = { id -> localStorage.remove(id) },
            entityToDomain = { entity ->
                TestDomain(
                    id = entity.id,
                    name = entity.name,
                    updatedAt = entity.updatedAt
                )
            },
            domainToEntity = { domain ->
                TestEntity(
                    id = domain.id,
                    name = domain.name,
                    updatedAt = domain.updatedAt
                )
            },
            domainToRemote = { domain, syncMetadata ->
                mapOf(
                    "name" to domain.name,
                    "updatedAt" to domain.updatedAt.toString(),
                    "localId" to syncMetadata.localId,
                    "syncedAt" to syncMetadata.syncedAt.toString(),
                    "deletedAt" to syncMetadata.deletedAt?.toString()
                )
            },
            remoteToDomain = { data ->
                TestDomain(
                    id = (data["localId"] as? Number)?.toLong() ?: 0L,
                    name = data["name"] as? String ?: "",
                    updatedAt = LocalDateTime.parse(data["updatedAt"] as String)
                )
            },
            extractSyncMetadata = { data ->
                SyncMetadata(
                    localId = (data["localId"] as? Number)?.toLong() ?: 0L,
                    syncedAt = LocalDateTime.parse(data["syncedAt"] as String),
                    deletedAt = (data["deletedAt"] as? String)?.let { LocalDateTime.parse(it) }
                )
            },
            getLocalId = { entity -> entity.id },
            getUpdatedAt = { entity -> entity.updatedAt }
        )
    }

    private fun createSyncer(
        config: SyncerConfig<TestEntity, TestDomain> = createTestConfig()
    ): ConfigDrivenEntitySyncer<TestEntity, TestDomain> {
        return ConfigDrivenEntitySyncer(
            config = config,
            firestore = firestore,
            syncMappingDao = syncMappingDao,
            timestampConverter = timestampConverter
        )
    }

    // ========== カテゴリ A: SyncerConfig 構築テスト ==========

    @Test
    fun `SyncerConfig can be constructed with all required fields`() {
        // When
        val config = createTestConfig()

        // Then
        assertNotNull(config)
        assertEquals("test_entity", config.entityType)
    }

    @Test
    fun `SyncerConfig fields return constructed values`() {
        // Given
        val config = createTestConfig(
            entityType = "medication",
            collectionPathPrefix = "medications"
        )

        // Then
        assertEquals("medication", config.entityType)
        assertEquals(
            "careRecipients/cr-1/medications",
            config.collectionPath("cr-1")
        )
    }

    @Test
    fun `SyncerConfig with different entityType have different values`() {
        // Given
        val config1 = createTestConfig(entityType = "medication")
        val config2 = createTestConfig(entityType = "note")

        // Then
        assertNotEquals(config1.entityType, config2.entityType)
    }

    // ========== カテゴリ B: ConfigDrivenEntitySyncer 委譲テスト ==========

    @Test
    fun `ConfigDrivenEntitySyncer returns entityType from config`() {
        // Given
        val syncer = createSyncer()

        // Then
        assertEquals("test_entity", syncer.entityType)
    }

    @Test
    fun `ConfigDrivenEntitySyncer delegates collectionPath to config`() {
        // Given
        val syncer = createSyncer()

        // When
        val path = syncer.collectionPath("test-cr")

        // Then
        assertEquals("careRecipients/test-cr/test_entities", path)
    }

    @Test
    fun `ConfigDrivenEntitySyncer delegates getAllLocal to config`() = runTest {
        // Given
        localStorage[1L] = TestEntity(id = 1, name = "Entity1", updatedAt = now)
        localStorage[2L] = TestEntity(id = 2, name = "Entity2", updatedAt = now)
        val syncer = createSyncer()

        // When
        val result = syncer.getAllLocal()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `ConfigDrivenEntitySyncer delegates getLocalById to config`() = runTest {
        // Given
        localStorage[1L] = TestEntity(id = 1, name = "Entity1", updatedAt = now)
        val syncer = createSyncer()

        // When / Then: 存在するエンティティ
        val found = syncer.getLocalById(1)
        assertNotNull(found)
        assertEquals("Entity1", found!!.name)

        // When / Then: 存在しないエンティティ
        val notFound = syncer.getLocalById(999)
        assertNull(notFound)
    }

    @Test
    fun `ConfigDrivenEntitySyncer delegates entity mapper methods to config`() {
        // Given
        val syncer = createSyncer()
        val entity = TestEntity(id = 1, name = "TestName", updatedAt = now)

        // When: entityToDomain
        val domain = syncer.entityToDomain(entity)

        // Then
        assertEquals(1L, domain.id)
        assertEquals("TestName", domain.name)
        assertEquals(now, domain.updatedAt)

        // When: domainToEntity (ラウンドトリップ)
        val backToEntity = syncer.domainToEntity(domain)

        // Then
        assertEquals(entity, backToEntity)
    }

    @Test
    fun `ConfigDrivenEntitySyncer delegates remote mapper methods to config`() {
        // Given
        val syncer = createSyncer()
        val domain = TestDomain(id = 5, name = "RemoteTest", updatedAt = now)
        val syncMetadata = SyncMetadata(localId = 5, syncedAt = now)

        // When: domainToRemote
        val remoteData = syncer.domainToRemote(domain, syncMetadata)

        // Then
        assertEquals("RemoteTest", remoteData["name"])
        assertEquals(5L, remoteData["localId"])

        // When: remoteToDomain
        val backToDomain = syncer.remoteToDomain(remoteData)

        // Then
        assertEquals(5L, backToDomain.id)
        assertEquals("RemoteTest", backToDomain.name)
        assertEquals(now, backToDomain.updatedAt)

        // When: extractSyncMetadata
        val metadata = syncer.extractSyncMetadata(remoteData)

        // Then
        assertEquals(5L, metadata.localId)
        assertEquals(now, metadata.syncedAt)
        assertNull(metadata.deletedAt)
    }

    @Test
    fun `ConfigDrivenEntitySyncer delegates getLocalId and getUpdatedAt to config`() {
        // Given
        val syncer = createSyncer()
        val entity = TestEntity(id = 42, name = "Test", updatedAt = now)

        // When / Then
        assertEquals(42L, syncer.getLocalId(entity))
        assertEquals(now, syncer.getUpdatedAt(entity))
    }

    // ========== カテゴリ C: 統合テスト ==========

    @Test
    fun `ConfigDrivenEntitySyncer delegates saveLocal to config`() = runTest {
        // Given
        val syncer = createSyncer()
        val entity = TestEntity(id = 0, name = "NewEntity", updatedAt = now)

        // When: id=0 のエンティティを保存（自動採番）
        val savedId = syncer.saveLocal(entity)

        // Then
        assertEquals(100L, savedId) // nextId の初期値
        assertEquals(1, localStorage.size)
        assertEquals("NewEntity", localStorage[100L]?.name)
    }

    @Test
    fun `ConfigDrivenEntitySyncer delegates deleteLocal to config`() = runTest {
        // Given
        localStorage[1L] = TestEntity(id = 1, name = "ToDelete", updatedAt = now)
        val syncer = createSyncer()
        assertEquals(1, localStorage.size)

        // When
        syncer.deleteLocal(1L)

        // Then
        assertEquals(0, localStorage.size)
    }
}
