package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Task の Firestore Document ↔ Domain Model 変換マッパー
 */
@Singleton
class TaskRemoteMapper @Inject constructor(
    private val timestampConverter: FirestoreTimestampConverter
) : RemoteMapper<Task> {

    override fun toDomain(data: Map<String, Any?>): Task {
        val localId = (data["localId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("localId is required")
        val title = data["title"] as? String
            ?: throw IllegalArgumentException("title is required")
        val createdAt = data["createdAt"]
            ?: throw IllegalArgumentException("createdAt is required")
        val updatedAt = data["updatedAt"]
            ?: throw IllegalArgumentException("updatedAt is required")

        return Task(
            id = localId,
            title = title,
            description = data["description"] as? String ?: "",
            dueDate = (data["dueDate"] as? String)?.let {
                timestampConverter.toLocalDate(it)
            },
            isCompleted = data["isCompleted"] as? Boolean ?: false,
            priority = parsePriority(data["priority"] as? String),
            createdAt = timestampConverter.toLocalDateTimeFromAny(createdAt),
            updatedAt = timestampConverter.toLocalDateTimeFromAny(updatedAt)
        )
    }

    override fun toRemote(domain: Task, syncMetadata: SyncMetadata?): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "localId" to domain.id,
            "title" to domain.title,
            "description" to domain.description,
            "dueDate" to domain.dueDate?.let { timestampConverter.toDateString(it) },
            "isCompleted" to domain.isCompleted,
            "priority" to domain.priority.name,
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

    private fun parsePriority(value: String?): TaskPriority {
        if (value == null) return TaskPriority.MEDIUM
        return try {
            TaskPriority.valueOf(value)
        } catch (_: IllegalArgumentException) {
            TaskPriority.MEDIUM
        }
    }
}
