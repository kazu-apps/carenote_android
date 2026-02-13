package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.NoteCommentDao
import com.carenote.app.data.mapper.NoteCommentMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.NoteComment
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.NoteCommentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class NoteCommentRepositoryImpl @Inject constructor(
    private val noteCommentDao: NoteCommentDao,
    private val mapper: NoteCommentMapper,
    private val activeRecipientProvider: ActiveCareRecipientProvider,
    private val authRepository: AuthRepository
) : NoteCommentRepository {

    override fun getCommentsForNote(noteId: Long): Flow<List<NoteComment>> {
        return noteCommentDao.getCommentsForNote(noteId).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getAllComments(): Flow<List<NoteComment>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            noteCommentDao.getAllComments(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override suspend fun insertComment(comment: NoteComment): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert note comment", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            val createdBy = authRepository.getCurrentUser()?.uid ?: ""
            noteCommentDao.insertComment(
                mapper.toEntity(comment).copy(
                    careRecipientId = recipientId,
                    createdBy = createdBy
                )
            )
        }
    }

    override suspend fun updateComment(comment: NoteComment): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update note comment", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            noteCommentDao.updateComment(mapper.toEntity(comment).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun deleteComment(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete note comment", it) }
        ) {
            noteCommentDao.deleteComment(id)
        }
    }

    override suspend fun deleteCommentsForNote(noteId: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete note comments", it) }
        ) {
            noteCommentDao.deleteCommentsForNote(noteId)
        }
    }
}
