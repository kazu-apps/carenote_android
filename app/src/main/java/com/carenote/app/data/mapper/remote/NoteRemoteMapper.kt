package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Note の Firestore Document ↔ Domain Model 変換マッパー
 *
 * Note: authorName は非正規化フィールドとして Firestore に保存されるが、
 * Domain Model には含まれない。authorName の追加は Repository 層の責務。
 */
@Singleton
class NoteRemoteMapper @Inject constructor(
    private val timestampConverter: FirestoreTimestampConverter
) : RemoteMapper<Note> {

    override fun toDomain(data: Map<String, Any?>): Note {
        val localId = (data["localId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("localId is required")
        val title = data["title"] as? String
            ?: throw IllegalArgumentException("title is required")
        val content = data["content"] as? String
            ?: throw IllegalArgumentException("content is required")
        val createdAt = data["createdAt"]
            ?: throw IllegalArgumentException("createdAt is required")
        val updatedAt = data["updatedAt"]
            ?: throw IllegalArgumentException("updatedAt is required")

        val tag = parseTag(data["tag"] as? String)

        return Note(
            id = localId,
            title = title,
            content = content,
            tag = tag,
            createdBy = data["createdBy"] as? String ?: data["authorId"] as? String ?: "",
            createdAt = timestampConverter.toLocalDateTimeFromAny(createdAt),
            updatedAt = timestampConverter.toLocalDateTimeFromAny(updatedAt)
        )
    }

    /**
     * Domain Model を Firestore データに変換
     *
     * @param domain Domain Model
     * @param syncMetadata 同期メタデータ
     * @param authorName 作成者名（非正規化用）。null の場合は authorName フィールドを追加しない
     */
    fun toRemote(
        domain: Note,
        syncMetadata: SyncMetadata?,
        authorName: String?
    ): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "localId" to domain.id,
            "title" to domain.title,
            "content" to domain.content,
            "tag" to domain.tag.name,
            "createdBy" to domain.createdBy,
            "createdAt" to timestampConverter.toTimestamp(domain.createdAt),
            "updatedAt" to timestampConverter.toTimestamp(domain.updatedAt)
        )

        authorName?.let {
            result["authorName"] = it
        }

        syncMetadata?.let { metadata ->
            result["syncedAt"] = timestampConverter.toTimestamp(metadata.syncedAt)
            result["deletedAt"] = metadata.deletedAt?.let {
                timestampConverter.toTimestamp(it)
            }
        }

        return result
    }

    override fun toRemote(domain: Note, syncMetadata: SyncMetadata?): Map<String, Any?> {
        return toRemote(domain, syncMetadata, authorName = null)
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

    private fun parseTag(value: String?): NoteTag {
        if (value == null) return NoteTag.OTHER
        return try {
            NoteTag.valueOf(value)
        } catch (_: IllegalArgumentException) {
            NoteTag.OTHER
        }
    }
}
