package com.carenote.app.data.local

import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DatabaseRecoveryHelperTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var helper: DatabaseRecoveryHelper
    private lateinit var dbFile: File

    @Before
    fun setUp() {
        helper = spyk(DatabaseRecoveryHelper())
        dbFile = File(tempFolder.root, "test.db")
    }

    @Test
    fun `recoverIfNeeded does nothing when database file does not exist`() {
        helper.recoverIfNeeded(dbFile, ByteArray(32))

        verify(exactly = 0) { helper.canOpenDatabase(any(), any()) }
        verify(exactly = 0) { helper.deleteDatabaseFiles(any()) }
    }

    @Test
    fun `recoverIfNeeded does nothing when passphrase matches`() {
        dbFile.writeText("fake-db-content")
        every { helper.canOpenDatabase(dbFile, any()) } returns true

        helper.recoverIfNeeded(dbFile, ByteArray(32))

        verify(exactly = 0) { helper.deleteDatabaseFiles(any()) }
        assertTrue(dbFile.exists())
    }

    @Test
    fun `recoverIfNeeded deletes database when passphrase does not match`() {
        dbFile.writeText("fake-db-content")
        every { helper.canOpenDatabase(dbFile, any()) } returns false

        helper.recoverIfNeeded(dbFile, ByteArray(32))

        verify(exactly = 1) { helper.deleteDatabaseFiles(dbFile) }
        assertFalse(dbFile.exists())
    }

    @Test
    fun `deleteDatabaseFiles removes all auxiliary files`() {
        dbFile.writeText("db")
        val walFile = File("${dbFile.absolutePath}-wal").apply { writeText("wal") }
        val shmFile = File("${dbFile.absolutePath}-shm").apply { writeText("shm") }
        val journalFile = File("${dbFile.absolutePath}-journal").apply { writeText("journal") }

        helper.deleteDatabaseFiles(dbFile)

        assertFalse(dbFile.exists())
        assertFalse(walFile.exists())
        assertFalse(shmFile.exists())
        assertFalse(journalFile.exists())
    }
}
