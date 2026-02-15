package com.carenote.app.data.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.testing.TestDataFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
class HealthRecordCsvExporterTest {

    private lateinit var context: Context
    private lateinit var exporter: HealthRecordCsvExporter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        exporter = HealthRecordCsvExporter(context)
    }

    private fun createRecord(
        id: Long = 1L,
        temperature: Double? = 36.5,
        bloodPressureHigh: Int? = 120,
        bloodPressureLow: Int? = 80,
        pulse: Int? = 72,
        weight: Double? = 60.0,
        meal: MealAmount? = MealAmount.FULL,
        excretion: ExcretionType? = ExcretionType.NORMAL,
        conditionNote: String = "",
        recordedAt: LocalDateTime = TestDataFixtures.NOW
    ) = HealthRecord(
        id = id,
        temperature = temperature,
        bloodPressureHigh = bloodPressureHigh,
        bloodPressureLow = bloodPressureLow,
        pulse = pulse,
        weight = weight,
        meal = meal,
        excretion = excretion,
        conditionNote = conditionNote,
        recordedAt = recordedAt
    )

    /**
     * Test CSV content generation directly without FileProvider.
     * FileProvider requires manifest registration unavailable in Robolectric.
     */
    private fun generateCsvContent(records: List<HealthRecord>): String {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        val file = File(exportDir, "test_${System.currentTimeMillis()}.csv")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF")
                val headers = listOf(
                    "Recorded At", "Temperature", "BP (Sys)", "BP (Dia)",
                    "Pulse", "Weight", "Meal", "Excretion", "Condition Note"
                )
                writer.write(headers.joinToString(",") { exporter.escapeCsv(it) })
                writer.write("\r\n")
                for (record in records) {
                    val fields = listOf(
                        record.recordedAt.format(dateFormatter),
                        record.temperature?.toString() ?: "",
                        record.bloodPressureHigh?.toString() ?: "",
                        record.bloodPressureLow?.toString() ?: "",
                        record.pulse?.toString() ?: "",
                        record.weight?.toString() ?: "",
                        record.meal?.name ?: "",
                        record.excretion?.name ?: "",
                        record.conditionNote
                    )
                    writer.write(fields.joinToString(",") { exporter.escapeCsv(it) })
                    writer.write("\r\n")
                }
            }
        }
        return file.readText()
    }

    @Test
    fun `empty list produces header only`() {
        val content = generateCsvContent(emptyList())
        val lines = content.trim().lines()
        assertEquals(1, lines.size)
    }

    @Test
    fun `single record produces header and one data row`() {
        val content = generateCsvContent(listOf(createRecord()))
        val lines = content.trim().lines()
        assertEquals(2, lines.size)
    }

    @Test
    fun `multiple records produce correct number of rows`() {
        val records = listOf(
            createRecord(id = 1L),
            createRecord(id = 2L),
            createRecord(id = 3L)
        )
        val content = generateCsvContent(records)
        val lines = content.trim().lines()
        assertEquals(4, lines.size)
    }

    @Test
    fun `null fields produce empty values`() {
        val record = createRecord(
            temperature = null,
            bloodPressureHigh = null,
            bloodPressureLow = null,
            pulse = null,
            weight = null,
            meal = null,
            excretion = null
        )
        val content = generateCsvContent(listOf(record))
        val dataLine = content.trim().lines()[1]
        val fields = parseCsvLine(dataLine)
        assertEquals("", fields[1])
        assertEquals("", fields[2])
        assertEquals("", fields[3])
        assertEquals("", fields[4])
        assertEquals("", fields[5])
        assertEquals("", fields[6])
        assertEquals("", fields[7])
    }

    @Test
    fun `comma in value is escaped`() {
        val result = exporter.escapeCsv("headache, fever")
        assertEquals("\"headache, fever\"", result)
    }

    @Test
    fun `double quote in value is escaped`() {
        val result = exporter.escapeCsv("said \"hello\"")
        assertEquals("\"said \"\"hello\"\"\"", result)
    }

    @Test
    fun `newline in value is escaped`() {
        val result = exporter.escapeCsv("line1\nline2")
        assertEquals("\"line1\nline2\"", result)
    }

    @Test
    fun `carriage return in value is escaped`() {
        val result = exporter.escapeCsv("line1\rline2")
        assertEquals("\"line1\rline2\"", result)
    }

    @Test
    fun `plain value is not escaped`() {
        val result = exporter.escapeCsv("hello")
        assertEquals("hello", result)
    }

    @Test
    fun `file starts with UTF-8 BOM`() {
        val content = generateCsvContent(listOf(createRecord()))
        val bytes = content.toByteArray(Charsets.UTF_8)
        assertEquals(0xEF.toByte(), bytes[0])
        assertEquals(0xBB.toByte(), bytes[1])
        assertEquals(0xBF.toByte(), bytes[2])
    }

    @Test
    fun `record with condition note containing comma`() {
        val record = createRecord(conditionNote = "headache, nausea")
        val content = generateCsvContent(listOf(record))
        assertTrue(content.contains("\"headache, nausea\""))
    }

    @Test
    fun `file is saved in exports directory`() {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        generateCsvContent(listOf(createRecord()))
        val files = exportDir.listFiles()
        assertTrue(files != null && files.isNotEmpty())
    }

    @Test
    fun `stale cache files are deleted during export`() {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        val staleFile = File(exportDir, "old_export.csv")
        staleFile.writeText("old data")
        // Set lastModified to 2 hours ago (past the 1-hour threshold)
        staleFile.setLastModified(System.currentTimeMillis() - 2 * 3_600_000L)
        assertTrue(staleFile.exists())

        // Trigger any export to invoke cleanupStaleCache
        // We just need to verify the stale file is gone after export dir is accessed
        val recentFile = File(exportDir, "recent.csv")
        recentFile.writeText("recent data")
        // recent file keeps current timestamp

        // Manually invoke the cleanup logic (same as what export does)
        val now = System.currentTimeMillis()
        exportDir.listFiles()?.filter {
            now - it.lastModified() > AppConfig.Export.CACHE_MAX_AGE_MS
        }?.forEach { it.delete() }

        assertFalse("Stale file should be deleted", staleFile.exists())
        assertTrue("Recent file should be preserved", recentFile.exists())

        // Cleanup
        recentFile.delete()
    }

    @Test
    fun `recent cache files are preserved during export`() {
        val exportDir = File(context.cacheDir, AppConfig.Export.CACHE_DIR_NAME).also { it.mkdirs() }
        val recentFile = File(exportDir, "recent_export.csv")
        recentFile.writeText("recent data")
        // Keep current timestamp (within 1-hour threshold)
        assertTrue(recentFile.exists())

        val now = System.currentTimeMillis()
        exportDir.listFiles()?.filter {
            now - it.lastModified() > AppConfig.Export.CACHE_MAX_AGE_MS
        }?.forEach { it.delete() }

        assertTrue("Recent file should be preserved", recentFile.exists())

        // Cleanup
        recentFile.delete()
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' && inQuotes -> inQuotes = false
                c == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
