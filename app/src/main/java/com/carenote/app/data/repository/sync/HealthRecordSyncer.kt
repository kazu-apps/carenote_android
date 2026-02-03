package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.entity.HealthRecordEntity
import com.carenote.app.data.mapper.HealthRecordMapper
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.mapper.remote.HealthRecordRemoteMapper
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.HealthRecord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HealthRecord エンティティの同期処理を担当
 */
@Singleton
class HealthRecordSyncer @Inject constructor(
    firestore: FirebaseFirestore,
    syncMappingDao: SyncMappingDao,
    timestampConverter: FirestoreTimestampConverter,
    private val healthRecordDao: HealthRecordDao,
    private val entityMapper: HealthRecordMapper,
    private val remoteMapper: HealthRecordRemoteMapper
) : EntitySyncer<HealthRecordEntity, HealthRecord>(
    firestore,
    syncMappingDao,
    timestampConverter
) {

    override val entityType: String = "healthRecord"

    override fun collectionPath(careRecipientId: String): String =
        "careRecipients/$careRecipientId/healthRecords"

    override suspend fun getAllLocal(): List<HealthRecordEntity> =
        healthRecordDao.getAllRecords().first()

    override suspend fun getLocalById(id: Long): HealthRecordEntity? =
        healthRecordDao.getRecordById(id).first()

    override suspend fun saveLocal(entity: HealthRecordEntity): Long =
        healthRecordDao.insertRecord(entity)

    override suspend fun deleteLocal(id: Long) =
        healthRecordDao.deleteRecord(id)

    override fun entityToDomain(entity: HealthRecordEntity): HealthRecord =
        entityMapper.toDomain(entity)

    override fun domainToEntity(domain: HealthRecord): HealthRecordEntity =
        entityMapper.toEntity(domain)

    override fun domainToRemote(
        domain: HealthRecord,
        syncMetadata: SyncMetadata
    ): Map<String, Any?> = remoteMapper.toRemote(domain, syncMetadata)

    override fun remoteToDomain(data: Map<String, Any?>): HealthRecord =
        remoteMapper.toDomain(data)

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata =
        remoteMapper.extractSyncMetadata(data)

    override fun getLocalId(entity: HealthRecordEntity): Long = entity.id

    override fun getUpdatedAt(entity: HealthRecordEntity): LocalDateTime =
        LocalDateTime.parse(entity.updatedAt)
}
