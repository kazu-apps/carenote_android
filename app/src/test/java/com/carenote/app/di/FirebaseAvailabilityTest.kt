package com.carenote.app.di

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAvailabilityTest {

    @Test
    fun `check returns unavailable when FirebaseApp is not initialized`() {
        val availability = FirebaseAvailability.check()
        assertFalse(availability.isAvailable)
    }

    @Test
    fun `data class equality works`() {
        val available = FirebaseAvailability(true)
        val unavailable = FirebaseAvailability(false)
        assertTrue(available.isAvailable)
        assertFalse(unavailable.isAvailable)
    }
}
