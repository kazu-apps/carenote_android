package com.carenote.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.dao.NoteDao
import com.carenote.app.data.mapper.NoteMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.PhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import timber.log.Timber
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val mapper: NoteMapper,
    private val photoRepository: PhotoRepository,
    private val activeRecipientProvider: ActiveCareRecipientProvider,
    private val authRepository: AuthRepository
) : NoteRepository {

    private val pagingConfig = PagingConfig(pageSize = AppConfig.Paging.PAGE_SIZE)
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun getAllNotes(): Flow<List<Note>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            noteDao.getAllNotes(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getNotesByDate(date: LocalDate): Flow<List<Note>> {
        val startOfDay = date.atStartOfDay().format(dateTimeFormatter)
        val startOfNextDay = date.plusDays(1).atStartOfDay().format(dateTimeFormatter)
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            noteDao.getNotesByDateRange(startOfDay, startOfNextDay, recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getNoteById(id: Long): Flow<Note?> {
        return noteDao.getNoteById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun searchNotes(query: String, tag: NoteTag?): Flow<List<Note>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            when {
                query.isBlank() && tag == null -> noteDao.getAllNotes(recipientId)
                query.isBlank() && tag != null -> noteDao.getNotesByTag(tag.name, recipientId)
                query.isNotBlank() && tag == null -> noteDao.searchNotes(query, recipientId)
                else -> noteDao.searchNotesByTag(query, tag!!.name, recipientId)
            }
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun searchPagedNotes(query: String, tag: NoteTag?): Flow<PagingData<Note>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            Pager(pagingConfig) {
                when (tag) {
                    null -> noteDao.getPagedNotes(query, recipientId)
                    else -> noteDao.getPagedNotesByTag(query, tag.name, recipientId)
                }
            }.flow.map { pagingData -> pagingData.map { mapper.toDomain(it) } }
        }
    }

    override suspend fun insertNote(note: Note): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert note", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            val createdBy = authRepository.getCurrentUser()?.uid ?: ""
            noteDao.insertNote(mapper.toEntity(note).copy(careRecipientId = recipientId, createdBy = createdBy))
        }
    }

    override suspend fun updateNote(note: Note): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update note", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            noteDao.updateNote(mapper.toEntity(note).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun deleteNote(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete note", it) }
        ) {
            photoRepository.deletePhotosForParent("note", id)
                .onFailure { error -> Timber.w("Failed to delete photos for note $id: $error") }
            noteDao.deleteNote(id)
        }
    }
}
