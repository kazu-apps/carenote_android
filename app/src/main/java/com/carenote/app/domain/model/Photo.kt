package com.carenote.app.domain.model

import java.time.LocalDateTime

data class Photo(
    val id: Long = 0,
    val parentType: String,
    val parentId: Long,
    val localUri: String,
    val remoteUrl: String? = null,
    val uploadStatus: PhotoUploadStatus = PhotoUploadStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PhotoUploadStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    FAILED
}
