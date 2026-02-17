package com.carenote.app.domain.model

fun CalendarEvent.validate(): List<String> {
    val errors = mutableListOf<String>()
    if (!isTask && priority != null) errors.add("priority is only for TASK type")
    if (!isTask && reminderEnabled) errors.add("reminderEnabled is only for TASK type")
    if (title.isBlank()) errors.add("title must not be blank")
    return errors
}
