package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["parent_type", "parent_id"]),
        Index(value = ["care_recipient_id"])
    ]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "care_recipient_id", defaultValue = "0")
    val careRecipientId: Long = 0,

    @ColumnInfo(name = "parent_type")
    val parentType: String,

    @ColumnInfo(name = "parent_id")
    val parentId: Long,

    @ColumnInfo(name = "local_uri")
    val localUri: String,

    @ColumnInfo(name = "remote_url")
    val remoteUrl: String? = null,

    @ColumnInfo(name = "upload_status")
    val uploadStatus: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
