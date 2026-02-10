package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.EmergencyContactEntity
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.model.RelationshipType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactMapper @Inject constructor() : Mapper<EmergencyContactEntity, EmergencyContact> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: EmergencyContactEntity): EmergencyContact {
        return EmergencyContact(
            id = entity.id,
            name = entity.name,
            phoneNumber = entity.phoneNumber,
            relationship = parseRelationshipType(entity.relationship),
            memo = entity.memo,
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: EmergencyContact): EmergencyContactEntity {
        return EmergencyContactEntity(
            id = domain.id,
            name = domain.name,
            phoneNumber = domain.phoneNumber,
            relationship = domain.relationship.name,
            memo = domain.memo,
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter)
        )
    }

    private fun parseRelationshipType(value: String): RelationshipType {
        return try {
            RelationshipType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            RelationshipType.OTHER
        }
    }
}
