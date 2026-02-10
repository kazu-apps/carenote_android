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

    @Query("SELECT * FROM tasks ORDER BY is_completed ASC, created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY created_at DESC")
    fun getIncompleteTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE due_date = :date ORDER BY is_completed ASC, created_at DESC")
    fun getTasksByDueDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE (:query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY is_completed ASC, created_at DESC")
    fun getPagedAllTasks(query: String): PagingSource<Int, TaskEntity>

    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND (:query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY created_at DESC")
    fun getPagedIncompleteTasks(query: String): PagingSource<Int, TaskEntity>

    @Query("SELECT * FROM tasks WHERE is_completed = 1 AND (:query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY created_at DESC")
    fun getPagedCompletedTasks(query: String): PagingSource<Int, TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0")
    fun getIncompleteTaskCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE updated_at > :lastSyncTime")
    suspend fun getModifiedSince(lastSyncTime: String): List<TaskEntity>
}
