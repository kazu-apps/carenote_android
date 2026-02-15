package com.carenote.app.di

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import timber.log.Timber

data class BillingAvailability(val isAvailable: Boolean) {
    companion object {
        fun check(context: Context): BillingAvailability = try {
            val result = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context)
            val available = result == ConnectionResult.SUCCESS
            if (available) {
                Timber.d("Google Play Billing is available")
            } else {
                Timber.w("Google Play Services not available (code: $result)")
            }
            BillingAvailability(available)
        } catch (_: Exception) {
            Timber.w("Google Play Services check failed")
            BillingAvailability(false)
        }
    }
}
