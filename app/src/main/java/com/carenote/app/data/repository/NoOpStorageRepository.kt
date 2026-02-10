package com.carenote.app.data.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.repository.StorageRepository

class NoOpStorageRepository : StorageRepository {

    override suspend fun uploadPhoto(
        localUri: String,
        remotePath: String
    ): Result<String, DomainError> {
        return Result.Failure(
            DomainError.NetworkError("Firebase Storage is not available")
        )
    }

    override suspend fun deletePhoto(remotePath: String): Result<Unit, DomainError> {
        return Result.Failure(
            DomainError.NetworkError("Firebase Storage is not available")
        )
    }
}
