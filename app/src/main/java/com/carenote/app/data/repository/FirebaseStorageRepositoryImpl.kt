package com.carenote.app.data.repository

import android.net.Uri
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageRepositoryImpl @Inject constructor(
    private val storage: dagger.Lazy<FirebaseStorage>
) : StorageRepository {

    override suspend fun uploadPhoto(
        localUri: String,
        remotePath: String
    ): Result<String, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.NetworkError("Failed to upload photo: ${it.message}", it) }
        ) {
            val ref = storage.get().reference.child(remotePath)
            ref.putFile(Uri.parse(localUri)).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Timber.d("Photo uploaded: $remotePath")
            downloadUrl
        }
    }

    override suspend fun deletePhoto(remotePath: String): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.NetworkError("Failed to delete photo: ${it.message}", it) }
        ) {
            storage.get().reference.child(remotePath).delete().await()
            Timber.d("Photo deleted: $remotePath")
        }
    }
}
