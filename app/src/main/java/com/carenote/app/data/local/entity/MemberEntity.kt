package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
    indices = [
        Index(value = ["care_recipient_id"]),
        Index(value = ["uid"]),
        Index(value = ["care_recipient_id", "uid"], unique = true)
    ]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "care_recipient_id", defaultValue = "0")
    val careRecipientId: Long = 0,

    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "role")
    val role: String,

    @ColumnInfo(name = "joined_at")
    val joinedAt: String
)
