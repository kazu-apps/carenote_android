package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val mapper: NoteMapper
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getNoteById(id: Long): Flow<Note?> {
        return noteDao.getNoteById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun searchNotes(query: String, tag: NoteTag?): Flow<List<Note>> {
        val flow = when {
            query.isBlank() && tag == null -> noteDao.getAllNotes()
            query.isBlank() && tag != null -> noteDao.getNotesByTag(tag.name)
            query.isNotBlank() && tag == null -> noteDao.searchNotes(query)
            else -> noteDao.searchNotesByTag(query, tag!!.name)
        }
        return flow.map { entities -> mapper.toDomainList(entities) }
    }

    override suspend fun insertNote(note: Note): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert note", it) }
        ) {
            noteDao.insertNote(mapper.toEntity(note))
        }
    }

    override suspend fun updateNote(note: Note): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update note", it) }
        ) {
            noteDao.updateNote(mapper.toEntity(note))
        }
    }

    override suspend fun deleteNote(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete note", it) }
        ) {
            noteDao.deleteNote(id)
        }
    }
}
