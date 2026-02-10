package com.carenote.app.domain.model

import java.time.LocalDateTime

data class EmergencyContact(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: RelationshipType = RelationshipType.OTHER,
    val memo: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
