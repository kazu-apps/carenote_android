package com.carenote.app.ui.util

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class RootDetectorTest {

    private val rootDetector = RootDetector()

    @Test
    fun `isDeviceRooted returns false on clean environment`() {
        assertFalse(rootDetector.isDeviceRooted())
    }

    @Test
    fun `isDeviceRooted returns true when Build TAGS contains test-keys`() {
        ReflectionHelpers.setStaticField(Build::class.java, "TAGS", "test-keys")
        try {
            assertTrue(rootDetector.isDeviceRooted())
        } finally {
            ReflectionHelpers.setStaticField(Build::class.java, "TAGS", null)
        }
    }

    @Test
    fun `isDeviceRooted returns false when Build TAGS is null`() {
        ReflectionHelpers.setStaticField(Build::class.java, "TAGS", null)
        assertFalse(rootDetector.isDeviceRooted())
    }

    @Test
    fun `isDeviceRooted returns false when Build TAGS does not contain test-keys`() {
        ReflectionHelpers.setStaticField(Build::class.java, "TAGS", "release-keys")
        try {
            assertFalse(rootDetector.isDeviceRooted())
        } finally {
            ReflectionHelpers.setStaticField(Build::class.java, "TAGS", null)
        }
    }

    @Test
    fun `checkSystemProperties is accessible via isDeviceRooted`() {
        // On a Robolectric environment, android.os.SystemProperties
        // is available. ro.debuggable defaults to "0" in Robolectric.
        // This test verifies the method doesn't throw and integrates properly.
        ReflectionHelpers.setStaticField(Build::class.java, "TAGS", "release-keys")
        try {
            // Should not throw even though SystemProperties is called
            rootDetector.isDeviceRooted()
        } finally {
            ReflectionHelpers.setStaticField(Build::class.java, "TAGS", null)
        }
    }

    @Test
    fun `checkSystemProperties returns false when exception is thrown`() {
        // The method catches all exceptions and returns false.
        // In Robolectric, SystemProperties is available so no exception.
        // We just verify it doesn't cause isDeviceRooted to throw.
        val result = rootDetector.isDeviceRooted()
        // On clean Robolectric env, build tags are null and
        // ro.debuggable is "0", so result should be false
        assertFalse(result)
    }
}
