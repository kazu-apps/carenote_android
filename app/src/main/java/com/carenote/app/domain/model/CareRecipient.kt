package com.carenote.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    UNSPECIFIED
}

data class CareRecipient(
    val id: Long = 0,
    val name: String,
    val birthDate: LocalDate? = null,
    val gender: Gender = Gender.UNSPECIFIED,
    val nickname: String = "",
    val careLevel: String = "",
    val medicalHistory: String = "",
    val allergies: String = "",
    val memo: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
