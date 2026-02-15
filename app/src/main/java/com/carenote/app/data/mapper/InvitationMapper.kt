package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.InvitationEntity
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvitationMapper @Inject constructor() : Mapper<InvitationEntity, Invitation> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: InvitationEntity): Invitation {
        return Invitation(
            id = entity.id,
            careRecipientId = entity.careRecipientId,
            inviterUid = entity.inviterUid,
            inviteeEmail = entity.inviteeEmail,
            status = try {
                InvitationStatus.valueOf(entity.status)
            } catch (_: IllegalArgumentException) {
                InvitationStatus.PENDING
            },
            token = entity.token,
            expiresAt = LocalDateTime.parse(entity.expiresAt, dateTimeFormatter),
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: Invitation): InvitationEntity {
        return InvitationEntity(
            id = domain.id,
            careRecipientId = domain.careRecipientId,
            inviterUid = domain.inviterUid,
            inviteeEmail = domain.inviteeEmail,
            status = domain.status.name,
            token = domain.token,
            expiresAt = domain.expiresAt.format(dateTimeFormatter),
            createdAt = domain.createdAt.format(dateTimeFormatter)
        )
    }
}
