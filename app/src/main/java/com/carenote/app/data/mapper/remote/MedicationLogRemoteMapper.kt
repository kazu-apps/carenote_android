package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MedicationLog の Firestore Document ↔ Domain Model 変換マッパー
 */
@Singleton
class MedicationLogRemoteMapper @Inject constructor(
    private val timestampConverter: FirestoreTimestampConverter
) : RemoteMapper<MedicationLog> {

    override fun toDomain(data: Map<String, Any?>): MedicationLog {
        val localId = (data["localId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("localId is required")
        val medicationLocalId = (data["medicationLocalId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("medicationLocalId is required")
        val statusString = data["status"] as? String
            ?: throw IllegalArgumentException("status is required")
        val scheduledAt = data["scheduledAt"]
            ?: throw IllegalArgumentException("scheduledAt is required")
        val recordedAt = data["recordedAt"]
            ?: throw IllegalArgumentException("recordedAt is required")

        val status = try {
            MedicationLogStatus.valueOf(statusString)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid status: $statusString")
        }

        val timing = (data["timing"] as? String)?.let { timingStr ->
            try {
                MedicationTiming.valueOf(timingStr)
            } catch (e: IllegalArgumentException) {
                Timber.w("Unknown MedicationTiming from remote: '$timingStr', falling back to null")
                null
            }
        }

        return MedicationLog(
            id = localId,
            medicationId = medicationLocalId,
            status = status,
            scheduledAt = timestampConverter.toLocalDateTimeFromAny(scheduledAt),
            recordedAt = timestampConverter.toLocalDateTimeFromAny(recordedAt),
            memo = data["memo"] as? String ?: "",
            timing = timing
        )
    }

    override fun toRemote(domain: MedicationLog, syncMetadata: SyncMetadata?): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "localId" to domain.id,
            "medicationLocalId" to domain.medicationId,
            "status" to domain.status.name,
            "scheduledAt" to timestampConverter.toTimestamp(domain.scheduledAt),
            "recordedAt" to timestampConverter.toTimestamp(domain.recordedAt),
            "memo" to domain.memo,
            "timing" to domain.timing?.name
        )

        syncMetadata?.let { metadata ->
            result["syncedAt"] = timestampConverter.toTimestamp(metadata.syncedAt)
            result["deletedAt"] = metadata.deletedAt?.let {
                timestampConverter.toTimestamp(it)
            }
        }

        return result
    }

    override fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata {
        val localId = (data["localId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("localId is required")
        val syncedAt = data["syncedAt"]
            ?: throw IllegalArgumentException("syncedAt is required")

        return SyncMetadata(
            localId = localId,
            syncedAt = timestampConverter.toLocalDateTimeFromAny(syncedAt),
            deletedAt = timestampConverter.toLocalDateTimeFromAnyOrNull(data["deletedAt"])
        )
    }
}
