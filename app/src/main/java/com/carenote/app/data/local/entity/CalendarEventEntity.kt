package com.carenote.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_events",
    indices = [
        Index(value = ["date"]),
        Index(value = ["care_recipient_id"])
    ]
)
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "care_recipient_id", defaultValue = "0")
    val careRecipientId: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "start_time")
    val startTime: String? = null,

    @ColumnInfo(name = "end_time")
    val endTime: String? = null,

    @ColumnInfo(name = "is_all_day")
    val isAllDay: Int = 1,

    @ColumnInfo(name = "type")
    val type: String = "OTHER",

    @ColumnInfo(name = "completed")
    val completed: Int = 0,

    @ColumnInfo(name = "recurrence_frequency")
    val recurrenceFrequency: String = "NONE",

    @ColumnInfo(name = "recurrence_interval")
    val recurrenceInterval: Int = 1,

    @ColumnInfo(name = "priority")
    val priority: String? = null,

    @ColumnInfo(name = "reminder_enabled", defaultValue = "0")
    val reminderEnabled: Int = 0,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: String? = null,

    @ColumnInfo(name = "created_by", defaultValue = "")
    val createdBy: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
