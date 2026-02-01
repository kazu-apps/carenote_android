package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.NoteEntity
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteMapper @Inject constructor() : Mapper<NoteEntity, Note> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: NoteEntity): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            tag = parseTag(entity.tag),
            authorId = entity.authorId,
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: Note): NoteEntity {
        return NoteEntity(
            id = domain.id,
            title = domain.title,
            content = domain.content,
            tag = domain.tag.name,
            authorId = domain.authorId,
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter)
        )
    }

    private fun parseTag(value: String): NoteTag {
        return try {
            NoteTag.valueOf(value)
        } catch (_: IllegalArgumentException) {
            NoteTag.OTHER
        }
    }
}
