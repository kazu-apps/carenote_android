package com.carenote.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class CareRecipient(
    val id: String,
    val name: String,
    val nickname: String = "",
    val birthDate: LocalDate? = null,
    val gender: String = "",
    val careLevel: String = "",
    val medicalHistory: String = "",
    val allergies: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
