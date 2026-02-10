package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationTiming
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Medication の Firestore Document ↔ Domain Model 変換マッパー
 */
@Singleton
class MedicationRemoteMapper @Inject constructor(
    private val timestampConverter: FirestoreTimestampConverter
) : RemoteMapper<Medication> {

    override fun toDomain(data: Map<String, Any?>): Medication {
        val localId = (data["localId"] as? Number)?.toLong()
            ?: throw IllegalArgumentException("localId is required")
        val name = data["name"] as? String
            ?: throw IllegalArgumentException("name is required")
        val createdAt = data["createdAt"]
            ?: throw IllegalArgumentException("createdAt is required")
        val updatedAt = data["updatedAt"]
            ?: throw IllegalArgumentException("updatedAt is required")

        return Medication(
            id = localId,
            name = name,
            dosage = data["dosage"] as? String ?: "",
            timings = parseTimings(data["timings"]),
            times = parseTimes(data["times"]),
            reminderEnabled = data["reminderEnabled"] as? Boolean ?: true,
            createdAt = timestampConverter.toLocalDateTimeFromAny(createdAt),
            updatedAt = timestampConverter.toLocalDateTimeFromAny(updatedAt),
            currentStock = (data["currentStock"] as? Number)?.toInt(),
            lowStockThreshold = (data["lowStockThreshold"] as? Number)?.toInt()
        )
    }

    override fun toRemote(domain: Medication, syncMetadata: SyncMetadata?): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "localId" to domain.id,
            "name" to domain.name,
            "dosage" to domain.dosage,
            "timings" to serializeTimings(domain.timings),
            "times" to serializeTimes(domain.times),
            "reminderEnabled" to domain.reminderEnabled,
            "createdAt" to timestampConverter.toTimestamp(domain.createdAt),
            "updatedAt" to timestampConverter.toTimestamp(domain.updatedAt),
            "currentStock" to domain.currentStock,
            "lowStockThreshold" to domain.lowStockThreshold
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

    @Suppress("UNCHECKED_CAST")
    private fun parseTimings(value: Any?): List<MedicationTiming> {
        val list = value as? List<*> ?: return emptyList()
        return list.mapNotNull { item ->
            val name = item as? String ?: return@mapNotNull null
            try {
                MedicationTiming.valueOf(name)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    private fun serializeTimings(timings: List<MedicationTiming>): List<String> {
        return timings.map { it.name }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTimes(value: Any?): Map<MedicationTiming, LocalTime> {
        val map = value as? Map<String, String> ?: return emptyMap()
        return map.mapNotNull { (key, timeString) ->
            try {
                val timing = MedicationTiming.valueOf(key)
                val time = timestampConverter.toLocalTime(timeString)
                timing to time
            } catch (_: Exception) {
                null
            }
        }.toMap()
    }

    private fun serializeTimes(times: Map<MedicationTiming, LocalTime>): Map<String, String> {
        return times.map { (timing, time) ->
            timing.name to timestampConverter.toTimeString(time)
        }.toMap()
    }
}
