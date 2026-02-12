package com.carenote.app.fakes

import com.carenote.app.domain.repository.AnalyticsRepository

class FakeAnalyticsRepository : AnalyticsRepository {
    val loggedScreens = mutableListOf<String>()
    val loggedEvents = mutableListOf<Pair<String, Map<String, String>>>()

    override fun logScreenView(screenName: String, screenClass: String) {
        loggedScreens.add(screenName)
    }

    override fun logEvent(eventName: String, params: Map<String, String>) {
        loggedEvents.add(eventName to params)
    }

    fun clear() {
        loggedScreens.clear()
        loggedEvents.clear()
    }
}
