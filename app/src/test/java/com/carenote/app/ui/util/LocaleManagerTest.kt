package com.carenote.app.ui.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.carenote.app.domain.model.AppLanguage
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class LocaleManagerTest {

    @Before
    fun setUp() {
        mockkStatic(AppCompatDelegate::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(AppCompatDelegate::class)
    }

    @Test
    fun `applyLanguage JAPANESE sets locale to ja`() {
        every { AppCompatDelegate.getApplicationLocales() } returns LocaleListCompat.getEmptyLocaleList()
        every { AppCompatDelegate.setApplicationLocales(any()) } returns Unit

        LocaleManager.applyLanguage(AppLanguage.JAPANESE)

        verify {
            AppCompatDelegate.setApplicationLocales(
                match { it.toLanguageTags() == "ja" }
            )
        }
    }

    @Test
    fun `applyLanguage SYSTEM sets empty locale list`() {
        every { AppCompatDelegate.getApplicationLocales() } returns LocaleListCompat.forLanguageTags("ja")
        every { AppCompatDelegate.setApplicationLocales(any()) } returns Unit

        LocaleManager.applyLanguage(AppLanguage.SYSTEM)

        verify {
            AppCompatDelegate.setApplicationLocales(
                match { it.isEmpty }
            )
        }
    }

    @Test
    fun `applyLanguage skips when locale already set`() {
        val jaLocale = LocaleListCompat.forLanguageTags("ja")
        every { AppCompatDelegate.getApplicationLocales() } returns jaLocale

        LocaleManager.applyLanguage(AppLanguage.JAPANESE)

        verify(exactly = 0) { AppCompatDelegate.setApplicationLocales(any()) }
    }
}
