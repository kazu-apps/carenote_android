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
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.repository.HealthRecordPdfExporterInterface
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
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
class HealthRecordPdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthRecordPdfExporterInterface {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private val columnWidths = intArrayOf(80, 45, 40, 40, 35, 40, 50, 50, 135)

    override suspend fun export(records: List<HealthRecord>): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        cleanupStaleCache(exportDir)
        val fileName = "${AppConfig.Export.PDF_FILE_PREFIX}${System.currentTimeMillis()}.pdf"
        val file = File(exportDir, fileName)

        val document = PdfDocument()
        try {
            var pageNumber = 1
            var page = startNewPage(document, pageNumber)
            var canvas = page.canvas
            var yPos = drawTitle(canvas)
            yPos = drawTableHeader(canvas, yPos)

            for (record in records) {
                if (yPos + AppConfig.Export.PDF_LINE_HEIGHT > AppConfig.Export.PDF_PAGE_HEIGHT - AppConfig.Export.PDF_MARGIN) {
                    document.finishPage(page)
                    pageNumber++
                    page = startNewPage(document, pageNumber)
                    canvas = page.canvas
                    yPos = AppConfig.Export.PDF_MARGIN.toFloat() + AppConfig.Export.PDF_HEADER_LINE_HEIGHT
                    yPos = drawTableHeader(canvas, yPos - AppConfig.Export.PDF_HEADER_LINE_HEIGHT)
                }
                yPos = drawDataRow(canvas, record, yPos)
            }

            document.finishPage(page)

            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }
        } finally {
            document.close()
        }

        Timber.d("PDF exported: ${file.length()} bytes, ${records.size} records")

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
        val title = context.getString(R.string.health_records_export_title)
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

    private fun drawDataRow(canvas: Canvas, record: HealthRecord, yPos: Float): Float {
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
            record.recordedAt.format(dateFormatter),
            record.temperature?.let { "%.1f".format(it) } ?: "",
            record.bloodPressureHigh?.toString() ?: "",
            record.bloodPressureLow?.toString() ?: "",
            record.pulse?.toString() ?: "",
            record.weight?.let { "%.1f".format(it) } ?: "",
            record.meal?.let { localizeMealAmount(it) } ?: "",
            record.excretion?.let { localizeExcretionType(it) } ?: "",
            record.conditionNote
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
        context.getString(R.string.export_header_recorded_at),
        context.getString(R.string.export_header_temperature),
        context.getString(R.string.export_header_bp_high),
        context.getString(R.string.export_header_bp_low),
        context.getString(R.string.export_header_pulse),
        context.getString(R.string.export_header_weight),
        context.getString(R.string.export_header_meal),
        context.getString(R.string.export_header_excretion),
        context.getString(R.string.export_header_condition_note)
    )

    private fun localizeMealAmount(meal: MealAmount): String = when (meal) {
        MealAmount.FULL -> context.getString(R.string.meal_full)
        MealAmount.MOSTLY -> context.getString(R.string.meal_mostly)
        MealAmount.HALF -> context.getString(R.string.meal_half)
        MealAmount.LITTLE -> context.getString(R.string.meal_little)
        MealAmount.NONE -> context.getString(R.string.meal_none)
    }

    private fun localizeExcretionType(excretion: ExcretionType): String = when (excretion) {
        ExcretionType.NORMAL -> context.getString(R.string.excretion_normal)
        ExcretionType.SOFT -> context.getString(R.string.excretion_soft)
        ExcretionType.HARD -> context.getString(R.string.excretion_hard)
        ExcretionType.DIARRHEA -> context.getString(R.string.excretion_diarrhea)
        ExcretionType.NONE -> context.getString(R.string.excretion_none)
    }

    private fun cleanupStaleCache(dir: File) {
        val now = System.currentTimeMillis()
        dir.listFiles()?.filter {
            now - it.lastModified() > AppConfig.Export.CACHE_MAX_AGE_MS
        }?.forEach { SecureFileDeleter.delete(it) }
    }
}
