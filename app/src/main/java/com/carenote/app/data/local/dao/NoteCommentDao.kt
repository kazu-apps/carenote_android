package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.NoteCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteCommentDao {

    @Query("SELECT * FROM note_comments WHERE note_id = :noteId ORDER BY created_at ASC")
    fun getCommentsForNote(noteId: Long): Flow<List<NoteCommentEntity>>

    @Query("SELECT * FROM note_comments WHERE care_recipient_id = :careRecipientId ORDER BY created_at DESC")
    fun getAllComments(careRecipientId: Long): Flow<List<NoteCommentEntity>>

    @Query("SELECT * FROM note_comments WHERE id = :id")
    fun getCommentById(id: Long): Flow<NoteCommentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: NoteCommentEntity): Long

    @Update
    suspend fun updateComment(comment: NoteCommentEntity)

    @Query("DELETE FROM note_comments WHERE id = :id")
    suspend fun deleteComment(id: Long)

    @Query("DELETE FROM note_comments WHERE note_id = :noteId")
    suspend fun deleteCommentsForNote(noteId: Long)

    @Query("SELECT * FROM note_comments WHERE updated_at > :lastSyncTime")
    suspend fun getModifiedSince(lastSyncTime: String): List<NoteCommentEntity>
}
