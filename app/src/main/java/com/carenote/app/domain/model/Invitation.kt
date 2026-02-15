package com.carenote.app.domain.model

import java.time.LocalDateTime

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED
}

data class Invitation(
    val id: Long = 0,
    val careRecipientId: Long = 0,
    val inviterUid: String,
    val inviteeEmail: String,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val token: String,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
