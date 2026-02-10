package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos WHERE parent_type = :parentType AND parent_id = :parentId ORDER BY created_at ASC")
    fun getPhotosByParent(parentType: String, parentId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE upload_status = :status")
    suspend fun getPhotosWithStatus(status: String): List<PhotoEntity>

    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Update
    suspend fun update(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM photos WHERE parent_type = :parentType AND parent_id = :parentId")
    suspend fun deleteByParent(parentType: String, parentId: Long)

    @Query("UPDATE photos SET upload_status = :status, remote_url = :remoteUrl, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateUploadStatus(id: Long, status: String, remoteUrl: String?, updatedAt: String)

    @Query("UPDATE photos SET parent_id = :parentId, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateParentId(id: Long, parentId: Long, updatedAt: String)
}
