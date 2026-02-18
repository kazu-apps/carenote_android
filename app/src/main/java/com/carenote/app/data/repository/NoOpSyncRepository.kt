package com.carenote.app.data.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.common.SyncState
import com.carenote.app.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.time.LocalDateTime

class NoOpSyncRepository : SyncRepository {

    override val syncState: Flow<SyncState> = MutableStateFlow(SyncState.Idle)

    override suspend fun syncAll(careRecipientId: String): SyncResult {
        Timber.w("syncAll called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    override suspend fun syncMedications(careRecipientId: String): SyncResult {
        Timber.w("syncMedications called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    override suspend fun syncMedicationLogs(
        careRecipientId: String,
        medicationId: Long
    ): SyncResult {
        Timber.w("syncMedicationLogs called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    override suspend fun syncNotes(careRecipientId: String): SyncResult {
        Timber.w("syncNotes called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    override suspend fun syncHealthRecords(careRecipientId: String): SyncResult {
        Timber.w("syncHealthRecords called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    override suspend fun syncCalendarEvents(careRecipientId: String): SyncResult {
        Timber.w("syncCalendarEvents called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    override suspend fun syncNoteComments(careRecipientId: String): SyncResult {
        Timber.w("syncNoteComments called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    override suspend fun getLastSyncTime(): LocalDateTime? = null

    override suspend fun pushLocalChanges(careRecipientId: String): SyncResult {
        Timber.w("pushLocalChanges called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    override suspend fun pullRemoteChanges(careRecipientId: String): SyncResult {
        Timber.w("pullRemoteChanges called but Firebase is not configured")
        return firebaseUnavailableFailure()
    }

    private fun firebaseUnavailableFailure(): SyncResult.Failure =
        SyncResult.Failure(
            DomainError.NetworkError("Firebase is not configured. Please add google-services.json.")
        )
}
