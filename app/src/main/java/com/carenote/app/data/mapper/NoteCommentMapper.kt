package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.NoteCommentEntity
import com.carenote.app.domain.model.NoteComment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCommentMapper @Inject constructor() : Mapper<NoteCommentEntity, NoteComment> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: NoteCommentEntity): NoteComment {
        return NoteComment(
            id = entity.id,
            careRecipientId = entity.careRecipientId,
            noteId = entity.noteId,
            content = entity.content,
            createdBy = entity.createdBy,
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: NoteComment): NoteCommentEntity {
        return NoteCommentEntity(
            id = domain.id,
            careRecipientId = domain.careRecipientId,
            noteId = domain.noteId,
            content = domain.content,
            createdBy = domain.createdBy,
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter)
        )
    }
}
