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
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.repository.MedicationLogPdfExporterInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationLogPdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) : MedicationLogPdfExporterInterface {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private val columnWidths = intArrayOf(80, 80, 110, 60, 50, 135)

    override suspend fun export(
        logs: List<MedicationLog>,
        medicationNames: Map<Long, String>
    ): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        val fileName = "${AppConfig.Export.MEDICATION_LOG_PDF_FILE_PREFIX}${System.currentTimeMillis()}.pdf"
        val file = File(exportDir, fileName)

        val document = PdfDocument()
        try {
            var pageNumber = 1
            var page = startNewPage(document, pageNumber)
            var canvas = page.canvas
            var yPos = drawTitle(canvas)
            yPos = drawTableHeader(canvas, yPos)

            for (log in logs) {
                if (yPos + AppConfig.Export.PDF_LINE_HEIGHT > AppConfig.Export.PDF_PAGE_HEIGHT - AppConfig.Export.PDF_MARGIN) {
                    document.finishPage(page)
                    pageNumber++
                    page = startNewPage(document, pageNumber)
                    canvas = page.canvas
                    yPos = AppConfig.Export.PDF_MARGIN.toFloat() + AppConfig.Export.PDF_HEADER_LINE_HEIGHT
                    yPos = drawTableHeader(canvas, yPos - AppConfig.Export.PDF_HEADER_LINE_HEIGHT)
                }
                yPos = drawDataRow(canvas, log, medicationNames, yPos)
            }

            document.finishPage(page)

            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }
        } finally {
            document.close()
        }

        Timber.d("Medication log PDF exported: ${file.length()} bytes, ${logs.size} records")

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
        val title = context.getString(R.string.medication_log_export_title)
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
        log: MedicationLog,
        medicationNames: Map<Long, String>,
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

        canvas.drawLine(margin, yPos + AppConfig.Export.PDF_LINE_HEIGHT, margin + totalWidth, yPos + AppConfig.Export.PDF_LINE_HEIGHT, linePaint)

        val fields = listOf(
            log.recordedAt.format(dateFormatter),
            log.scheduledAt.format(dateFormatter),
            medicationNames[log.medicationId] ?: "",
            localizeStatus(log.status),
            log.timing?.let { localizeTiming(it) } ?: "",
            log.memo
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
        context.getString(R.string.medication_log_export_header_recorded_at),
        context.getString(R.string.medication_log_export_header_scheduled_at),
        context.getString(R.string.medication_log_export_header_medication_name),
        context.getString(R.string.medication_log_export_header_status),
        context.getString(R.string.medication_log_export_header_timing),
        context.getString(R.string.medication_log_export_header_memo)
    )

    private fun localizeStatus(status: MedicationLogStatus): String = when (status) {
        MedicationLogStatus.TAKEN -> context.getString(R.string.medication_status_taken)
        MedicationLogStatus.SKIPPED -> context.getString(R.string.medication_status_skipped)
        MedicationLogStatus.POSTPONED -> context.getString(R.string.medication_status_postponed)
    }

    private fun localizeTiming(timing: MedicationTiming): String = when (timing) {
        MedicationTiming.MORNING -> context.getString(R.string.medication_morning)
        MedicationTiming.NOON -> context.getString(R.string.medication_noon)
        MedicationTiming.EVENING -> context.getString(R.string.medication_evening)
    }
}
