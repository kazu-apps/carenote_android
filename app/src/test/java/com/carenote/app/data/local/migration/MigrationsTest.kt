package com.carenote.app.data.local.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Room Migration オブジェクトのユニットテスト
 * 各 Migration のバージョン番号、SQL 実行、連続性を検証
 */
class MigrationsTest {

    @Test
    fun `MIGRATION_1_2 has correct version numbers`() {
        assertEquals(1, Migrations.MIGRATION_1_2.startVersion)
        assertEquals(2, Migrations.MIGRATION_1_2.endVersion)
    }

    @Test
    fun `MIGRATION_2_3 has correct version numbers`() {
        assertEquals(2, Migrations.MIGRATION_2_3.startVersion)
        assertEquals(3, Migrations.MIGRATION_2_3.endVersion)
    }

    @Test
    fun `MIGRATION_3_4 has correct version numbers`() {
        assertEquals(3, Migrations.MIGRATION_3_4.startVersion)
        assertEquals(4, Migrations.MIGRATION_3_4.endVersion)
    }

    @Test
    fun `MIGRATION_4_5 has correct version numbers`() {
        assertEquals(4, Migrations.MIGRATION_4_5.startVersion)
        assertEquals(5, Migrations.MIGRATION_4_5.endVersion)
    }

    @Test
    fun `MIGRATION_5_6 has correct version numbers`() {
        assertEquals(5, Migrations.MIGRATION_5_6.startVersion)
        assertEquals(6, Migrations.MIGRATION_5_6.endVersion)
    }

    @Test
    fun `MIGRATION_6_7 has correct version numbers`() {
        assertEquals(6, Migrations.MIGRATION_6_7.startVersion)
        assertEquals(7, Migrations.MIGRATION_6_7.endVersion)
    }

    @Test
    fun `MIGRATION_7_8 has correct version numbers`() {
        assertEquals(7, Migrations.MIGRATION_7_8.startVersion)
        assertEquals(8, Migrations.MIGRATION_7_8.endVersion)
    }

    @Test
    fun `all() contains seven migrations in order`() {
        val all = Migrations.all()
        assertEquals(7, all.size)
        assertEquals(1, all[0].startVersion)
        assertEquals(2, all[0].endVersion)
        assertEquals(2, all[1].startVersion)
        assertEquals(3, all[1].endVersion)
        assertEquals(3, all[2].startVersion)
        assertEquals(4, all[2].endVersion)
        assertEquals(4, all[3].startVersion)
        assertEquals(5, all[3].endVersion)
        assertEquals(5, all[4].startVersion)
        assertEquals(6, all[4].endVersion)
        assertEquals(6, all[5].startVersion)
        assertEquals(7, all[5].endVersion)
        assertEquals(7, all[6].startVersion)
        assertEquals(8, all[6].endVersion)
    }

    @Test
    fun `all() returns a new array each call`() {
        val first = Migrations.all()
        val second = Migrations.all()
        assertTrue("all() should return a new array instance", first !== second)
    }

    @Test
    fun `migrations form a continuous chain from version 1 to 8`() {
        val all = Migrations.all()
        for (i in 0 until all.size - 1) {
            assertEquals(
                "Migration ${all[i].startVersion}->${all[i].endVersion} endVersion " +
                    "should match next migration startVersion",
                all[i].endVersion,
                all[i + 1].startVersion
            )
        }
        assertEquals(1, all.first().startVersion)
        assertEquals(8, all.last().endVersion)
    }

    @Test
    fun `MIGRATION_1_2 executes CREATE TABLE notes SQL`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val sqlSlot = slot<String>()
        every { db.execSQL(capture(sqlSlot)) } just Runs

        Migrations.MIGRATION_1_2.migrate(db)

        verify(exactly = 1) { db.execSQL(any()) }
        val sql = sqlSlot.captured
        assertTrue("Should create notes table", sql.contains("CREATE TABLE IF NOT EXISTS `notes`"))
        assertTrue("Should have title column", sql.contains("`title` TEXT NOT NULL"))
        assertTrue("Should have content column", sql.contains("`content` TEXT NOT NULL"))
        assertTrue("Should have tag column", sql.contains("`tag` TEXT NOT NULL"))
        assertTrue("Should have author_id column", sql.contains("`author_id` TEXT NOT NULL"))
        assertTrue("Should have created_at column", sql.contains("`created_at` TEXT NOT NULL"))
        assertTrue("Should have updated_at column", sql.contains("`updated_at` TEXT NOT NULL"))
    }

    @Test
    fun `MIGRATION_2_3 executes CREATE TABLE health_records SQL`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val sqlSlot = slot<String>()
        every { db.execSQL(capture(sqlSlot)) } just Runs

        Migrations.MIGRATION_2_3.migrate(db)

        verify(exactly = 1) { db.execSQL(any()) }
        val sql = sqlSlot.captured
        assertTrue("Should create health_records table", sql.contains("CREATE TABLE IF NOT EXISTS `health_records`"))
        assertTrue("Should have temperature column", sql.contains("`temperature` REAL"))
        assertTrue("Should have blood_pressure_high column", sql.contains("`blood_pressure_high` INTEGER"))
        assertTrue("Should have blood_pressure_low column", sql.contains("`blood_pressure_low` INTEGER"))
        assertTrue("Should have pulse column", sql.contains("`pulse` INTEGER"))
        assertTrue("Should have weight column", sql.contains("`weight` REAL"))
        assertTrue("Should have condition_note column", sql.contains("`condition_note` TEXT NOT NULL"))
        assertTrue("Should have recorded_at column", sql.contains("`recorded_at` TEXT NOT NULL"))
    }

    @Test
    fun `MIGRATION_3_4 executes CREATE TABLE calendar_events SQL`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val sqlSlot = slot<String>()
        every { db.execSQL(capture(sqlSlot)) } just Runs

        Migrations.MIGRATION_3_4.migrate(db)

        verify(exactly = 1) { db.execSQL(any()) }
        val sql = sqlSlot.captured
        assertTrue("Should create calendar_events table", sql.contains("CREATE TABLE IF NOT EXISTS `calendar_events`"))
        assertTrue("Should have title column", sql.contains("`title` TEXT NOT NULL"))
        assertTrue("Should have description column", sql.contains("`description` TEXT NOT NULL"))
        assertTrue("Should have date column", sql.contains("`date` TEXT NOT NULL"))
        assertTrue("Should have start_time column", sql.contains("`start_time` TEXT"))
        assertTrue("Should have end_time column", sql.contains("`end_time` TEXT"))
        assertTrue("Should have is_all_day column", sql.contains("`is_all_day` INTEGER NOT NULL"))
    }

    @Test
    fun `MIGRATION_4_5 executes CREATE TABLE tasks SQL`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val sqlSlot = slot<String>()
        every { db.execSQL(capture(sqlSlot)) } just Runs

        Migrations.MIGRATION_4_5.migrate(db)

        verify(exactly = 1) { db.execSQL(any()) }
        val sql = sqlSlot.captured
        assertTrue("Should create tasks table", sql.contains("CREATE TABLE IF NOT EXISTS `tasks`"))
        assertTrue("Should have title column", sql.contains("`title` TEXT NOT NULL"))
        assertTrue("Should have description column", sql.contains("`description` TEXT NOT NULL"))
        assertTrue("Should have due_date column", sql.contains("`due_date` TEXT"))
        assertTrue("Should have is_completed column", sql.contains("`is_completed` INTEGER NOT NULL"))
        assertTrue("Should have priority column", sql.contains("`priority` TEXT NOT NULL"))
    }

    @Test
    fun `MIGRATION_5_6 executes six CREATE INDEX statements`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val sqlStatements = mutableListOf<String>()
        every { db.execSQL(capture(sqlStatements)) } just Runs

        Migrations.MIGRATION_5_6.migrate(db)

        verify(exactly = 6) { db.execSQL(any()) }
        assertEquals(6, sqlStatements.size)
        assertTrue("Should create index on notes.tag", sqlStatements[0].contains("index_notes_tag"))
        assertTrue("Should create index on notes.created_at", sqlStatements[1].contains("index_notes_created_at"))
        assertTrue(
            "Should create index on health_records.recorded_at",
            sqlStatements[2].contains("index_health_records_recorded_at")
        )
        assertTrue(
            "Should create index on calendar_events.date",
            sqlStatements[3].contains("index_calendar_events_date")
        )
        assertTrue(
            "Should create index on tasks.is_completed",
            sqlStatements[4].contains("index_tasks_is_completed")
        )
        assertTrue("Should create index on tasks.due_date", sqlStatements[5].contains("index_tasks_due_date"))
    }

    @Test
    fun `MIGRATION_6_7 creates sync_mappings table with indexes`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val sqlStatements = mutableListOf<String>()
        every { db.execSQL(capture(sqlStatements)) } just Runs

        Migrations.MIGRATION_6_7.migrate(db)

        verify(exactly = 3) { db.execSQL(any()) }
        assertEquals(3, sqlStatements.size)
        assertTrue(
            "Should create sync_mappings table",
            sqlStatements[0].contains("CREATE TABLE IF NOT EXISTS `sync_mappings`")
        )
        assertTrue(
            "Should have entity_type column",
            sqlStatements[0].contains("`entity_type` TEXT NOT NULL")
        )
        assertTrue(
            "Should have local_id column",
            sqlStatements[0].contains("`local_id` INTEGER NOT NULL")
        )
        assertTrue(
            "Should have remote_id column",
            sqlStatements[0].contains("`remote_id` TEXT NOT NULL")
        )
        assertTrue(
            "Should create unique index on entity_type and local_id",
            sqlStatements[1].contains("index_sync_mappings_entity_type_local_id")
        )
        assertTrue(
            "Should create unique index on entity_type and remote_id",
            sqlStatements[2].contains("index_sync_mappings_entity_type_remote_id")
        )
    }

    @Test
    fun `MIGRATION_7_8 adds timing column to medication_logs`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val sqlSlot = slot<String>()
        every { db.execSQL(capture(sqlSlot)) } just Runs

        Migrations.MIGRATION_7_8.migrate(db)

        verify(exactly = 1) { db.execSQL(any()) }
        val sql = sqlSlot.captured
        assertTrue(
            "Should ALTER TABLE medication_logs ADD COLUMN timing",
            sql.contains("ALTER TABLE medication_logs ADD COLUMN timing TEXT")
        )
    }
}
