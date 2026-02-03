package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.TaskEntity
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskMapper @Inject constructor() : Mapper<TaskEntity, Task> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun toDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            dueDate = entity.dueDate?.let { LocalDate.parse(it, dateFormatter) },
            isCompleted = entity.isCompleted == 1,
            priority = parseTaskPriority(entity.priority),
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter)
        )
    }

    private fun parseTaskPriority(value: String): TaskPriority {
        return try {
            TaskPriority.valueOf(value)
        } catch (_: IllegalArgumentException) {
            TaskPriority.MEDIUM
        }
    }

    override fun toEntity(domain: Task): TaskEntity {
        return TaskEntity(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            dueDate = domain.dueDate?.format(dateFormatter),
            isCompleted = if (domain.isCompleted) 1 else 0,
            priority = domain.priority.name,
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter)
        )
    }
}
