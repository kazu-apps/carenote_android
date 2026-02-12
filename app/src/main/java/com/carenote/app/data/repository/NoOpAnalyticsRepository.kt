package com.carenote.app.data.repository

import com.carenote.app.domain.repository.AnalyticsRepository
import timber.log.Timber

class NoOpAnalyticsRepository : AnalyticsRepository {

    override fun logScreenView(screenName: String, screenClass: String) {
        Timber.d("NoOp Analytics screen_view: $screenName")
    }

    override fun logEvent(eventName: String, params: Map<String, String>) {
        Timber.d("NoOp Analytics event: $eventName")
    }
}
