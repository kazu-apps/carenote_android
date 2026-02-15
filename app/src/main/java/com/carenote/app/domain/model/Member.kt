package com.carenote.app.domain.model

import java.time.LocalDateTime

enum class MemberRole {
    OWNER,
    MEMBER
}

data class Member(
    val id: Long = 0,
    val careRecipientId: Long = 0,
    val uid: String,
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: LocalDateTime = LocalDateTime.now()
)
