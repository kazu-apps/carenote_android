package com.carenote.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.repository.HealthRecordCsvExporterInterface
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
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
class HealthRecordCsvExporter @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthRecordCsvExporterInterface {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override suspend fun export(records: List<HealthRecord>): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        cleanupStaleCache(exportDir)
        val fileName = "${AppConfig.Export.CSV_FILE_PREFIX}${System.currentTimeMillis()}.csv"
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF")
                writer.write(buildHeaderRow())
                writer.write("\r\n")
                for (record in records) {
                    writer.write(buildDataRow(record))
                    writer.write("\r\n")
                }
            }
        }

        Timber.d("CSV exported: ${file.length()} bytes, ${records.size} records")

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun buildHeaderRow(): String {
        val headers = listOf(
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
        return headers.joinToString(",") { escapeCsv(it) }
    }

    private fun buildDataRow(record: HealthRecord): String {
        val fields = listOf(
            record.recordedAt.format(dateFormatter),
            record.temperature?.toString() ?: "",
            record.bloodPressureHigh?.toString() ?: "",
            record.bloodPressureLow?.toString() ?: "",
            record.pulse?.toString() ?: "",
            record.weight?.toString() ?: "",
            record.meal?.let { localizeMealAmount(it) } ?: "",
            record.excretion?.let { localizeExcretionType(it) } ?: "",
            record.conditionNote
        )
        return fields.joinToString(",") { escapeCsv(it) }
    }

    internal fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

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
