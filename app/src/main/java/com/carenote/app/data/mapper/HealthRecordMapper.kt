package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.HealthRecordEntity
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRecordMapper @Inject constructor() : Mapper<HealthRecordEntity, HealthRecord> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: HealthRecordEntity): HealthRecord {
        return HealthRecord(
            id = entity.id,
            careRecipientId = entity.careRecipientId,
            temperature = entity.temperature,
            bloodPressureHigh = entity.bloodPressureHigh,
            bloodPressureLow = entity.bloodPressureLow,
            pulse = entity.pulse,
            weight = entity.weight,
            meal = entity.meal?.let { parseMealAmount(it) },
            excretion = entity.excretion?.let { parseExcretionType(it) },
            conditionNote = entity.conditionNote,
            createdBy = entity.createdBy,
            recordedAt = LocalDateTime.parse(entity.recordedAt, dateTimeFormatter),
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: HealthRecord): HealthRecordEntity {
        return HealthRecordEntity(
            id = domain.id,
            careRecipientId = domain.careRecipientId,
            temperature = domain.temperature,
            bloodPressureHigh = domain.bloodPressureHigh,
            bloodPressureLow = domain.bloodPressureLow,
            pulse = domain.pulse,
            weight = domain.weight,
            meal = domain.meal?.name,
            excretion = domain.excretion?.name,
            conditionNote = domain.conditionNote,
            createdBy = domain.createdBy,
            recordedAt = domain.recordedAt.format(dateTimeFormatter),
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter)
        )
    }

    private fun parseMealAmount(value: String): MealAmount? {
        return try {
            MealAmount.valueOf(value)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun parseExcretionType(value: String): ExcretionType? {
        return try {
            ExcretionType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
