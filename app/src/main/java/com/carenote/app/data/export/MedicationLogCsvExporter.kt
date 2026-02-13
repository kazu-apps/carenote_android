package com.carenote.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.repository.MedicationLogCsvExporterInterface
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
class MedicationLogCsvExporter @Inject constructor(
    @ApplicationContext private val context: Context
) : MedicationLogCsvExporterInterface {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override suspend fun export(
        logs: List<MedicationLog>,
        medicationNames: Map<Long, String>
    ): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        cleanupStaleCache(exportDir)
        val fileName = "${AppConfig.Export.MEDICATION_LOG_CSV_FILE_PREFIX}${System.currentTimeMillis()}.csv"
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF")
                writer.write(buildHeaderRow())
                writer.write("\r\n")
                for (log in logs) {
                    writer.write(buildDataRow(log, medicationNames))
                    writer.write("\r\n")
                }
            }
        }

        Timber.d("Medication log CSV exported: ${file.length()} bytes, ${logs.size} records")

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun buildHeaderRow(): String {
        val headers = listOf(
            context.getString(R.string.medication_log_export_header_recorded_at),
            context.getString(R.string.medication_log_export_header_scheduled_at),
            context.getString(R.string.medication_log_export_header_medication_name),
            context.getString(R.string.medication_log_export_header_status),
            context.getString(R.string.medication_log_export_header_timing),
            context.getString(R.string.medication_log_export_header_memo)
        )
        return headers.joinToString(",") { escapeCsv(it) }
    }

    private fun buildDataRow(log: MedicationLog, medicationNames: Map<Long, String>): String {
        val fields = listOf(
            log.recordedAt.format(dateFormatter),
            log.scheduledAt.format(dateFormatter),
            medicationNames[log.medicationId] ?: "",
            localizeStatus(log.status),
            log.timing?.let { localizeTiming(it) } ?: "",
            log.memo
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

    private fun cleanupStaleCache(dir: File) {
        val now = System.currentTimeMillis()
        dir.listFiles()?.filter {
            now - it.lastModified() > AppConfig.Export.CACHE_MAX_AGE_MS
        }?.forEach { SecureFileDeleter.delete(it) }
    }
}
