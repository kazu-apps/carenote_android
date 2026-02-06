package com.carenote.app.ui.util

import android.content.Context
import androidx.biometric.BiometricManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BiometricHelperTest {

    private lateinit var helper: BiometricHelper
    private val context = mockk<Context>(relaxed = true)
    private val biometricManager = mockk<BiometricManager>()

    private val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    @Before
    fun setUp() {
        helper = BiometricHelper()
        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(context) } returns biometricManager
    }

    @After
    fun tearDown() {
        unmockkStatic(BiometricManager::class)
    }

    @Test
    fun `canAuthenticate returns true when BIOMETRIC_SUCCESS`() {
        every { biometricManager.canAuthenticate(authenticators) } returns
            BiometricManager.BIOMETRIC_SUCCESS

        assertTrue(helper.canAuthenticate(context))
    }

    @Test
    fun `canAuthenticate returns false when NO_HARDWARE`() {
        every { biometricManager.canAuthenticate(authenticators) } returns
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE

        assertFalse(helper.canAuthenticate(context))
    }

    @Test
    fun `canAuthenticate returns false when NONE_ENROLLED`() {
        every { biometricManager.canAuthenticate(authenticators) } returns
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED

        assertFalse(helper.canAuthenticate(context))
    }

    @Test
    fun `canAuthenticate returns false when HW_UNAVAILABLE`() {
        every { biometricManager.canAuthenticate(authenticators) } returns
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE

        assertFalse(helper.canAuthenticate(context))
    }
}
