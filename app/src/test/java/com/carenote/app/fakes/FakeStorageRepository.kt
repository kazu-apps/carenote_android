package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.repository.StorageRepository

class FakeStorageRepository : StorageRepository {

    var shouldFail = false
    var lastUploadedPath: String? = null
    var lastDeletedPath: String? = null

    fun clear() {
        shouldFail = false
        lastUploadedPath = null
        lastDeletedPath = null
    }

    override suspend fun uploadPhoto(
        localUri: String,
        remotePath: String
    ): Result<String, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.NetworkError("Fake upload error"))
        }
        lastUploadedPath = remotePath
        return Result.Success("https://storage.example.com/$remotePath")
    }

    override suspend fun deletePhoto(remotePath: String): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.NetworkError("Fake delete error"))
        }
        lastDeletedPath = remotePath
        return Result.Success(Unit)
    }
}
