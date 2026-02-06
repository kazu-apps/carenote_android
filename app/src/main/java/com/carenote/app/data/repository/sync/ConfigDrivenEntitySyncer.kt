package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.remote.model.SyncMetadata
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import dagger.Lazy as DaggerLazy

/**
 * SyncerConfig に基づいて EntitySyncer の全抽象メソッドを委譲するクラス
 *
 * 標準的な Syncer（DAO / EntityMapper / RemoteMapper への純粋な委譲のみ）を
 * SyncerConfig で構成することで、ボイラープレートクラスの作成を不要にする。
 *
 * @param Entity Room Entity 型
 * @param Domain Domain Model 型
 */
class ConfigDrivenEntitySyncer<Entity, Domain>(
    private val config: SyncerConfig<Entity, Domain>,
    firestore: DaggerLazy<FirebaseFirestore>,
    syncMappingDao: SyncMappingDao,
    timestampConverter: FirestoreTimestampConverter
) : EntitySyncer<Entity, Domain>(firestore, syncMappingDao, timestampConverter) {

    override val entityType: String = config.entityType

    override fun collectionPath(careRecipientId: String): String =
        config.collectionPath(careRecipientId)

    override suspend fun getAllLocal(): List<Entity> =
        config.getAllLocal()

    override suspend fun getLocalById(id: Long): Entity? =
        config.getLocalById(id)

    override suspend fun saveLocal(entity: Entity): Long =
        config.saveLocal(entity)

    override suspend fun deleteLocal(id: Long) =
        config.deleteLocal(id)

    override fun entityToDomain(entity: Entity): Domain =
        config.entityToDomain(entity)

    override fun domainToEntity(domain: Domain): Entity =
        config.domainToEntity(domain)

    override fun domainToRemote(domain: Domain, syncMetadata: SyncMetadata): Map<String, Any?> =
        config.domainToRemote(domain, syncMetadata)

    override fun remoteToDomain(data: Map<String, Any?>): Domain =
        config.remoteToDomain(data)

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata =
        config.extractSyncMetadata(data)

    override fun getLocalId(entity: Entity): Long =
        config.getLocalId(entity)

    override fun getUpdatedAt(entity: Entity): LocalDateTime =
        config.getUpdatedAt(entity)
}
