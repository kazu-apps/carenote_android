package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.PhotoDao
import com.carenote.app.data.mapper.PhotoMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.model.PhotoUploadStatus
import com.carenote.app.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao,
    private val mapper: PhotoMapper
) : PhotoRepository {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun getPhotosForParent(parentType: String, parentId: Long): Flow<List<Photo>> {
        return photoDao.getPhotosByParent(parentType, parentId).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun addPhoto(photo: Photo): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to add photo", it) }
        ) {
            photoDao.insert(mapper.toEntity(photo))
        }
    }

    override suspend fun deletePhoto(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete photo", it) }
        ) {
            photoDao.deleteById(id)
        }
    }

    override suspend fun deletePhotosForParent(
        parentType: String,
        parentId: Long
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete photos for parent", it) }
        ) {
            photoDao.deleteByParent(parentType, parentId)
        }
    }

    override suspend fun getPendingPhotos(): Result<List<Photo>, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to get pending photos", it) }
        ) {
            photoDao.getPhotosWithStatus(PhotoUploadStatus.PENDING.name)
                .map { mapper.toDomain(it) }
        }
    }

    override suspend fun updateUploadStatus(
        id: Long,
        status: PhotoUploadStatus,
        remoteUrl: String?
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update photo upload status", it) }
        ) {
            photoDao.updateUploadStatus(
                id = id,
                status = status.name,
                remoteUrl = remoteUrl,
                updatedAt = LocalDateTime.now().format(dateTimeFormatter)
            )
        }
    }

    override suspend fun updatePhotosParentId(
        photoIds: List<Long>,
        newParentId: Long
    ): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update photos parent id", it) }
        ) {
            val updatedAt = LocalDateTime.now().format(dateTimeFormatter)
            for (id in photoIds) {
                photoDao.updateParentId(id, newParentId, updatedAt)
            }
        }
    }
}
