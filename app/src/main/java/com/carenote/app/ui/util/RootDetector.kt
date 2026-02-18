package com.carenote.app.ui.util

import android.os.Build
import java.io.File

interface RootDetectionChecker {
    fun isDeviceRooted(): Boolean
}

class RootDetector : RootDetectionChecker {

    override fun isDeviceRooted(): Boolean =
        checkBuildTags() || checkSuBinaryPaths() || checkSystemProperties()

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

    private fun checkSystemProperties(): Boolean =
        try {
            val clazz = Class.forName("android.os.SystemProperties")
            val get = clazz.getMethod("get", String::class.java)
            val debuggable = get.invoke(null, "ro.debuggable") as? String
            debuggable == "1"
        } catch (_: Exception) {
            false
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
