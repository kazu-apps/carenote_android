package com.carenote.app.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.di.WidgetEntryPoint
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.Task
import com.carenote.app.ui.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class CareNoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )

        val medications = entryPoint.medicationRepository()
            .getAllMedications()
            .first()
            .take(AppConfig.Widget.MAX_MEDICATION_ITEMS)

        val todayLogs = entryPoint.medicationLogRepository()
            .getLogsForDate(LocalDate.now())
            .first()

        val tasks = entryPoint.taskRepository()
            .getTasksByDueDate(LocalDate.now())
            .first()
            .filter { !it.isCompleted }
            .take(AppConfig.Widget.MAX_TASK_ITEMS)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    context = context,
                    medications = medications,
                    todayLogs = todayLogs,
                    tasks = tasks
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    context: Context,
    medications: List<Medication>,
    todayLogs: List<MedicationLog>,
    tasks: List<Task>
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // Medication section
        Text(
            text = context.getString(R.string.widget_medication_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GlanceTheme.colors.onBackground
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))

        if (medications.isEmpty()) {
            Text(
                text = context.getString(R.string.widget_no_medications),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        } else {
            medications.forEach { medication ->
                MedicationRow(context, medication, todayLogs)
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Task section
        Text(
            text = context.getString(R.string.widget_tasks_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GlanceTheme.colors.onBackground
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))

        if (tasks.isEmpty()) {
            Text(
                text = context.getString(R.string.widget_no_tasks),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        } else {
            tasks.forEach { task ->
                TaskRow(context, task)
            }
        }
    }
}

@Composable
private fun MedicationRow(
    context: Context,
    medication: Medication,
    todayLogs: List<MedicationLog>
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = medication.name,
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onBackground
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.width(4.dp))

        val timingStatuses = medication.timings.map { timing ->
            val log = todayLogs.find {
                it.medicationId == medication.id && it.timing == timing
            }
            timing to log?.status
        }

        if (timingStatuses.isEmpty()) {
            val anyLog = todayLogs.find { it.medicationId == medication.id }
            Text(
                text = statusLabel(context, anyLog?.status),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        } else {
            timingStatuses.forEach { (timing, status) ->
                Text(
                    text = "${timingLabel(timing)}${statusLabel(context, status)}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun TaskRow(context: Context, task: Task) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "\u2610",
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = task.title,
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onBackground
            ),
            maxLines = 1
        )
    }
}

private fun timingLabel(timing: MedicationTiming): String =
    when (timing) {
        MedicationTiming.MORNING -> "\u2600"
        MedicationTiming.NOON -> "\u2B50"
        MedicationTiming.EVENING -> "\uD83C\uDF19"
    }

private fun statusLabel(context: Context, status: MedicationLogStatus?): String =
    when (status) {
        MedicationLogStatus.TAKEN -> context.getString(R.string.widget_status_taken)
        MedicationLogStatus.SKIPPED -> context.getString(R.string.widget_status_skipped)
        MedicationLogStatus.POSTPONED, null -> context.getString(R.string.widget_status_pending)
    }
