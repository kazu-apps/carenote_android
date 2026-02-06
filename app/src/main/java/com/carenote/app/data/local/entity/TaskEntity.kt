package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["is_completed"]),
        Index(value = ["due_date"]),
        Index(value = ["is_completed", "created_at"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "due_date")
    val dueDate: String? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Int = 0,

    @ColumnInfo(name = "priority")
    val priority: String = "MEDIUM",

    @ColumnInfo(name = "recurrence_frequency")
    val recurrenceFrequency: String = "NONE",

    @ColumnInfo(name = "recurrence_interval")
    val recurrenceInterval: Int = 1,

    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Int = 0,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
