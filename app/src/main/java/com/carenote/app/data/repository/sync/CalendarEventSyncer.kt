package com.carenote.app.data.repository.sync

import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.local.entity.CalendarEventEntity
import com.carenote.app.data.mapper.CalendarEventMapper
import com.carenote.app.data.mapper.remote.CalendarEventRemoteMapper
import com.carenote.app.data.mapper.remote.FirestoreTimestampConverter
import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.CalendarEvent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CalendarEvent エンティティの同期処理を担当
 */
@Singleton
class CalendarEventSyncer @Inject constructor(
    firestore: FirebaseFirestore,
    syncMappingDao: SyncMappingDao,
    timestampConverter: FirestoreTimestampConverter,
    private val calendarEventDao: CalendarEventDao,
    private val entityMapper: CalendarEventMapper,
    private val remoteMapper: CalendarEventRemoteMapper
) : EntitySyncer<CalendarEventEntity, CalendarEvent>(
    firestore,
    syncMappingDao,
    timestampConverter
) {

    override val entityType: String = "calendarEvent"

    override fun collectionPath(careRecipientId: String): String =
        "careRecipients/$careRecipientId/calendarEvents"

    override suspend fun getAllLocal(): List<CalendarEventEntity> =
        calendarEventDao.getAllEvents().first()

    override suspend fun getLocalById(id: Long): CalendarEventEntity? =
        calendarEventDao.getEventById(id).first()

    override suspend fun saveLocal(entity: CalendarEventEntity): Long =
        calendarEventDao.insertEvent(entity)

    override suspend fun deleteLocal(id: Long) =
        calendarEventDao.deleteEvent(id)

    override fun entityToDomain(entity: CalendarEventEntity): CalendarEvent =
        entityMapper.toDomain(entity)

    override fun domainToEntity(domain: CalendarEvent): CalendarEventEntity =
        entityMapper.toEntity(domain)

    override fun domainToRemote(
        domain: CalendarEvent,
        syncMetadata: SyncMetadata
    ): Map<String, Any?> = remoteMapper.toRemote(domain, syncMetadata)

    override fun remoteToDomain(data: Map<String, Any?>): CalendarEvent =
        remoteMapper.toDomain(data)

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata =
        remoteMapper.extractSyncMetadata(data)

    override fun getLocalId(entity: CalendarEventEntity): Long = entity.id

    override fun getUpdatedAt(entity: CalendarEventEntity): LocalDateTime =
        LocalDateTime.parse(entity.updatedAt)
}
