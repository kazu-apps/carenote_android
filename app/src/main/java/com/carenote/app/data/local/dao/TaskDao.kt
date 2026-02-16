package com.carenote.app.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query(
        "SELECT * FROM tasks " +
            "WHERE care_recipient_id = :careRecipientId " +
            "ORDER BY is_completed ASC, created_at DESC"
    )
    fun getAllTasks(careRecipientId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<TaskEntity?>

    @Query(
        "SELECT * FROM tasks " +
            "WHERE care_recipient_id = :careRecipientId " +
            "AND is_completed = 0 " +
            "ORDER BY created_at DESC"
    )
    fun getIncompleteTasks(careRecipientId: Long): Flow<List<TaskEntity>>

    @Query(
        "SELECT * FROM tasks " +
            "WHERE care_recipient_id = :careRecipientId " +
            "AND due_date = :date " +
            "ORDER BY is_completed ASC, created_at DESC"
    )
    fun getTasksByDueDate(
        date: String,
        careRecipientId: Long
    ): Flow<List<TaskEntity>>

    @Query(
        "SELECT * FROM tasks " +
            "WHERE care_recipient_id = :careRecipientId " +
            "AND (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR description LIKE '%' || :query || '%') " +
            "ORDER BY is_completed ASC, created_at DESC"
    )
    fun getPagedAllTasks(
        query: String,
        careRecipientId: Long
    ): PagingSource<Int, TaskEntity>

    @Query(
        "SELECT * FROM tasks " +
            "WHERE care_recipient_id = :careRecipientId " +
            "AND is_completed = 0 " +
            "AND (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR description LIKE '%' || :query || '%') " +
            "ORDER BY created_at DESC"
    )
    fun getPagedIncompleteTasks(
        query: String,
        careRecipientId: Long
    ): PagingSource<Int, TaskEntity>

    @Query(
        "SELECT * FROM tasks " +
            "WHERE care_recipient_id = :careRecipientId " +
            "AND is_completed = 1 " +
            "AND (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR description LIKE '%' || :query || '%') " +
            "ORDER BY created_at DESC"
    )
    fun getPagedCompletedTasks(
        query: String,
        careRecipientId: Long
    ): PagingSource<Int, TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE care_recipient_id = :careRecipientId AND is_completed = 0")
    fun getIncompleteTaskCount(careRecipientId: Long): Flow<Int>

    @Query("SELECT * FROM tasks WHERE updated_at > :lastSyncTime")
    suspend fun getModifiedSince(lastSyncTime: String): List<TaskEntity>
}
