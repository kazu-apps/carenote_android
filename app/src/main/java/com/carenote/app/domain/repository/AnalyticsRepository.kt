package com.carenote.app.domain.repository

interface AnalyticsRepository {
    fun logScreenView(screenName: String, screenClass: String = "")
    fun logEvent(eventName: String, params: Map<String, String> = emptyMap())
}
