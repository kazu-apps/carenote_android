package com.carenote.app.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.domain.repository.TaskPdfExporterInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.carenote.app.data.util.SecureFileDeleter
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskPdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) : TaskPdfExporterInterface {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val dueDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val columnWidths = intArrayOf(90, 120, 65, 45, 50, 55, 90)

    override suspend fun export(events: List<CalendarEvent>): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        cleanupStaleCache(exportDir)
        val fileName = "${AppConfig.Export.TASK_PDF_FILE_PREFIX}${System.currentTimeMillis()}.pdf"
        val file = File(exportDir, fileName)

        val document = PdfDocument()
        try {
            var pageNumber = 1
            var page = startNewPage(document, pageNumber)
            var canvas = page.canvas
            var yPos = drawTitle(canvas)
            yPos = drawTableHeader(canvas, yPos)

            for (event in events) {
                val maxY = AppConfig.Export.PDF_PAGE_HEIGHT - AppConfig.Export.PDF_MARGIN
                if (yPos + AppConfig.Export.PDF_LINE_HEIGHT > maxY) {
                    document.finishPage(page)
                    pageNumber++
                    page = startNewPage(document, pageNumber)
                    canvas = page.canvas
                    yPos = AppConfig.Export.PDF_MARGIN.toFloat() + AppConfig.Export.PDF_HEADER_LINE_HEIGHT
                    yPos = drawTableHeader(canvas, yPos - AppConfig.Export.PDF_HEADER_LINE_HEIGHT)
                }
                yPos = drawDataRow(canvas, event, yPos)
            }

            document.finishPage(page)

            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }
        } finally {
            document.close()
        }

        Timber.d("Task PDF exported: ${file.length()} bytes, ${events.size} records")

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun startNewPage(document: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(
            AppConfig.Export.PDF_PAGE_WIDTH,
            AppConfig.Export.PDF_PAGE_HEIGHT,
            pageNumber
        ).create()
        return document.startPage(pageInfo)
    }

    private fun drawTitle(canvas: Canvas): Float {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = AppConfig.Export.PDF_FONT_SIZE_TITLE
            isFakeBoldText = true
        }
        val title = context.getString(R.string.task_export_title)
        val y = AppConfig.Export.PDF_MARGIN.toFloat() + AppConfig.Export.PDF_FONT_SIZE_TITLE
        canvas.drawText(title, AppConfig.Export.PDF_MARGIN.toFloat(), y, paint)
        return y + AppConfig.Export.PDF_HEADER_LINE_HEIGHT + 4f
    }

    private fun drawTableHeader(canvas: Canvas, yPos: Float): Float {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = AppConfig.Export.PDF_FONT_SIZE_HEADER
            isFakeBoldText = true
        }
        val bgPaint = Paint().apply {
            color = Color.rgb(46, 125, 50)
        }
        val margin = AppConfig.Export.PDF_MARGIN.toFloat()
        val totalWidth = columnWidths.sum().toFloat()
        canvas.drawRect(
            margin, yPos,
            margin + totalWidth, yPos + AppConfig.Export.PDF_HEADER_LINE_HEIGHT,
            bgPaint
        )
        val headers = getHeaders()
        var xPos = margin + 4f
        for (i in headers.indices) {
            canvas.drawText(
                truncateText(headers[i], columnWidths[i] - 8, paint),
                xPos,
                yPos + AppConfig.Export.PDF_FONT_SIZE_HEADER + 4f,
                paint
            )
            xPos += columnWidths[i]
        }
        return yPos + AppConfig.Export.PDF_HEADER_LINE_HEIGHT
    }

    private fun drawDataRow(
        canvas: Canvas,
        event: CalendarEvent,
        yPos: Float
    ): Float {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = AppConfig.Export.PDF_FONT_SIZE_BODY
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.5f
        }
        val margin = AppConfig.Export.PDF_MARGIN.toFloat()
        val totalWidth = columnWidths.sum().toFloat()

        val lineY = yPos + AppConfig.Export.PDF_LINE_HEIGHT
        canvas.drawLine(
            margin, lineY, margin + totalWidth, lineY, linePaint
        )

        val fields = listOf(
            event.title,
            event.description,
            event.date.format(dueDateFormatter),
            localizePriority(event.priority ?: TaskPriority.MEDIUM),
            if (event.completed) {
                context.getString(R.string.tasks_completed)
            } else {
                context.getString(R.string.tasks_incomplete)
            },
            localizeRecurrence(event.recurrenceFrequency),
            event.createdAt.format(dateFormatter)
        )
        var xPos = margin + 4f
        for (i in fields.indices) {
            canvas.drawText(
                truncateText(fields[i], columnWidths[i] - 8, paint),
                xPos,
                yPos + AppConfig.Export.PDF_FONT_SIZE_BODY + 3f,
                paint
            )
            xPos += columnWidths[i]
        }
        return yPos + AppConfig.Export.PDF_LINE_HEIGHT
    }

    private fun truncateText(text: String, maxWidth: Int, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return if (truncated.isEmpty()) "" else "$truncated..."
    }

    private fun getHeaders(): List<String> = listOf(
        context.getString(R.string.task_export_header_title),
        context.getString(R.string.task_export_header_description),
        context.getString(R.string.task_export_header_due_date),
        context.getString(R.string.task_export_header_priority),
        context.getString(R.string.task_export_header_status),
        context.getString(R.string.task_export_header_recurrence),
        context.getString(R.string.task_export_header_created_at)
    )

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
