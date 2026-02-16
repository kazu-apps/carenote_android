package com.carenote.app.ui.screens.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.ui.components.CareNoteCard
import com.carenote.app.ui.theme.CareNoteColors
import com.carenote.app.ui.theme.ChipShape
import com.carenote.app.ui.util.DateTimeFormatters

@Composable
fun TaskCard(
    task: Task,
    onToggleCompletion: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CareNoteCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion() }
            )
            TaskCardContent(task = task)
            PriorityBadge(priority = task.priority)
        }
    }
}

@Composable
private fun RowScope.TaskCardContent(task: Task) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium.copy(
                textDecoration = if (task.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }
            ),
            color = if (task.isCompleted) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = AppConfig.Task.TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis
        )

        if (task.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = AppConfig.Task.DESCRIPTION_PREVIEW_MAX_LINES,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (task.dueDate != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = DateTimeFormatters.formatDate(task.dueDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    if (priority == TaskPriority.MEDIUM) return

    val careNoteColors = CareNoteColors.current
    val (containerColor, contentColor, labelResId) = when (priority) {
        TaskPriority.HIGH -> Triple(
            careNoteColors.taskPriorityHighColor,
            Color.White,
            R.string.tasks_task_priority_high
        )
        TaskPriority.LOW -> Triple(
            careNoteColors.taskPriorityLowColor,
            Color.White,
            R.string.tasks_task_priority_low
        )
        else -> return
    }

    Surface(
        shape = ChipShape,
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = stringResource(labelResId),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
