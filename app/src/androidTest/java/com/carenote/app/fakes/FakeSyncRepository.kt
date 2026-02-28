package com.carenote.app.fakes

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.common.SyncState
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncRepository の E2E テスト用 Fake 実装
 *
 * Firestore への依存を排除し、テストコードから同期状態を制御可能にする。
 */
@Singleton
class FakeSyncRepository @Inject constructor() : SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: Flow<SyncState> = _syncState.asStateFlow()

    /** true にすると全メソッドが失敗を返す */
    var shouldFail = false

    /** shouldFail = true 時に返すエラー */
    var failureError: DomainError = DomainError.NetworkError("Test network error")

    /** 成功時に返す SyncResult のデフォルト値 */
    var defaultSyncResult: SyncResult = SyncResult.Success(
        uploadedCount = 0,
        downloadedCount = 0,
        conflictCount = 0
    )

    /** getLastSyncTime() で返す値 */
    var lastSyncTimeValue: LocalDateTime? = null

    /** syncAll() が呼ばれた回数 */
    var syncAllCallCount = 0
        private set

    /** setupInitialCareRecipient() が呼ばれた回数 */
    var setupCallCount = 0
        private set

    /** setupInitialCareRecipient() を失敗させるかどうか */
    var shouldFailSetup = false

    /** 成功時に返す careRecipientId */
    var defaultSetupCareRecipientId = "test-care-recipient-id"

    /** 最後に syncAll() に渡された careRecipientId */
    var lastCareRecipientId: String? = null
        private set

    /**
     * syncState を設定する
     */
    fun setSyncState(state: SyncState) {
        _syncState.value = state
    }

    /**
     * 全状態をリセットする
     */
    fun clear() {
        _syncState.value = SyncState.Idle
        shouldFail = false
        failureError = DomainError.NetworkError("Test network error")
        defaultSyncResult = SyncResult.Success(
            uploadedCount = 0,
            downloadedCount = 0,
            conflictCount = 0
        )
        lastSyncTimeValue = null
        syncAllCallCount = 0
        lastCareRecipientId = null
        setupCallCount = 0
        shouldFailSetup = false
        defaultSetupCareRecipientId = "test-care-recipient-id"
    }

    // ========== SyncRepository interface implementation ==========

    override suspend fun syncAll(careRecipientId: String): SyncResult {
        syncAllCallCount++
        lastCareRecipientId = careRecipientId
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun syncMedications(careRecipientId: String): SyncResult {
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun syncMedicationLogs(
        careRecipientId: String,
        medicationId: Long
    ): SyncResult {
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun syncNotes(careRecipientId: String): SyncResult {
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun syncHealthRecords(careRecipientId: String): SyncResult {
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun syncCalendarEvents(careRecipientId: String): SyncResult {
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun syncNoteComments(careRecipientId: String): SyncResult {
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun getLastSyncTime(): LocalDateTime? {
        return lastSyncTimeValue
    }

    override suspend fun pushLocalChanges(careRecipientId: String): SyncResult {
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun pullRemoteChanges(careRecipientId: String): SyncResult {
        if (shouldFail) return SyncResult.Failure(failureError)
        return defaultSyncResult
    }

    override suspend fun setupInitialCareRecipient(
        userId: String,
        careRecipient: CareRecipient
    ): Result<String, DomainError> {
        setupCallCount++
        if (shouldFailSetup) {
            return Result.Failure(DomainError.NetworkError("Test setup failure"))
        }
        return Result.Success(defaultSetupCareRecipientId)
    }
}
