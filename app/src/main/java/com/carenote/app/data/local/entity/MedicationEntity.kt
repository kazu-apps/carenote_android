package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "dosage")
    val dosage: String = "",

    @ColumnInfo(name = "timings")
    val timings: String = "",

    @ColumnInfo(name = "times")
    val times: String = "",

    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
