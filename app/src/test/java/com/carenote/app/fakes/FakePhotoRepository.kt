package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.model.PhotoUploadStatus
import com.carenote.app.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePhotoRepository : PhotoRepository {

    private val photos = MutableStateFlow<List<Photo>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setPhotos(list: List<Photo>) {
        photos.value = list
    }

    fun getAll(): List<Photo> = photos.value

    fun clear() {
        photos.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getPhotosForParent(parentType: String, parentId: Long): Flow<List<Photo>> {
        return photos.map { list ->
            list.filter { it.parentType == parentType && it.parentId == parentId }
        }
    }

    override suspend fun addPhoto(photo: Photo): Result<Long, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake add photo error"))
        }
        val id = nextId++
        val newPhoto = photo.copy(id = id)
        photos.value = photos.value + newPhoto
        return Result.Success(id)
    }

    override suspend fun deletePhoto(id: Long): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake delete photo error"))
        }
        photos.value = photos.value.filter { it.id != id }
        return Result.Success(Unit)
    }

    override suspend fun deletePhotosForParent(
        parentType: String,
        parentId: Long
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake delete photos error"))
        }
        photos.value = photos.value.filter {
            !(it.parentType == parentType && it.parentId == parentId)
        }
        return Result.Success(Unit)
    }

    override suspend fun getPendingPhotos(): Result<List<Photo>, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake get pending error"))
        }
        return Result.Success(
            photos.value.filter { it.uploadStatus == PhotoUploadStatus.PENDING }
        )
    }

    override suspend fun updateUploadStatus(
        id: Long,
        status: PhotoUploadStatus,
        remoteUrl: String?
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake update status error"))
        }
        photos.value = photos.value.map { photo ->
            if (photo.id == id) photo.copy(uploadStatus = status, remoteUrl = remoteUrl) else photo
        }
        return Result.Success(Unit)
    }

    override suspend fun updatePhotosParentId(
        photoIds: List<Long>,
        newParentId: Long
    ): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake update parent id error"))
        }
        photos.value = photos.value.map { photo ->
            if (photo.id in photoIds) {
                photo.copy(parentId = newParentId, updatedAt = java.time.LocalDateTime.now())
            } else {
                photo
            }
        }
        return Result.Success(Unit)
    }
}
