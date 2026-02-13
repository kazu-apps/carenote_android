package com.carenote.app.data.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SecureFileDeleterTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `delete existing file returns true and removes file`() {
        val file = tempFolder.newFile("test.csv")
        file.writeText("sensitive data")
        assertTrue(SecureFileDeleter.delete(file))
        assertFalse(file.exists())
    }

    @Test
    fun `delete non-existing file returns true`() {
        val file = File(tempFolder.root, "nonexistent.csv")
        assertTrue(SecureFileDeleter.delete(file))
    }

    @Test
    fun `delete empty file returns true`() {
        val file = tempFolder.newFile("empty.csv")
        assertTrue(SecureFileDeleter.delete(file))
        assertFalse(file.exists())
    }

    @Test
    fun `delete removes file content from disk`() {
        val file = tempFolder.newFile("pii.csv")
        file.writeText("Name,Email\nJohn,john@example.com")
        assertTrue(SecureFileDeleter.delete(file))
        assertFalse(file.exists())
    }

    @Test
    fun `deleteDirectory removes all files`() {
        val dir = tempFolder.newFolder("exports")
        File(dir, "a.csv").apply { writeText("d1") }
        File(dir, "b.pdf").apply { writeText("d2") }
        assertTrue(SecureFileDeleter.deleteDirectory(dir))
        assertFalse(dir.exists())
    }

    @Test
    fun `deleteDirectory handles nested directories`() {
        val dir = tempFolder.newFolder("exports")
        val sub = File(dir, "sub").apply { mkdirs() }
        File(sub, "nested.csv").apply { writeText("data") }
        assertTrue(SecureFileDeleter.deleteDirectory(dir))
        assertFalse(dir.exists())
    }

    @Test
    fun `deleteDirectory on non-existing returns true`() {
        assertTrue(SecureFileDeleter.deleteDirectory(File(tempFolder.root, "nope")))
    }

    @Test
    fun `deleteDirectory on file delegates to delete`() {
        val file = tempFolder.newFile("notdir.csv")
        file.writeText("data")
        assertTrue(SecureFileDeleter.deleteDirectory(file))
        assertFalse(file.exists())
    }
}
