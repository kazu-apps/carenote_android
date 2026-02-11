package com.carenote.app.ui.util

import android.content.Context
import android.content.res.AssetManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException

class AssetReaderTest {

    private val context: Context = mockk()
    private val assetManager: AssetManager = mockk()

    @Test
    fun `readAssetText returns file content`() {
        val expectedContent = "利用規約の内容です。"
        val inputStream = ByteArrayInputStream(expectedContent.toByteArray())

        every { context.assets } returns assetManager
        every { assetManager.open("terms.html") } returns inputStream

        val result = readAssetText(context, "terms.html")

        assertEquals(expectedContent, result)
    }

    @Test(expected = IOException::class)
    fun `readAssetText throws IOException when file not found`() {
        every { context.assets } returns assetManager
        every { assetManager.open("nonexistent.html") } throws IOException("File not found")

        readAssetText(context, "nonexistent.html")
    }
}
