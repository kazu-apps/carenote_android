package com.carenote.app.data.repository

import androidx.core.os.bundleOf
import com.carenote.app.domain.repository.AnalyticsRepository
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsRepositoryImpl @Inject constructor(
    private val analytics: dagger.Lazy<FirebaseAnalytics>
) : AnalyticsRepository {

    override fun logScreenView(screenName: String, screenClass: String) {
        val bundle = bundleOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName,
            FirebaseAnalytics.Param.SCREEN_CLASS to screenClass
        )
        analytics.get().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        Timber.d("Analytics screen_view: $screenName")
    }

    override fun logEvent(eventName: String, params: Map<String, String>) {
        val bundle = if (params.isNotEmpty()) {
            bundleOf(*params.map { (k, v) -> k to v }.toTypedArray())
        } else {
            null
        }
        analytics.get().logEvent(eventName, bundle)
        Timber.d("Analytics event: $eventName")
    }
}
