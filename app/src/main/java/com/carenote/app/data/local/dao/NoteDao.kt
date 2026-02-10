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

    @Query("SELECT * FROM notes ORDER BY created_at DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<NoteEntity?>

    @Query(
        "SELECT * FROM notes " +
            "WHERE title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%' " +
            "ORDER BY created_at DESC"
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE tag = :tag ORDER BY created_at DESC")
    fun getNotesByTag(tag: String): Flow<List<NoteEntity>>

    @Query(
        "SELECT * FROM notes " +
            "WHERE (title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "AND tag = :tag " +
            "ORDER BY created_at DESC"
    )
    fun searchNotesByTag(
        query: String,
        tag: String
    ): Flow<List<NoteEntity>>

    @Query(
        "SELECT * FROM notes " +
            "WHERE (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "ORDER BY created_at DESC"
    )
    fun getPagedNotes(query: String): PagingSource<Int, NoteEntity>

    @Query(
        "SELECT * FROM notes " +
            "WHERE tag = :tag " +
            "AND (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "ORDER BY created_at DESC"
    )
    fun getPagedNotesByTag(query: String, tag: String): PagingSource<Int, NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("SELECT * FROM notes WHERE updated_at > :lastSyncTime")
    suspend fun getModifiedSince(lastSyncTime: String): List<NoteEntity>
}
