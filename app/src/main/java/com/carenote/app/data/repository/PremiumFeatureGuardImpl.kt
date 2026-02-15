package com.carenote.app.data.repository

import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.NotificationCountDataSource
import com.carenote.app.domain.repository.BillingRepository
import com.carenote.app.domain.repository.PremiumFeatureGuard
import com.carenote.app.domain.util.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumFeatureGuardImpl @Inject constructor(
    private val billingRepository: BillingRepository,
    private val notificationCountDataSource: NotificationCountDataSource,
    private val clock: Clock
) : PremiumFeatureGuard {

    override suspend fun canSendTaskReminder(): Boolean {
        if (billingRepository.premiumStatus.value.isActive) return true
        val count = notificationCountDataSource.getTaskReminderCountToday(clock)
        return count < AppConfig.Notification.TASK_REMINDER_FREE_DAILY_LIMIT
    }

    override suspend fun recordTaskReminderSent() {
        notificationCountDataSource.incrementTaskReminderCount(clock)
    }

    override fun getTaskReminderCountToday(): Int {
        return notificationCountDataSource.getTaskReminderCountToday(clock)
    }

    override fun getTaskReminderDailyLimit(): Int {
        return AppConfig.Notification.TASK_REMINDER_FREE_DAILY_LIMIT
    }
}
