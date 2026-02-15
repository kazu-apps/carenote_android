package com.carenote.app.fakes

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.PremiumFeatureGuard

class FakePremiumFeatureGuard : PremiumFeatureGuard {
    var canSend = true
    var recordedCount = 0
    var todayCount = 0

    override suspend fun canSendTaskReminder(): Boolean = canSend

    override suspend fun recordTaskReminderSent() {
        recordedCount++
        todayCount++
    }

    override fun getTaskReminderCountToday(): Int = todayCount

    override fun getTaskReminderDailyLimit(): Int =
        AppConfig.Notification.TASK_REMINDER_FREE_DAILY_LIMIT

    fun clear() {
        canSend = true
        recordedCount = 0
        todayCount = 0
    }
}
