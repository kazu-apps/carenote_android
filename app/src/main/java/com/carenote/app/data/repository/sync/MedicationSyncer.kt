package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.entity.MedicationEntity
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.mapper.remote.MedicationRemoteMapper
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Medication
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Medication エンティティの同期処理を担当
 */
@Singleton
class MedicationSyncer @Inject constructor(
    firestore: FirebaseFirestore,
    syncMappingDao: SyncMappingDao,
    timestampConverter: FirestoreTimestampConverter,
    private val medicationDao: MedicationDao,
    private val entityMapper: MedicationMapper,
    private val remoteMapper: MedicationRemoteMapper
) : EntitySyncer<MedicationEntity, Medication>(firestore, syncMappingDao, timestampConverter) {

    override val entityType: String = "medication"

    override fun collectionPath(careRecipientId: String): String =
        "careRecipients/$careRecipientId/medications"

    override suspend fun getAllLocal(): List<MedicationEntity> =
        medicationDao.getAllMedications().first()

    override suspend fun getLocalById(id: Long): MedicationEntity? =
        medicationDao.getMedicationById(id).first()

    override suspend fun saveLocal(entity: MedicationEntity): Long =
        medicationDao.insertMedication(entity)

    override suspend fun deleteLocal(id: Long) =
        medicationDao.deleteMedication(id)

    override fun entityToDomain(entity: MedicationEntity): Medication =
        entityMapper.toDomain(entity)

    override fun domainToEntity(domain: Medication): MedicationEntity =
        entityMapper.toEntity(domain)

    override fun domainToRemote(domain: Medication, syncMetadata: SyncMetadata): Map<String, Any?> =
        remoteMapper.toRemote(domain, syncMetadata)

    override fun remoteToDomain(data: Map<String, Any?>): Medication =
        remoteMapper.toDomain(data)

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata =
        remoteMapper.extractSyncMetadata(data)

    override fun getLocalId(entity: MedicationEntity): Long = entity.id

    override fun getUpdatedAt(entity: MedicationEntity): LocalDateTime =
        LocalDateTime.parse(entity.updatedAt)
}
