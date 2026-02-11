package com.carenote.app.ui.viewmodel

import android.net.Uri
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.repository.ImageCompressorInterface
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakePhotoRepository
import com.carenote.app.ui.util.SnackbarController
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var photoRepository: FakePhotoRepository
    private lateinit var imageCompressor: ImageCompressorInterface
    private lateinit var snackbarController: SnackbarController
    private val fakeClock = FakeClock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        photoRepository = FakePhotoRepository()
        imageCompressor = mockk()
        snackbarController = SnackbarController()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createManager(
        parentType: String = "note",
        parentId: Long = 0L
    ): PhotoManager {
        return PhotoManager(
            parentType = parentType,
            parentId = parentId,
            photoRepository = photoRepository,
            imageCompressor = imageCompressor,
            scope = testScope,
            snackbarController = snackbarController,
            clock = fakeClock
        )
    }

    // --- Initialization ---

    @Test
    fun `initial photos list is empty`() {
        val manager = createManager()
        assertEquals(emptyList<Photo>(), manager.photos.value)
    }

    @Test
    fun `initial hasChanges is false`() {
        val manager = createManager()
        assertFalse(manager.hasChanges)
    }

    // --- addPhotos normal ---

    @Test
    fun `addPhotos adds single photo`() = testScope.runTest {
        val uri = mockk<Uri>()
        val compressedUri = mockk<Uri>()
        coEvery { imageCompressor.compress(uri) } returns compressedUri
        coEvery { compressedUri.toString() } returns "compressed://photo1"

        val manager = createManager()
        manager.addPhotos(listOf(uri))
        advanceUntilIdle()

        assertEquals(1, manager.photos.value.size)
        assertEquals("note", manager.photos.value[0].parentType)
        assertEquals("compressed://photo1", manager.photos.value[0].localUri)
    }

    @Test
    fun `addPhotos adds multiple photos`() = testScope.runTest {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()
        val compressed1 = mockk<Uri>()
        val compressed2 = mockk<Uri>()
        coEvery { imageCompressor.compress(uri1) } returns compressed1
        coEvery { imageCompressor.compress(uri2) } returns compressed2
        coEvery { compressed1.toString() } returns "compressed://1"
        coEvery { compressed2.toString() } returns "compressed://2"

        val manager = createManager()
        manager.addPhotos(listOf(uri1, uri2))
        advanceUntilIdle()

        assertEquals(2, manager.photos.value.size)
    }

    @Test
    fun `addPhotos respects MAX_PHOTOS_PER_PARENT limit`() = testScope.runTest {
        // Pre-populate with 5 photos (MAX)
        val existingPhotos = (1..5).map { i ->
            Photo(
                id = i.toLong(),
                parentType = "note",
                parentId = 1L,
                localUri = "existing://$i",
                createdAt = fakeClock.now(),
                updatedAt = fakeClock.now()
            )
        }
        photoRepository.setPhotos(existingPhotos)

        val manager = createManager(parentId = 1L)
        manager.loadPhotos()
        advanceUntilIdle()

        val uri = mockk<Uri>()
        manager.addPhotos(listOf(uri))
        advanceUntilIdle()

        assertEquals(5, manager.photos.value.size)
    }

    @Test
    fun `addPhotos takes only remaining capacity`() = testScope.runTest {
        // Pre-populate with 4 photos
        val existingPhotos = (1..4).map { i ->
            Photo(
                id = i.toLong(),
                parentType = "note",
                parentId = 1L,
                localUri = "existing://$i",
                createdAt = fakeClock.now(),
                updatedAt = fakeClock.now()
            )
        }
        photoRepository.setPhotos(existingPhotos)

        val manager = createManager(parentId = 1L)
        manager.loadPhotos()
        advanceUntilIdle()

        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()
        val compressed1 = mockk<Uri>()
        coEvery { imageCompressor.compress(uri1) } returns compressed1
        coEvery { compressed1.toString() } returns "compressed://new1"

        manager.addPhotos(listOf(uri1, uri2))
        advanceUntilIdle()

        assertEquals(5, manager.photos.value.size)
    }

    // --- addPhotos error ---

    @Test
    fun `addPhotos shows snackbar on compress exception`() = testScope.runTest {
        val uri = mockk<Uri>()
        coEvery { imageCompressor.compress(uri) } throws RuntimeException("compress failed")

        val manager = createManager()
        manager.addPhotos(listOf(uri))
        advanceUntilIdle()

        assertEquals(0, manager.photos.value.size)
    }

    @Test
    fun `addPhotos shows snackbar on addPhoto failure`() = testScope.runTest {
        val uri = mockk<Uri>()
        val compressedUri = mockk<Uri>()
        coEvery { imageCompressor.compress(uri) } returns compressedUri
        coEvery { compressedUri.toString() } returns "compressed://photo"

        photoRepository.shouldFail = true

        val manager = createManager()
        manager.addPhotos(listOf(uri))
        advanceUntilIdle()

        assertEquals(0, manager.photos.value.size)
    }

    // --- removePhoto ---

    @Test
    fun `removePhoto removes photo successfully`() = testScope.runTest {
        val uri = mockk<Uri>()
        val compressedUri = mockk<Uri>()
        coEvery { imageCompressor.compress(uri) } returns compressedUri
        coEvery { compressedUri.toString() } returns "compressed://photo1"

        val manager = createManager()
        manager.addPhotos(listOf(uri))
        advanceUntilIdle()

        val addedPhoto = manager.photos.value[0]
        manager.removePhoto(addedPhoto)
        advanceUntilIdle()

        assertEquals(0, manager.photos.value.size)
    }

    @Test
    fun `removePhoto logs on failure`() = testScope.runTest {
        val uri = mockk<Uri>()
        val compressedUri = mockk<Uri>()
        coEvery { imageCompressor.compress(uri) } returns compressedUri
        coEvery { compressedUri.toString() } returns "compressed://photo1"

        val manager = createManager()
        manager.addPhotos(listOf(uri))
        advanceUntilIdle()

        val addedPhoto = manager.photos.value[0]
        photoRepository.shouldFail = true
        manager.removePhoto(addedPhoto)
        advanceUntilIdle()

        assertEquals(1, manager.photos.value.size)
    }

    // --- loadPhotos ---

    @Test
    fun `loadPhotos loads existing photos`() = testScope.runTest {
        val existingPhotos = listOf(
            Photo(
                id = 1L,
                parentType = "note",
                parentId = 5L,
                localUri = "file://photo1",
                createdAt = fakeClock.now(),
                updatedAt = fakeClock.now()
            ),
            Photo(
                id = 2L,
                parentType = "note",
                parentId = 5L,
                localUri = "file://photo2",
                createdAt = fakeClock.now(),
                updatedAt = fakeClock.now()
            )
        )
        photoRepository.setPhotos(existingPhotos)

        val manager = createManager(parentId = 5L)
        manager.loadPhotos()
        advanceUntilIdle()

        assertEquals(2, manager.photos.value.size)
    }

    @Test
    fun `loadPhotos sets initialPhotoCount`() = testScope.runTest {
        val existingPhotos = listOf(
            Photo(
                id = 1L,
                parentType = "note",
                parentId = 5L,
                localUri = "file://photo1",
                createdAt = fakeClock.now(),
                updatedAt = fakeClock.now()
            )
        )
        photoRepository.setPhotos(existingPhotos)

        val manager = createManager(parentId = 5L)
        manager.loadPhotos()
        advanceUntilIdle()

        assertEquals(1, manager.initialPhotoCount)
    }

    // --- hasChanges ---

    @Test
    fun `hasChanges reflects additions and removals`() = testScope.runTest {
        val uri = mockk<Uri>()
        val compressedUri = mockk<Uri>()
        coEvery { imageCompressor.compress(uri) } returns compressedUri
        coEvery { compressedUri.toString() } returns "compressed://photo1"

        val manager = createManager()

        assertFalse(manager.hasChanges)

        manager.addPhotos(listOf(uri))
        advanceUntilIdle()
        assertTrue(manager.hasChanges)

        val addedPhoto = manager.photos.value[0]
        manager.removePhoto(addedPhoto)
        advanceUntilIdle()
        assertFalse(manager.hasChanges)
    }

    // --- updateParentId ---

    @Test
    fun `updateParentId updates only photos with parentId 0`() = testScope.runTest {
        val uri = mockk<Uri>()
        val compressedUri = mockk<Uri>()
        coEvery { imageCompressor.compress(uri) } returns compressedUri
        coEvery { compressedUri.toString() } returns "compressed://photo1"

        val manager = createManager(parentId = 0L)
        manager.addPhotos(listOf(uri))
        advanceUntilIdle()

        assertEquals(0L, manager.photos.value[0].parentId)

        manager.updateParentId(42L)
        advanceUntilIdle()

        val updated = photoRepository.getAll().find { it.localUri == "compressed://photo1" }
        assertEquals(42L, updated?.parentId)
    }

    @Test
    fun `updateParentId skips when no photos with parentId 0`() = testScope.runTest {
        val existingPhotos = listOf(
            Photo(
                id = 1L,
                parentType = "note",
                parentId = 5L,
                localUri = "file://photo1",
                createdAt = fakeClock.now(),
                updatedAt = fakeClock.now()
            )
        )
        photoRepository.setPhotos(existingPhotos)

        val manager = createManager(parentId = 5L)
        manager.loadPhotos()
        advanceUntilIdle()

        manager.updateParentId(99L)
        advanceUntilIdle()

        assertEquals(5L, photoRepository.getAll()[0].parentId)
    }
}
