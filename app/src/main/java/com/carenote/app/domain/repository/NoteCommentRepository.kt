package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.NoteComment
import kotlinx.coroutines.flow.Flow

interface NoteCommentRepository {
    fun getCommentsForNote(noteId: Long): Flow<List<NoteComment>>
    fun getAllComments(): Flow<List<NoteComment>>
    suspend fun insertComment(comment: NoteComment): Result<Long, DomainError>
    suspend fun updateComment(comment: NoteComment): Result<Unit, DomainError>
    suspend fun deleteComment(id: Long): Result<Unit, DomainError>
    suspend fun deleteCommentsForNote(noteId: Long): Result<Unit, DomainError>
}
