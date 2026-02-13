package com.carenote.app.domain.model

import java.time.LocalDateTime

data class NoteComment(
    val id: Long = 0,
    val careRecipientId: Long = 0,
    val noteId: Long,
    val content: String,
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
