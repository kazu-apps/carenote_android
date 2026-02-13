package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.NoteComment
import com.carenote.app.domain.repository.NoteCommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteCommentRepository : NoteCommentRepository {

    private val comments = MutableStateFlow<List<NoteComment>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setComments(list: List<NoteComment>) {
        comments.value = list
    }

    fun clear() {
        comments.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getCommentsForNote(noteId: Long): Flow<List<NoteComment>> {
        return comments.map { list -> list.filter { it.noteId == noteId } }
    }

    override fun getAllComments(): Flow<List<NoteComment>> {
        return comments
    }

    override suspend fun insertComment(comment: NoteComment): Result<Long, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        val id = nextId++
        val newComment = comment.copy(id = id)
        comments.value = comments.value + newComment
        return Result.Success(id)
    }

    override suspend fun updateComment(comment: NoteComment): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake update error"))
        comments.value = comments.value.map {
            if (it.id == comment.id) comment else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteComment(id: Long): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        comments.value = comments.value.filter { it.id != id }
        return Result.Success(Unit)
    }

    override suspend fun deleteCommentsForNote(noteId: Long): Result<Unit, DomainError> {
        if (shouldFail) return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        comments.value = comments.value.filter { it.noteId != noteId }
        return Result.Success(Unit)
    }
}
