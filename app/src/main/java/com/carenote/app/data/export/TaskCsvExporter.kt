package com.carenote.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.domain.repository.TaskCsvExporterInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.carenote.app.data.util.SecureFileDeleter
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskCsvExporter @Inject constructor(
    @ApplicationContext private val context: Context
) : TaskCsvExporterInterface {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val dueDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override suspend fun export(tasks: List<Task>): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        cleanupStaleCache(exportDir)
        val fileName = "${AppConfig.Export.TASK_CSV_FILE_PREFIX}${System.currentTimeMillis()}.csv"
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF")
                writer.write(buildHeaderRow())
                writer.write("\r\n")
                for (task in tasks) {
                    writer.write(buildDataRow(task))
                    writer.write("\r\n")
                }
            }
        }

        Timber.d("Task CSV exported: ${file.length()} bytes, ${tasks.size} records")

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun buildHeaderRow(): String {
        val headers = listOf(
            context.getString(R.string.task_export_header_title),
            context.getString(R.string.task_export_header_description),
            context.getString(R.string.task_export_header_due_date),
            context.getString(R.string.task_export_header_priority),
            context.getString(R.string.task_export_header_status),
            context.getString(R.string.task_export_header_recurrence),
            context.getString(R.string.task_export_header_created_at)
        )
        return headers.joinToString(",") { it.escapeCsv() }
    }

    private fun buildDataRow(task: Task): String {
        val fields = listOf(
            task.title,
            task.description,
            task.dueDate?.format(dueDateFormatter) ?: "",
            localizePriority(task.priority),
            if (task.isCompleted) {
                context.getString(R.string.tasks_completed)
            } else {
                context.getString(R.string.tasks_incomplete)
            },
            localizeRecurrence(task.recurrenceFrequency),
            task.createdAt.format(dateFormatter)
        )
        return fields.joinToString(",") { it.escapeCsv() }
    }

    private fun localizePriority(priority: TaskPriority): String = when (priority) {
        TaskPriority.LOW -> context.getString(R.string.tasks_task_priority_low)
        TaskPriority.MEDIUM -> context.getString(R.string.tasks_task_priority_medium)
        TaskPriority.HIGH -> context.getString(R.string.tasks_task_priority_high)
    }

    private fun localizeRecurrence(frequency: RecurrenceFrequency): String = when (frequency) {
        RecurrenceFrequency.NONE -> context.getString(R.string.tasks_recurrence_none)
        RecurrenceFrequency.DAILY -> context.getString(R.string.tasks_recurrence_daily)
        RecurrenceFrequency.WEEKLY -> context.getString(R.string.tasks_recurrence_weekly)
        RecurrenceFrequency.MONTHLY -> context.getString(R.string.tasks_recurrence_monthly)
    }

    private fun cleanupStaleCache(dir: File) {
        val now = System.currentTimeMillis()
        dir.listFiles()?.filter {
            now - it.lastModified() > AppConfig.Export.CACHE_MAX_AGE_MS
        }?.forEach { SecureFileDeleter.delete(it) }
    }
}
