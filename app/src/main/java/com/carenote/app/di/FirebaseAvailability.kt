package com.carenote.app.di

import com.google.firebase.FirebaseApp
import timber.log.Timber

data class FirebaseAvailability(val isAvailable: Boolean) {
    companion object {
        fun check(): FirebaseAvailability = try {
            FirebaseApp.getInstance()
            Timber.d("Firebase is available")
            FirebaseAvailability(true)
        } catch (_: Exception) {
            Timber.w("Firebase is not available: google-services.json may be missing")
            FirebaseAvailability(false)
        }
    }
}
