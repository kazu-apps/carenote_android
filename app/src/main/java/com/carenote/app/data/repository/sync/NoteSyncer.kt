package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.entity.NoteEntity
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.mapper.remote.NoteRemoteMapper
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Note エンティティの同期処理を担当
 */
@Singleton
class NoteSyncer @Inject constructor(
    firestore: FirebaseFirestore,
    syncMappingDao: SyncMappingDao,
    timestampConverter: FirestoreTimestampConverter,
    private val noteDao: NoteDao,
    private val entityMapper: NoteMapper,
    private val remoteMapper: NoteRemoteMapper
) : EntitySyncer<NoteEntity, Note>(firestore, syncMappingDao, timestampConverter) {

    override val entityType: String = "note"

    override fun collectionPath(careRecipientId: String): String =
        "careRecipients/$careRecipientId/notes"

    override suspend fun getAllLocal(): List<NoteEntity> =
        noteDao.getAllNotes().first()

    override suspend fun getLocalById(id: Long): NoteEntity? =
        noteDao.getNoteById(id).first()

    override suspend fun saveLocal(entity: NoteEntity): Long =
        noteDao.insertNote(entity)

    override suspend fun deleteLocal(id: Long) =
        noteDao.deleteNote(id)

    override fun entityToDomain(entity: NoteEntity): Note =
        entityMapper.toDomain(entity)

    override fun domainToEntity(domain: Note): NoteEntity =
        entityMapper.toEntity(domain)

    override fun domainToRemote(domain: Note, syncMetadata: SyncMetadata): Map<String, Any?> =
        remoteMapper.toRemote(domain, syncMetadata)

    override fun remoteToDomain(data: Map<String, Any?>): Note =
        remoteMapper.toDomain(data)

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata =
        remoteMapper.extractSyncMetadata(data)

    override fun getLocalId(entity: NoteEntity): Long = entity.id

    override fun getUpdatedAt(entity: NoteEntity): LocalDateTime =
        LocalDateTime.parse(entity.updatedAt)
}
