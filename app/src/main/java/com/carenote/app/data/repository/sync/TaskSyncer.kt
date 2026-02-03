package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.dao.TaskDao
import com.carenote.app.data.local.entity.TaskEntity
import com.carenote.app.data.mapper.TaskMapper
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.mapper.remote.TaskRemoteMapper
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Task エンティティの同期処理を担当
 */
@Singleton
class TaskSyncer @Inject constructor(
    firestore: FirebaseFirestore,
    syncMappingDao: SyncMappingDao,
    timestampConverter: FirestoreTimestampConverter,
    private val taskDao: TaskDao,
    private val entityMapper: TaskMapper,
    private val remoteMapper: TaskRemoteMapper
) : EntitySyncer<TaskEntity, Task>(firestore, syncMappingDao, timestampConverter) {

    override val entityType: String = "task"

    override fun collectionPath(careRecipientId: String): String =
        "careRecipients/$careRecipientId/tasks"

    override suspend fun getAllLocal(): List<TaskEntity> =
        taskDao.getAllTasks().first()

    override suspend fun getLocalById(id: Long): TaskEntity? =
        taskDao.getTaskById(id).first()

    override suspend fun saveLocal(entity: TaskEntity): Long =
        taskDao.insertTask(entity)

    override suspend fun deleteLocal(id: Long) =
        taskDao.deleteTask(id)

    override fun entityToDomain(entity: TaskEntity): Task =
        entityMapper.toDomain(entity)

    override fun domainToEntity(domain: Task): TaskEntity =
        entityMapper.toEntity(domain)

    override fun domainToRemote(domain: Task, syncMetadata: SyncMetadata): Map<String, Any?> =
        remoteMapper.toRemote(domain, syncMetadata)

    override fun remoteToDomain(data: Map<String, Any?>): Task =
        remoteMapper.toDomain(data)

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata =
        remoteMapper.extractSyncMetadata(data)

    override fun getLocalId(entity: TaskEntity): Long = entity.id

    override fun getUpdatedAt(entity: TaskEntity): LocalDateTime =
        LocalDateTime.parse(entity.updatedAt)
}
