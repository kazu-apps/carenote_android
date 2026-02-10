package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.PhotoEntity
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.model.PhotoUploadStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PhotoMapperTest {

    private lateinit var mapper: PhotoMapper
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Before
    fun setup() {
        mapper = PhotoMapper()
    }

    @Test
    fun `toDomain maps entity to domain correctly`() {
        val now = LocalDateTime.of(2026, 2, 8, 10, 0, 0)
        val entity = PhotoEntity(
            id = 1,
            parentType = "health_record",
            parentId = 42,
            localUri = "file:///cache/photo.jpg",
            remoteUrl = "https://storage.example.com/photo.jpg",
            uploadStatus = "UPLOADED",
            createdAt = now.format(dateTimeFormatter),
            updatedAt = now.format(dateTimeFormatter)
        )

        val domain = mapper.toDomain(entity)

        assertEquals(1L, domain.id)
        assertEquals("health_record", domain.parentType)
        assertEquals(42L, domain.parentId)
        assertEquals("file:///cache/photo.jpg", domain.localUri)
        assertEquals("https://storage.example.com/photo.jpg", domain.remoteUrl)
        assertEquals(PhotoUploadStatus.UPLOADED, domain.uploadStatus)
        assertEquals(now, domain.createdAt)
        assertEquals(now, domain.updatedAt)
    }

    @Test
    fun `toEntity maps domain to entity correctly`() {
        val now = LocalDateTime.of(2026, 2, 8, 10, 0, 0)
        val domain = Photo(
            id = 2,
            parentType = "note",
            parentId = 99,
            localUri = "file:///cache/photo2.jpg",
            remoteUrl = null,
            uploadStatus = PhotoUploadStatus.PENDING,
            createdAt = now,
            updatedAt = now
        )

        val entity = mapper.toEntity(domain)

        assertEquals(2L, entity.id)
        assertEquals("note", entity.parentType)
        assertEquals(99L, entity.parentId)
        assertEquals("file:///cache/photo2.jpg", entity.localUri)
        assertEquals(null, entity.remoteUrl)
        assertEquals("PENDING", entity.uploadStatus)
        assertEquals(now.format(dateTimeFormatter), entity.createdAt)
        assertEquals(now.format(dateTimeFormatter), entity.updatedAt)
    }

    @Test
    fun `toDomain maps PENDING status correctly`() {
        val entity = createEntity(uploadStatus = "PENDING")
        assertEquals(PhotoUploadStatus.PENDING, mapper.toDomain(entity).uploadStatus)
    }

    @Test
    fun `toDomain maps UPLOADING status correctly`() {
        val entity = createEntity(uploadStatus = "UPLOADING")
        assertEquals(PhotoUploadStatus.UPLOADING, mapper.toDomain(entity).uploadStatus)
    }

    @Test
    fun `toDomain maps FAILED status correctly`() {
        val entity = createEntity(uploadStatus = "FAILED")
        assertEquals(PhotoUploadStatus.FAILED, mapper.toDomain(entity).uploadStatus)
    }

    @Test
    fun `toDomain maps unknown status to PENDING`() {
        val entity = createEntity(uploadStatus = "UNKNOWN_STATUS")
        assertEquals(PhotoUploadStatus.PENDING, mapper.toDomain(entity).uploadStatus)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            createEntity(id = 1, uploadStatus = "PENDING"),
            createEntity(id = 2, uploadStatus = "UPLOADED")
        )

        val domains = mapper.toDomainList(entities)

        assertEquals(2, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(PhotoUploadStatus.PENDING, domains[0].uploadStatus)
        assertEquals(2L, domains[1].id)
        assertEquals(PhotoUploadStatus.UPLOADED, domains[1].uploadStatus)
    }

    private fun createEntity(
        id: Long = 1,
        uploadStatus: String = "PENDING"
    ): PhotoEntity {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return PhotoEntity(
            id = id,
            parentType = "health_record",
            parentId = 1,
            localUri = "file:///test.jpg",
            remoteUrl = null,
            uploadStatus = uploadStatus,
            createdAt = now,
            updatedAt = now
        )
    }
}
