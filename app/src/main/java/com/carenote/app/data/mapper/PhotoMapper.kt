package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.PhotoEntity
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.model.PhotoUploadStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoMapper @Inject constructor() : Mapper<PhotoEntity, Photo> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: PhotoEntity): Photo {
        return Photo(
            id = entity.id,
            careRecipientId = entity.careRecipientId,
            parentType = entity.parentType,
            parentId = entity.parentId,
            localUri = entity.localUri,
            remoteUrl = entity.remoteUrl,
            uploadStatus = parseStatus(entity.uploadStatus),
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: Photo): PhotoEntity {
        return PhotoEntity(
            id = domain.id,
            careRecipientId = domain.careRecipientId,
            parentType = domain.parentType,
            parentId = domain.parentId,
            localUri = domain.localUri,
            remoteUrl = domain.remoteUrl,
            uploadStatus = domain.uploadStatus.name,
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter)
        )
    }

    private fun parseStatus(value: String): PhotoUploadStatus {
        return try {
            PhotoUploadStatus.valueOf(value)
        } catch (_: IllegalArgumentException) {
            PhotoUploadStatus.PENDING
        }
    }
}
