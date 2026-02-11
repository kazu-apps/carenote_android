package com.carenote.app.ui.viewmodel

import android.net.Uri
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.repository.ImageCompressorInterface
import com.carenote.app.domain.repository.PhotoRepository
import com.carenote.app.domain.util.Clock
import com.carenote.app.ui.util.SnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber

class PhotoManager(
    private val parentType: String,
    private val parentId: Long,
    private val photoRepository: PhotoRepository,
    private val imageCompressor: ImageCompressorInterface,
    private val scope: CoroutineScope,
    private val snackbarController: SnackbarController,
    private val clock: Clock
) {

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    var initialPhotoCount: Int = 0
        private set

    val hasChanges: Boolean
        get() = _photos.value.size != initialPhotoCount

    fun loadPhotos() {
        scope.launch {
            val existingPhotos = photoRepository.getPhotosForParent(parentType, parentId)
                .firstOrNull()
                .orEmpty()
            _photos.value = existingPhotos
            initialPhotoCount = existingPhotos.size
        }
    }

    fun addPhotos(uris: List<Uri>) {
        val remaining = AppConfig.Photo.MAX_PHOTOS_PER_PARENT - _photos.value.size
        if (remaining <= 0) return
        val toAdd = uris.take(remaining)
        scope.launch {
            for (uri in toAdd) {
                try {
                    val compressed = imageCompressor.compress(uri)
                    val now = clock.now()
                    val photo = Photo(
                        parentType = parentType,
                        parentId = parentId,
                        localUri = compressed.toString(),
                        createdAt = now,
                        updatedAt = now
                    )
                    photoRepository.addPhoto(photo)
                        .onSuccess { id ->
                            _photos.value = _photos.value + photo.copy(id = id)
                        }
                        .onFailure { error ->
                            Timber.w("Failed to add photo: $error")
                            snackbarController.showMessage(R.string.photo_compress_failed)
                        }
                } catch (e: Exception) {
                    Timber.w("Failed to compress photo: $e")
                    snackbarController.showMessage(R.string.photo_compress_failed)
                }
            }
        }
    }

    fun removePhoto(photo: Photo) {
        scope.launch {
            photoRepository.deletePhoto(photo.id)
                .onSuccess {
                    _photos.value = _photos.value.filter { it.id != photo.id }
                }
                .onFailure { error ->
                    Timber.w("Failed to remove photo: $error")
                }
        }
    }

    suspend fun updateParentId(newParentId: Long) {
        val photoIds = _photos.value
            .filter { it.parentId == 0L }
            .map { it.id }
        if (photoIds.isNotEmpty()) {
            photoRepository.updatePhotosParentId(photoIds, newParentId)
        }
    }
}
