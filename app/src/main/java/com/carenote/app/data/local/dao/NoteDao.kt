package com.carenote.app.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE care_recipient_id = :careRecipientId ORDER BY created_at DESC")
    fun getAllNotes(careRecipientId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<NoteEntity?>

    @Query(
        "SELECT * FROM notes " +
            "WHERE care_recipient_id = :careRecipientId AND (title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "ORDER BY created_at DESC"
    )
    fun searchNotes(query: String, careRecipientId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE care_recipient_id = :careRecipientId AND tag = :tag ORDER BY created_at DESC")
    fun getNotesByTag(tag: String, careRecipientId: Long): Flow<List<NoteEntity>>

    @Query(
        "SELECT * FROM notes " +
            "WHERE care_recipient_id = :careRecipientId AND (title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "AND tag = :tag " +
            "ORDER BY created_at DESC"
    )
    fun searchNotesByTag(
        query: String,
        tag: String,
        careRecipientId: Long
    ): Flow<List<NoteEntity>>

    @Query(
        "SELECT * FROM notes " +
            "WHERE care_recipient_id = :careRecipientId AND (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "ORDER BY created_at DESC"
    )
    fun getPagedNotes(query: String, careRecipientId: Long): PagingSource<Int, NoteEntity>

    @Query(
        "SELECT * FROM notes " +
            "WHERE care_recipient_id = :careRecipientId AND tag = :tag " +
            "AND (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "ORDER BY created_at DESC"
    )
    fun getPagedNotesByTag(query: String, tag: String, careRecipientId: Long): PagingSource<Int, NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query(
        "SELECT * FROM notes " +
            "WHERE care_recipient_id = :careRecipientId " +
            "AND created_at >= :startOfDay " +
            "AND created_at < :startOfNextDay " +
            "ORDER BY created_at DESC"
    )
    fun getNotesByDateRange(
        startOfDay: String,
        startOfNextDay: String,
        careRecipientId: Long
    ): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE updated_at > :lastSyncTime")
    suspend fun getModifiedSince(lastSyncTime: String): List<NoteEntity>
}
