package com.carenote.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

/**
 * メモのタグ種別
 */
enum class NoteTag {
    CONDITION,
    MEAL,
    REPORT,
    OTHER
}

/**
 * メモ・申し送りモデル
 */
@Immutable
data class Note(
    val id: Long = 0,
    val careRecipientId: Long = 0,
    val title: String,
    val content: String,
    val tag: NoteTag = NoteTag.OTHER,
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
