package com.carenote.app.ui.util

import android.util.Log
import org.junit.Test

class CrashlyticsTreeTest {

    private val tree = CrashlyticsTree()

    @Test
    fun `DEBUG priority is silently ignored`() {
        // Should not throw even without Firebase initialized
        tree.log(Log.DEBUG, "TestTag", "debug message", null)
    }

    @Test
    fun `VERBOSE priority is silently ignored`() {
        tree.log(Log.VERBOSE, "TestTag", "verbose message", null)
    }

    @Test
    fun `WARN priority does not crash when Firebase not initialized`() {
        // FirebaseCrashlytics.getInstance() will throw in test,
        // but CrashlyticsTree catches the exception gracefully
        tree.log(Log.WARN, "TestTag", "warning message", null)
    }

    @Test
    fun `ERROR priority does not crash when Firebase not initialized`() {
        tree.log(Log.ERROR, "TestTag", "error message", null)
    }

    @Test
    fun `ASSERT priority does not crash when Firebase not initialized`() {
        tree.log(Log.ASSERT, "TestTag", "assert message", null)
    }

    @Test
    fun `exception is handled gracefully when Firebase not initialized`() {
        tree.log(Log.ERROR, "TestTag", "error with exception", RuntimeException("test"))
    }

    @Test
    fun `null tag is handled gracefully`() {
        tree.log(Log.WARN, null as String?, "message with null tag", null)
    }
}
