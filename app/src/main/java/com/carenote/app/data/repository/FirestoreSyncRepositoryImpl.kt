package com.carenote.app.data.repository

import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.SettingsDataSource
import com.carenote.app.data.local.dao.SyncMappingDao
import com.carenote.app.data.repository.sync.EntitySyncer
import com.carenote.app.data.repository.sync.MedicationLogSyncer
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.common.SyncState
import com.carenote.app.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * SyncRepository の Firestore 実装
 *
 * オフラインファースト同期を実現し、Room と Firestore 間の双方向同期を行う。
 * Last-Write-Wins (LWW) 戦略で競合を解決する。
 */
@Singleton
class FirestoreSyncRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
    private val syncMappingDao: SyncMappingDao,
    @Named("medication") private val medicationSyncer: EntitySyncer<*, *>,
    private val medicationLogSyncer: MedicationLogSyncer,
    @Named("note") private val noteSyncer: EntitySyncer<*, *>,
    @Named("healthRecord") private val healthRecordSyncer: EntitySyncer<*, *>,
    @Named("calendarEvent") private val calendarEventSyncer: EntitySyncer<*, *>,
    @Named("noteComment") private val noteCommentSyncer: EntitySyncer<*, *>
) : SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: Flow<SyncState> = _syncState.asStateFlow()

    override suspend fun syncAll(careRecipientId: String): SyncResult {
        Timber.d("Starting full sync for careRecipientId=$careRecipientId")
        val startTime = LocalDateTime.now()
        val accumulator = SyncAccumulator()

        val entitySyncResult = syncAllEntities(careRecipientId, accumulator)
        if (entitySyncResult != null) return entitySyncResult

        val medLogResult = syncMedicationLogsPhase(careRecipientId, accumulator)
        if (medLogResult != null) return medLogResult

        val finalResult = accumulator.toFinalResult()
        if (finalResult is SyncResult.Success) {
            settingsDataSource.updateLastSyncTime(startTime)
        }
        _syncState.value = SyncState.Success(startTime)
        Timber.d("Full sync completed: $finalResult")
        return finalResult
    }

    private suspend fun syncAllEntities(
        careRecipientId: String,
        accumulator: SyncAccumulator
    ): SyncResult? {
        val entityNames = listOf(
            "medications", "notes", "healthRecords",
            "calendarEvents", "noteComments"
        )

        entityNames.forEachIndexed { index, entityName ->
            val progress = index.toFloat() / AppConfig.Sync.ENTITY_TYPE_COUNT
            _syncState.value = SyncState.Syncing(progress, entityName)

            val result = syncEntityByName(careRecipientId, entityName)
            val failure = accumulator.accumulate(result)
            if (failure != null) {
                _syncState.value = SyncState.Error(failure.error, isRetryable = true)
                return failure
            }
        }
        return null
    }

    private suspend fun syncEntityByName(
        careRecipientId: String,
        entityName: String
    ): SyncResult = when (entityName) {
        "medications" -> syncMedications(careRecipientId)
        "notes" -> syncNotes(careRecipientId)
        "healthRecords" -> syncHealthRecords(careRecipientId)
        "calendarEvents" -> syncCalendarEvents(careRecipientId)
        "noteComments" -> syncNoteComments(careRecipientId)
        else -> SyncResult.Success(0, 0)
    }

    private suspend fun syncMedicationLogsPhase(
        careRecipientId: String,
        accumulator: SyncAccumulator
    ): SyncResult? {
        _syncState.value = SyncState.Syncing(
            AppConfig.Sync.ENTITY_TYPE_COUNT.toFloat() / (AppConfig.Sync.ENTITY_TYPE_COUNT + 1),
            "medicationLogs"
        )
        val lastSyncTime = getLastSyncTime()
        val result = syncAllMedicationLogs(careRecipientId, lastSyncTime)
        val failure = accumulator.accumulate(result)
        if (failure != null) {
            _syncState.value = SyncState.Error(failure.error, isRetryable = true)
            return failure
        }
        return null
    }

    private class SyncAccumulator {
        var totalUploaded = 0
        var totalDownloaded = 0
        var totalConflicts = 0
        val allFailedEntities = mutableListOf<Long>()
        val allErrors = mutableListOf<DomainError>()

        fun accumulate(result: SyncResult): SyncResult.Failure? {
            when (result) {
                is SyncResult.Success -> {
                    totalUploaded += result.uploadedCount
                    totalDownloaded += result.downloadedCount
                    totalConflicts += result.conflictCount
                }
                is SyncResult.PartialSuccess -> {
                    allFailedEntities.addAll(result.failedEntities)
                    allErrors.addAll(result.errors)
                }
                is SyncResult.Failure -> return result
            }
            return null
        }

        fun toFinalResult(): SyncResult = if (allFailedEntities.isEmpty()) {
            SyncResult.Success(
                uploadedCount = totalUploaded,
                downloadedCount = totalDownloaded,
                conflictCount = totalConflicts
            )
        } else {
            SyncResult.PartialSuccess(
                successCount = totalUploaded + totalDownloaded,
                failedEntities = allFailedEntities,
                errors = allErrors
            )
        }
    }

    override suspend fun syncMedications(careRecipientId: String): SyncResult {
        val lastSyncTime = getLastSyncTime()
        return medicationSyncer.sync(careRecipientId, lastSyncTime)
    }

    override suspend fun syncMedicationLogs(
        careRecipientId: String,
        medicationId: Long
    ): SyncResult {
        val lastSyncTime = getLastSyncTime()
        val medicationMapping = syncMappingDao.getByLocalId("medication", medicationId)
            ?: return SyncResult.Failure(
                DomainError.NotFoundError("Medication mapping not found for id=$medicationId")
            )

        return medicationLogSyncer.syncForMedication(
            careRecipientId,
            medicationId,
            medicationMapping.remoteId,
            lastSyncTime
        )
    }

    override suspend fun syncNotes(careRecipientId: String): SyncResult {
        val lastSyncTime = getLastSyncTime()
        return noteSyncer.sync(careRecipientId, lastSyncTime)
    }

    override suspend fun syncHealthRecords(careRecipientId: String): SyncResult {
        val lastSyncTime = getLastSyncTime()
        return healthRecordSyncer.sync(careRecipientId, lastSyncTime)
    }

    override suspend fun syncCalendarEvents(careRecipientId: String): SyncResult {
        val lastSyncTime = getLastSyncTime()
        return calendarEventSyncer.sync(careRecipientId, lastSyncTime)
    }

    override suspend fun syncNoteComments(careRecipientId: String): SyncResult {
        val lastSyncTime = getLastSyncTime()
        return noteCommentSyncer.sync(careRecipientId, lastSyncTime)
    }

    override suspend fun getLastSyncTime(): LocalDateTime? =
        settingsDataSource.getLastSyncTime()

    override suspend fun pushLocalChanges(careRecipientId: String): SyncResult {
        Timber.d("Pushing local changes for careRecipientId=$careRecipientId")
        _syncState.value = SyncState.Syncing(0f, "push")

        val lastSyncTime = getLastSyncTime()
        val syncTime = LocalDateTime.now()

        var totalUploaded = 0
        val allFailedEntities = mutableListOf<Long>()
        val allErrors = mutableListOf<DomainError>()

        val syncers = listOf(
            medicationSyncer, noteSyncer, healthRecordSyncer,
            calendarEventSyncer, noteCommentSyncer
        )

        syncers.forEachIndexed { index, syncer ->
            val progress = (index + 1).toFloat() / syncers.size
            _syncState.value = SyncState.Syncing(progress, syncer.entityType)

            val result = syncer.sync(careRecipientId, lastSyncTime)
            when (result) {
                is SyncResult.Success -> totalUploaded += result.uploadedCount
                is SyncResult.PartialSuccess -> {
                    totalUploaded += result.successCount
                    allFailedEntities.addAll(result.failedEntities)
                    allErrors.addAll(result.errors)
                }
                is SyncResult.Failure -> {
                    _syncState.value = SyncState.Error(result.error)
                    return result
                }
            }
        }

        if (allFailedEntities.isEmpty()) {
            settingsDataSource.updateLastSyncTime(syncTime)
        }
        _syncState.value = SyncState.Success(syncTime)

        return if (allFailedEntities.isEmpty()) {
            SyncResult.Success(uploadedCount = totalUploaded, downloadedCount = 0)
        } else {
            SyncResult.PartialSuccess(totalUploaded, allFailedEntities, allErrors)
        }
    }

    override suspend fun pullRemoteChanges(careRecipientId: String): SyncResult {
        Timber.d("Pulling remote changes for careRecipientId=$careRecipientId")
        _syncState.value = SyncState.Syncing(0f, "pull")

        val lastSyncTime = getLastSyncTime()
        val syncTime = LocalDateTime.now()

        var totalDownloaded = 0
        var totalConflicts = 0
        val allFailedEntities = mutableListOf<Long>()
        val allErrors = mutableListOf<DomainError>()

        val syncers = listOf(
            medicationSyncer, noteSyncer, healthRecordSyncer,
            calendarEventSyncer, noteCommentSyncer
        )

        syncers.forEachIndexed { index, syncer ->
            val progress = (index + 1).toFloat() / syncers.size
            _syncState.value = SyncState.Syncing(progress, syncer.entityType)

            val result = syncer.sync(careRecipientId, lastSyncTime)
            when (result) {
                is SyncResult.Success -> {
                    totalDownloaded += result.downloadedCount
                    totalConflicts += result.conflictCount
                }
                is SyncResult.PartialSuccess -> {
                    allFailedEntities.addAll(result.failedEntities)
                    allErrors.addAll(result.errors)
                }
                is SyncResult.Failure -> {
                    _syncState.value = SyncState.Error(result.error)
                    return result
                }
            }
        }

        if (allFailedEntities.isEmpty()) {
            settingsDataSource.updateLastSyncTime(syncTime)
        }
        _syncState.value = SyncState.Success(syncTime)

        return if (allFailedEntities.isEmpty()) {
            SyncResult.Success(
                uploadedCount = 0,
                downloadedCount = totalDownloaded,
                conflictCount = totalConflicts
            )
        } else {
            SyncResult.PartialSuccess(totalDownloaded, allFailedEntities, allErrors)
        }
    }

    /**
     * 全ての服薬に対する MedicationLog を同期
     */
    private suspend fun syncAllMedicationLogs(
        careRecipientId: String,
        lastSyncTime: LocalDateTime?
    ): SyncResult {
        val medicationMappings = syncMappingDao.getAllByType("medication")

        var totalUploaded = 0
        var totalDownloaded = 0
        val allFailedEntities = mutableListOf<Long>()
        val allErrors = mutableListOf<DomainError>()

        for (mapping in medicationMappings) {
            val result = medicationLogSyncer.syncForMedication(
                careRecipientId,
                mapping.localId,
                mapping.remoteId,
                lastSyncTime
            )

            when (result) {
                is SyncResult.Success -> {
                    totalUploaded += result.uploadedCount
                    totalDownloaded += result.downloadedCount
                }
                is SyncResult.PartialSuccess -> {
                    allFailedEntities.addAll(result.failedEntities)
                    allErrors.addAll(result.errors)
                }
                is SyncResult.Failure -> {
                    allErrors.add(result.error)
                }
            }
        }

        return if (allFailedEntities.isEmpty() && allErrors.isEmpty()) {
            SyncResult.Success(
                uploadedCount = totalUploaded,
                downloadedCount = totalDownloaded
            )
        } else if (allErrors.isNotEmpty() && totalUploaded == 0 && totalDownloaded == 0) {
            SyncResult.Failure(allErrors.first())
        } else {
            SyncResult.PartialSuccess(
                successCount = totalUploaded + totalDownloaded,
                failedEntities = allFailedEntities,
                errors = allErrors
            )
        }
    }
}
