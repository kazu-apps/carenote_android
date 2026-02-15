package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.MemberEntity
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.model.MemberRole
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberMapper @Inject constructor() : Mapper<MemberEntity, Member> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun toDomain(entity: MemberEntity): Member {
        return Member(
            id = entity.id,
            careRecipientId = entity.careRecipientId,
            uid = entity.uid,
            role = try {
                MemberRole.valueOf(entity.role)
            } catch (_: IllegalArgumentException) {
                MemberRole.MEMBER
            },
            joinedAt = LocalDateTime.parse(entity.joinedAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: Member): MemberEntity {
        return MemberEntity(
            id = domain.id,
            careRecipientId = domain.careRecipientId,
            uid = domain.uid,
            role = domain.role.name,
            joinedAt = domain.joinedAt.format(dateTimeFormatter)
        )
    }
}
