package com.carenote.app.fakes

import androidx.paging.PagingData
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeNoteRepository : NoteRepository {

    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setNotes(list: List<Note>) {
        notes.value = list
    }

    fun clear() {
        notes.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    fun currentNotes(): List<Note> = notes.value

    fun getFilteredNotes(query: String, tag: NoteTag? = null): List<Note> {
        return notes.value.filter { note ->
            val matchesQuery = query.isBlank() ||
                note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true)
            val matchesTag = tag == null || note.tag == tag
            matchesQuery && matchesTag
        }
    }

    override fun getAllNotes(): Flow<List<Note>> = notes

    override fun getNotesByDate(date: LocalDate): Flow<List<Note>> {
        return notes.map { list ->
            list.filter { it.createdAt.toLocalDate() == date }
        }
    }

    override fun getNoteById(id: Long): Flow<Note?> {
        return notes.map { list -> list.find { it.id == id } }
    }

    override fun searchNotes(query: String, tag: NoteTag?): Flow<List<Note>> {
        return notes.map { list ->
            list.filter { note ->
                val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
                val matchesTag = tag == null || note.tag == tag
                matchesQuery && matchesTag
            }
        }
    }

    override fun searchPagedNotes(query: String, tag: NoteTag?): Flow<PagingData<Note>> {
        return notes.map { list ->
            val filtered = list.filter { note ->
                val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
                val matchesTag = tag == null || note.tag == tag
                matchesQuery && matchesTag
            }
            PagingData.from(filtered)
        }
    }

    override suspend fun insertNote(note: Note): Result<Long, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        }
        val id = nextId++
        val newNote = note.copy(id = id)
        notes.value = notes.value + newNote
        return Result.Success(id)
    }

    override suspend fun updateNote(note: Note): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake update error"))
        }
        notes.value = notes.value.map {
            if (it.id == note.id) note else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteNote(id: Long): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        }
        notes.value = notes.value.filter { it.id != id }
        return Result.Success(Unit)
    }
}
