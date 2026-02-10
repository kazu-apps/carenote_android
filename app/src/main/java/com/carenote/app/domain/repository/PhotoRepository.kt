package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.model.PhotoUploadStatus
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    fun getPhotosForParent(parentType: String, parentId: Long): Flow<List<Photo>>

    suspend fun addPhoto(photo: Photo): Result<Long, DomainError>

    suspend fun deletePhoto(id: Long): Result<Unit, DomainError>

    suspend fun deletePhotosForParent(parentType: String, parentId: Long): Result<Unit, DomainError>

    suspend fun getPendingPhotos(): Result<List<Photo>, DomainError>

    suspend fun updateUploadStatus(
        id: Long,
        status: PhotoUploadStatus,
        remoteUrl: String? = null
    ): Result<Unit, DomainError>

    suspend fun updatePhotosParentId(
        photoIds: List<Long>,
        newParentId: Long
    ): Result<Unit, DomainError>
}
