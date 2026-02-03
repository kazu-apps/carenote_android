package com.carenote.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations for CareNote.
 *
 * Each migration adds a new table corresponding to a feature addition.
 * SQL statements are derived from the Room-exported schema JSON files
 * located at: app/schemas/com.carenote.app.data.local.CareNoteDatabase/
 */
object Migrations {

    /** v1 -> v2: Add notes table */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `tag` TEXT NOT NULL,
                    `author_id` TEXT NOT NULL,
                    `created_at` TEXT NOT NULL,
                    `updated_at` TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    /** v2 -> v3: Add health_records table */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `health_records` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `temperature` REAL,
                    `blood_pressure_high` INTEGER,
                    `blood_pressure_low` INTEGER,
                    `pulse` INTEGER,
                    `weight` REAL,
                    `meal` TEXT,
                    `excretion` TEXT,
                    `condition_note` TEXT NOT NULL,
                    `recorded_at` TEXT NOT NULL,
                    `created_at` TEXT NOT NULL,
                    `updated_at` TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    /** v3 -> v4: Add calendar_events table */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `calendar_events` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `date` TEXT NOT NULL,
                    `start_time` TEXT,
                    `end_time` TEXT,
                    `is_all_day` INTEGER NOT NULL,
                    `created_at` TEXT NOT NULL,
                    `updated_at` TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    /** v4 -> v5: Add tasks table */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tasks` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `due_date` TEXT,
                    `is_completed` INTEGER NOT NULL,
                    `priority` TEXT NOT NULL,
                    `created_at` TEXT NOT NULL,
                    `updated_at` TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    /** v5 -> v6: Add indexes to notes, health_records, calendar_events, tasks */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_tag` ON `notes` (`tag`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_created_at` ON `notes` (`created_at`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_records_recorded_at` ON `health_records` (`recorded_at`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_events_date` ON `calendar_events` (`date`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_is_completed` ON `tasks` (`is_completed`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_due_date` ON `tasks` (`due_date`)")
        }
    }

    /** All migrations in order, for convenient registration with Room. Returns a new array each call. */
    fun all(): Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
}
