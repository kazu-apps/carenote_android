package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CalendarEvent の Firestore Document ↔ Domain Model 変換マッパー
 */
@Singleton
class CalendarEventRemoteMapper @Inject constructor(
    private val timestampConverter: FirestoreTimestampConverter
) : RemoteMapper<CalendarEvent> {

    override fun toDomain(data: Map<String, Any?>): CalendarEvent {
        val localId = (data["localId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("localId is required")
        val title = data["title"] as? String
            ?: throw IllegalArgumentException("title is required")
        val dateString = data["date"] as? String
            ?: throw IllegalArgumentException("date is required")
        val createdAt = data["createdAt"]
            ?: throw IllegalArgumentException("createdAt is required")
        val updatedAt = data["updatedAt"]
            ?: throw IllegalArgumentException("updatedAt is required")

        return CalendarEvent(
            id = localId,
            title = title,
            description = data["description"] as? String ?: "",
            date = timestampConverter.toLocalDate(dateString),
            startTime = (data["startTime"] as? String)?.let {
                timestampConverter.toLocalTime(it)
            },
            endTime = (data["endTime"] as? String)?.let {
                timestampConverter.toLocalTime(it)
            },
            isAllDay = data["isAllDay"] as? Boolean ?: true,
            type = (data["type"] as? String)?.let {
                try { CalendarEventType.valueOf(it) } catch (_: IllegalArgumentException) { CalendarEventType.OTHER }
            } ?: CalendarEventType.OTHER,
            completed = data["completed"] as? Boolean ?: false,
            createdAt = timestampConverter.toLocalDateTimeFromAny(createdAt),
            updatedAt = timestampConverter.toLocalDateTimeFromAny(updatedAt)
        )
    }

    override fun toRemote(domain: CalendarEvent, syncMetadata: SyncMetadata?): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "localId" to domain.id,
            "title" to domain.title,
            "description" to domain.description,
            "date" to timestampConverter.toDateString(domain.date),
            "startTime" to domain.startTime?.let { timestampConverter.toTimeString(it) },
            "endTime" to domain.endTime?.let { timestampConverter.toTimeString(it) },
            "isAllDay" to domain.isAllDay,
            "type" to domain.type.name,
            "completed" to domain.completed,
            "createdAt" to timestampConverter.toTimestamp(domain.createdAt),
            "updatedAt" to timestampConverter.toTimestamp(domain.updatedAt)
        )

        syncMetadata?.let { metadata ->
            result["syncedAt"] = timestampConverter.toTimestamp(metadata.syncedAt)
            result["deletedAt"] = metadata.deletedAt?.let {
                timestampConverter.toTimestamp(it)
            }
        }

        return result
    }

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata {
        val localId = (data["localId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("localId is required")
        val syncedAt = data["syncedAt"]
            ?: throw IllegalArgumentException("syncedAt is required")

        return SyncMetadata(
            localId = localId,
            syncedAt = timestampConverter.toLocalDateTimeFromAny(syncedAt),
            deletedAt = timestampConverter.toLocalDateTimeFromAnyOrNull(data["deletedAt"])
        )
    }
}
