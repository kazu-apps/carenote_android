package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<Note>>

    fun getNoteById(id: Long): Flow<Note?>

    fun searchNotes(query: String, tag: NoteTag?): Flow<List<Note>>

    suspend fun insertNote(note: Note): Result<Long, DomainError>

    suspend fun updateNote(note: Note): Result<Unit, DomainError>

    suspend fun deleteNote(id: Long): Result<Unit, DomainError>
}
