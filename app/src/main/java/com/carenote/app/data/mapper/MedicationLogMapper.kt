package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationLogMapper @Inject constructor() : Mapper<MedicationLogEntity, MedicationLog> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: MedicationLogEntity): MedicationLog {
        val status = try {
            MedicationLogStatus.valueOf(entity.status)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Unknown MedicationLogStatus: '${entity.status}'. " +
                    "Expected one of: ${MedicationLogStatus.entries.joinToString()}",
                e
            )
        }
        val timing = entity.timing?.let { timingStr ->
            try {
                MedicationTiming.valueOf(timingStr)
            } catch (_: IllegalArgumentException) {
                Timber.w("Unknown MedicationTiming: '$timingStr', falling back to null")
                null
            }
        }
        return MedicationLog(
            id = entity.id,
            careRecipientId = entity.careRecipientId,
            medicationId = entity.medicationId,
            status = status,
            scheduledAt = LocalDateTime.parse(entity.scheduledAt, dateTimeFormatter),
            recordedAt = LocalDateTime.parse(entity.recordedAt, dateTimeFormatter),
            memo = entity.memo,
            timing = timing
        )
    }

    override fun toEntity(domain: MedicationLog): MedicationLogEntity {
        return MedicationLogEntity(
            id = domain.id,
            careRecipientId = domain.careRecipientId,
            medicationId = domain.medicationId,
            status = domain.status.name,
            scheduledAt = domain.scheduledAt.format(dateTimeFormatter),
            recordedAt = domain.recordedAt.format(dateTimeFormatter),
            memo = domain.memo,
            timing = domain.timing?.name
        )
    }
}
