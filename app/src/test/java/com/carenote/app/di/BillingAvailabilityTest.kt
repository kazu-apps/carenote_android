package com.carenote.app.di

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BillingAvailabilityTest {

    private val context = mockk<Context>(relaxed = true)
    private val googleApiAvailability = mockk<GoogleApiAvailability>()

    @Before
    fun setup() {
        mockkStatic(GoogleApiAvailability::class)
        every { GoogleApiAvailability.getInstance() } returns googleApiAvailability
    }

    @After
    fun tearDown() {
        unmockkStatic(GoogleApiAvailability::class)
    }

    @Test
    fun `check returns available when Google Play Services is available`() {
        every {
            googleApiAvailability.isGooglePlayServicesAvailable(context)
        } returns ConnectionResult.SUCCESS

        val result = BillingAvailability.check(context)

        assertTrue(result.isAvailable)
    }

    @Test
    fun `check returns unavailable when Google Play Services is not available`() {
        every {
            googleApiAvailability.isGooglePlayServicesAvailable(context)
        } returns ConnectionResult.SERVICE_MISSING

        val result = BillingAvailability.check(context)

        assertFalse(result.isAvailable)
    }

    @Test
    fun `check returns unavailable when exception is thrown`() {
        every {
            googleApiAvailability.isGooglePlayServicesAvailable(context)
        } throws RuntimeException("Test")

        val result = BillingAvailability.check(context)

        assertFalse(result.isAvailable)
    }
}
