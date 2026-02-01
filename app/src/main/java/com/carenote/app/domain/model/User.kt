package com.carenote.app.domain.model

import java.time.LocalDateTime

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime,
    val isPremium: Boolean = false
)
