package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationLogMapper @Inject constructor() : Mapper<MedicationLogEntity, MedicationLog> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: MedicationLogEntity): MedicationLog {
        return MedicationLog(
            id = entity.id,
            medicationId = entity.medicationId,
            status = MedicationLogStatus.valueOf(entity.status),
            scheduledAt = LocalDateTime.parse(entity.scheduledAt, dateTimeFormatter),
            recordedAt = LocalDateTime.parse(entity.recordedAt, dateTimeFormatter),
            memo = entity.memo
        )
    }

    override fun toEntity(domain: MedicationLog): MedicationLogEntity {
        return MedicationLogEntity(
            id = domain.id,
            medicationId = domain.medicationId,
            status = domain.status.name,
            scheduledAt = domain.scheduledAt.format(dateTimeFormatter),
            recordedAt = domain.recordedAt.format(dateTimeFormatter),
            memo = domain.memo
        )
    }
}
