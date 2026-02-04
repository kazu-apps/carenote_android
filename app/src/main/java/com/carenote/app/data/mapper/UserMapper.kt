package com.carenote.app.data.mapper

import com.carenote.app.domain.model.User
import com.google.firebase.auth.FirebaseUser
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserMapper @Inject constructor() {

    fun toDomain(firebaseUser: FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            createdAt = firebaseUser.metadata?.creationTimestamp?.toLocalDateTime()
                ?: LocalDateTime.now(),
            isPremium = false
        )
    }

    private fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}
