package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "care_recipients")
data class CareRecipientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "birth_date")
    val birthDate: String?,

    @ColumnInfo(name = "gender")
    val gender: String,

    @ColumnInfo(name = "nickname", defaultValue = "")
    val nickname: String = "",

    @ColumnInfo(name = "care_level", defaultValue = "")
    val careLevel: String = "",

    @ColumnInfo(name = "medical_history", defaultValue = "")
    val medicalHistory: String = "",

    @ColumnInfo(name = "allergies", defaultValue = "")
    val allergies: String = "",

    @ColumnInfo(name = "memo")
    val memo: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String,

    @ColumnInfo(name = "firestore_id")
    val firestoreId: String? = null
)
