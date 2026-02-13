package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.PhotoDao
import com.carenote.app.data.local.entity.PhotoEntity
import com.carenote.app.data.mapper.PhotoMapper
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.model.PhotoUploadStatus
import com.carenote.app.fakes.FakeActiveCareRecipientProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PhotoRepositoryImplTest {

    private lateinit var repository: PhotoRepositoryImpl
    private lateinit var photoDao: PhotoDao
    private lateinit var activeRecipientProvider: FakeActiveCareRecipientProvider
    private val mapper = PhotoMapper()
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Before
    fun setup() {
        photoDao = mockk()
        activeRecipientProvider = FakeActiveCareRecipientProvider()
        repository = PhotoRepositoryImpl(photoDao, mapper, activeRecipientProvider)
    }

    @Test
    fun `getPhotosForParent returns mapped photos`() = runTest {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        val entities = listOf(
            PhotoEntity(1, 1L, "health_record", 42, "file:///a.jpg", null, "PENDING", now, now),
            PhotoEntity(2, 1L, "health_record", 42, "file:///b.jpg", null, "UPLOADED", now, now)
        )
        every { photoDao.getPhotosByParent("health_record", 42) } returns flowOf(entities)

        val result = repository.getPhotosForParent("health_record", 42).first()

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(PhotoUploadStatus.PENDING, result[0].uploadStatus)
        assertEquals(2L, result[1].id)
        assertEquals(PhotoUploadStatus.UPLOADED, result[1].uploadStatus)
    }

    @Test
    fun `addPhoto returns success with id`() = runTest {
        val photo = createPhoto()
        coEvery { photoDao.insert(any()) } returns 5L

        val result = repository.addPhoto(photo)

        assertTrue(result.isSuccess)
        assertEquals(5L, (result as Result.Success).value)
    }

    @Test
    fun `addPhoto returns failure on exception`() = runTest {
        val photo = createPhoto()
        coEvery { photoDao.insert(any()) } throws RuntimeException("DB error")

        val result = repository.addPhoto(photo)

        assertTrue(result.isFailure)
    }

    @Test
    fun `deletePhoto calls dao deleteById`() = runTest {
        coEvery { photoDao.deleteById(7) } just Runs

        val result = repository.deletePhoto(7)

        assertTrue(result.isSuccess)
        coVerify { photoDao.deleteById(7) }
    }

    @Test
    fun `deletePhotosForParent calls dao deleteByParent`() = runTest {
        coEvery { photoDao.deleteByParent("note", 10) } just Runs

        val result = repository.deletePhotosForParent("note", 10)

        assertTrue(result.isSuccess)
        coVerify { photoDao.deleteByParent("note", 10) }
    }

    @Test
    fun `getPendingPhotos returns pending photos`() = runTest {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        val entities = listOf(
            PhotoEntity(1, 1L, "note", 1, "file:///a.jpg", null, "PENDING", now, now)
        )
        coEvery { photoDao.getPhotosWithStatus("PENDING") } returns entities

        val result = repository.getPendingPhotos()

        assertTrue(result.isSuccess)
        assertEquals(1, (result as Result.Success).value.size)
        assertEquals(PhotoUploadStatus.PENDING, result.value[0].uploadStatus)
    }

    @Test
    fun `updateUploadStatus calls dao with correct params`() = runTest {
        coEvery { photoDao.updateUploadStatus(1, "UPLOADED", "https://url.com", any()) } just Runs

        val result = repository.updateUploadStatus(1, PhotoUploadStatus.UPLOADED, "https://url.com")

        assertTrue(result.isSuccess)
        coVerify { photoDao.updateUploadStatus(1, "UPLOADED", "https://url.com", any()) }
    }

    @Test
    fun `updateUploadStatus without remoteUrl`() = runTest {
        coEvery { photoDao.updateUploadStatus(2, "FAILED", null, any()) } just Runs

        val result = repository.updateUploadStatus(2, PhotoUploadStatus.FAILED)

        assertTrue(result.isSuccess)
        coVerify { photoDao.updateUploadStatus(2, "FAILED", null, any()) }
    }

    @Test
    fun `deletePhoto returns failure on exception`() = runTest {
        coEvery { photoDao.deleteById(any()) } throws RuntimeException("DB error")

        val result = repository.deletePhoto(1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPendingPhotos returns failure on exception`() = runTest {
        coEvery { photoDao.getPhotosWithStatus(any()) } throws RuntimeException("DB error")

        val result = repository.getPendingPhotos()

        assertTrue(result.isFailure)
    }

    @Test
    fun `updatePhotosParentId updates all photo ids`() = runTest {
        coEvery { photoDao.updateParentId(any(), any(), any()) } just Runs

        val result = repository.updatePhotosParentId(listOf(1L, 2L, 3L), 42L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 3) { photoDao.updateParentId(any(), eq(42L), any()) }
        coVerify { photoDao.updateParentId(1L, 42L, any()) }
        coVerify { photoDao.updateParentId(2L, 42L, any()) }
        coVerify { photoDao.updateParentId(3L, 42L, any()) }
    }

    @Test
    fun `updatePhotosParentId with empty list succeeds`() = runTest {
        val result = repository.updatePhotosParentId(emptyList(), 42L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { photoDao.updateParentId(any(), any(), any()) }
    }

    @Test
    fun `updatePhotosParentId returns failure on exception`() = runTest {
        coEvery { photoDao.updateParentId(any(), any(), any()) } throws RuntimeException("DB error")

        val result = repository.updatePhotosParentId(listOf(1L), 42L)

        assertTrue(result.isFailure)
    }

    private fun createPhoto(id: Long = 0): Photo {
        return Photo(
            id = id,
            parentType = "health_record",
            parentId = 1,
            localUri = "file:///test.jpg",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
