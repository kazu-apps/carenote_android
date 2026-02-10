package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.CareRecipientEntity
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.domain.model.Gender
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareRecipientMapper @Inject constructor() : Mapper<CareRecipientEntity, CareRecipient> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun toDomain(entity: CareRecipientEntity): CareRecipient {
        return CareRecipient(
            id = entity.id,
            name = entity.name,
            birthDate = entity.birthDate?.let { LocalDate.parse(it, dateFormatter) },
            gender = parseGender(entity.gender),
            memo = entity.memo,
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: CareRecipient): CareRecipientEntity {
        return CareRecipientEntity(
            id = domain.id,
            name = domain.name,
            birthDate = domain.birthDate?.format(dateFormatter),
            gender = domain.gender.name,
            memo = domain.memo,
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter)
        )
    }

    private fun parseGender(value: String): Gender {
        return try {
            Gender.valueOf(value)
        } catch (_: IllegalArgumentException) {
            Gender.UNSPECIFIED
        }
    }
}
