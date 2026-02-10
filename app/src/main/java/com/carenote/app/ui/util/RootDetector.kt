package com.carenote.app.ui.util

import android.os.Build
import java.io.File

interface RootDetectionChecker {
    fun isDeviceRooted(): Boolean
}

class RootDetector : RootDetectionChecker {

    override fun isDeviceRooted(): Boolean =
        checkBuildTags() || checkSuBinaryPaths()

    private fun checkBuildTags(): Boolean =
        Build.TAGS?.contains("test-keys") == true

    private fun checkSuBinaryPaths(): Boolean =
        SU_PATHS.any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }

    companion object {
        private val SU_PATHS = listOf(
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/system/su",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk"
        )
    }
}
