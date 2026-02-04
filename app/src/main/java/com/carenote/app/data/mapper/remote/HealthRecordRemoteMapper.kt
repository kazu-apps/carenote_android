package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HealthRecord の Firestore Document ↔ Domain Model 変換マッパー
 */
@Singleton
class HealthRecordRemoteMapper @Inject constructor(
    private val timestampConverter: FirestoreTimestampConverter
) : RemoteMapper<HealthRecord> {

    override fun toDomain(data: Map<String, Any?>): HealthRecord {
        val localId = (data["localId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("localId is required")
        val recordedAt = data["recordedAt"]
            ?: throw IllegalArgumentException("recordedAt is required")
        val createdAt = data["createdAt"]
            ?: throw IllegalArgumentException("createdAt is required")
        val updatedAt = data["updatedAt"]
            ?: throw IllegalArgumentException("updatedAt is required")

        return HealthRecord(
            id = localId,
            temperature = (data["temperature"] as? Number)?.toDouble(),
            bloodPressureHigh = (data["bloodPressureHigh"] as? Number)?.toInt(),
            bloodPressureLow = (data["bloodPressureLow"] as? Number)?.toInt(),
            pulse = (data["pulse"] as? Number)?.toInt(),
            weight = (data["weight"] as? Number)?.toDouble(),
            meal = parseMealAmount(data["meal"] as? String),
            excretion = parseExcretionType(data["excretion"] as? String),
            conditionNote = data["conditionNote"] as? String ?: "",
            recordedAt = timestampConverter.toLocalDateTimeFromAny(recordedAt),
            createdAt = timestampConverter.toLocalDateTimeFromAny(createdAt),
            updatedAt = timestampConverter.toLocalDateTimeFromAny(updatedAt)
        )
    }

    override fun toRemote(domain: HealthRecord, syncMetadata: SyncMetadata?): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "localId" to domain.id,
            "temperature" to domain.temperature,
            "bloodPressureHigh" to domain.bloodPressureHigh,
            "bloodPressureLow" to domain.bloodPressureLow,
            "pulse" to domain.pulse,
            "weight" to domain.weight,
            "meal" to domain.meal?.name,
            "excretion" to domain.excretion?.name,
            "conditionNote" to domain.conditionNote,
            "recordedAt" to timestampConverter.toTimestamp(domain.recordedAt),
            "createdAt" to timestampConverter.toTimestamp(domain.createdAt),
            "updatedAt" to timestampConverter.toTimestamp(domain.updatedAt)
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

    private fun parseMealAmount(value: String?): MealAmount? {
        if (value == null) return null
        return try {
            MealAmount.valueOf(value)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun parseExcretionType(value: String?): ExcretionType? {
        if (value == null) return null
        return try {
            ExcretionType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
