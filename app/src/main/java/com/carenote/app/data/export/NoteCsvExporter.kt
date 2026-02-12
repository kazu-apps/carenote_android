package com.carenote.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.repository.NoteCsvExporterInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCsvExporter @Inject constructor(
    @ApplicationContext private val context: Context
) : NoteCsvExporterInterface {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override suspend fun export(notes: List<Note>): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        val fileName = "${AppConfig.Export.NOTE_CSV_FILE_PREFIX}${System.currentTimeMillis()}.csv"
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF")
                writer.write(buildHeaderRow())
                writer.write("\r\n")
                for (note in notes) {
                    writer.write(buildDataRow(note))
                    writer.write("\r\n")
                }
            }
        }

        Timber.d("Note CSV exported: ${file.length()} bytes, ${notes.size} records")

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun buildHeaderRow(): String {
        val headers = listOf(
            context.getString(R.string.note_export_header_title),
            context.getString(R.string.note_export_header_content),
            context.getString(R.string.note_export_header_tag),
            context.getString(R.string.note_export_header_created_at),
            context.getString(R.string.note_export_header_updated_at)
        )
        return headers.joinToString(",") { escapeCsv(it) }
    }

    private fun buildDataRow(note: Note): String {
        val fields = listOf(
            note.title,
            note.content,
            localizeTag(note.tag),
            note.createdAt.format(dateFormatter),
            note.updatedAt.format(dateFormatter)
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

    private fun localizeTag(tag: NoteTag): String = when (tag) {
        NoteTag.CONDITION -> context.getString(R.string.notes_tag_condition)
        NoteTag.MEAL -> context.getString(R.string.notes_tag_meal)
        NoteTag.REPORT -> context.getString(R.string.notes_tag_report)
        NoteTag.OTHER -> context.getString(R.string.notes_tag_other)
    }
}
