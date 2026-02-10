package com.carenote.app.fakes

import com.carenote.app.ui.util.RootDetectionChecker

class FakeRootDetector(var isRooted: Boolean = false) : RootDetectionChecker {
    override fun isDeviceRooted(): Boolean = isRooted
}
