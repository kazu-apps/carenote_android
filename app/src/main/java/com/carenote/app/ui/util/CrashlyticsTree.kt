package com.carenote.app.ui.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Timber Tree for release builds that sends logs to Firebase Crashlytics.
 *
 * - Ignores DEBUG and VERBOSE logs
 * - Sends WARN, ERROR, and ASSERT logs to Crashlytics
 * - Records exceptions for crash reporting
 *
 * Note: PII should never be logged (enforced by L-2 security review).
 */
class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Ignore DEBUG and VERBOSE logs in release builds
        if (priority < Log.WARN) return

        try {
            val crashlytics = FirebaseCrashlytics.getInstance()

            // Log message to Crashlytics (appears in crash reports as breadcrumbs)
            crashlytics.log("${tag ?: "CareNote"}: $message")

            // Record exception if present
            if (t != null) {
                crashlytics.recordException(t)
            }
        } catch (_: Exception) {
            // Crashlytics not initialized (e.g., google-services.json missing)
            // FirebaseApp.getInstance() may throw RuntimeException, not just IllegalStateException
            // Silently ignore - this is expected in development environments
        }
    }
}
