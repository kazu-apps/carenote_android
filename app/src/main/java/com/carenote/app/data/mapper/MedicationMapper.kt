package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.MedicationEntity
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationTiming
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationMapper @Inject constructor() : Mapper<MedicationEntity, Medication> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    override fun toDomain(entity: MedicationEntity): Medication {
        return Medication(
            id = entity.id,
            careRecipientId = entity.careRecipientId,
            name = entity.name,
            dosage = entity.dosage,
            timings = parseTimings(entity.timings),
            times = parseTimes(entity.times),
            reminderEnabled = entity.reminderEnabled,
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter),
            currentStock = entity.currentStock,
            lowStockThreshold = entity.lowStockThreshold
        )
    }

    override fun toEntity(domain: Medication): MedicationEntity {
        return MedicationEntity(
            id = domain.id,
            careRecipientId = domain.careRecipientId,
            name = domain.name,
            dosage = domain.dosage,
            timings = serializeTimings(domain.timings),
            times = serializeTimes(domain.times),
            reminderEnabled = domain.reminderEnabled,
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter),
            currentStock = domain.currentStock,
            lowStockThreshold = domain.lowStockThreshold
        )
    }

    private fun parseTimings(value: String): List<MedicationTiming> {
        if (value.isBlank()) return emptyList()
        return value.split(",").mapNotNull { timing ->
            try {
                MedicationTiming.valueOf(timing.trim())
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    private fun serializeTimings(timings: List<MedicationTiming>): String {
        return timings.joinToString(",") { it.name }
    }

    private fun parseTimes(value: String): Map<MedicationTiming, LocalTime> {
        if (value.isBlank()) return emptyMap()
        return value.split(";").mapNotNull { entry ->
            val parts = entry.split("=")
            if (parts.size == 2) {
                try {
                    val timing = MedicationTiming.valueOf(parts[0].trim())
                    val time = LocalTime.parse(parts[1].trim(), timeFormatter)
                    timing to time
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        }.toMap()
    }

    private fun serializeTimes(times: Map<MedicationTiming, LocalTime>): String {
        return times.entries.joinToString(";") { (timing, time) ->
            "${timing.name}=${time.format(timeFormatter)}"
        }
    }
}
