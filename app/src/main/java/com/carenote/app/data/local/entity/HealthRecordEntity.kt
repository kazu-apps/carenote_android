package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_records",
    indices = [
        Index(value = ["recorded_at"])
    ]
)
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "temperature")
    val temperature: Double? = null,

    @ColumnInfo(name = "blood_pressure_high")
    val bloodPressureHigh: Int? = null,

    @ColumnInfo(name = "blood_pressure_low")
    val bloodPressureLow: Int? = null,

    @ColumnInfo(name = "pulse")
    val pulse: Int? = null,

    @ColumnInfo(name = "weight")
    val weight: Double? = null,

    @ColumnInfo(name = "meal")
    val meal: String? = null,

    @ColumnInfo(name = "excretion")
    val excretion: String? = null,

    @ColumnInfo(name = "condition_note")
    val conditionNote: String = "",

    @ColumnInfo(name = "recorded_at")
    val recordedAt: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
