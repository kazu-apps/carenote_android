package com.carenote.app.domain.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result

interface StorageRepository {

    suspend fun uploadPhoto(localUri: String, remotePath: String): Result<String, DomainError>

    suspend fun deletePhoto(remotePath: String): Result<Unit, DomainError>
}
