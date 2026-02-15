package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invitations",
    indices = [
        Index(value = ["care_recipient_id"]),
        Index(value = ["invitee_email"]),
        Index(value = ["token"], unique = true),
        Index(value = ["status"])
    ]
)
data class InvitationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "care_recipient_id", defaultValue = "0")
    val careRecipientId: Long = 0,

    @ColumnInfo(name = "inviter_uid")
    val inviterUid: String,

    @ColumnInfo(name = "invitee_email")
    val inviteeEmail: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "token")
    val token: String,

    @ColumnInfo(name = "expires_at")
    val expiresAt: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String
)
