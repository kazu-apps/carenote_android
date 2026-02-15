package com.carenote.app.domain.repository

interface PremiumFeatureGuard {
    suspend fun canSendTaskReminder(): Boolean
    suspend fun recordTaskReminderSent()
    fun getTaskReminderCountToday(): Int
    fun getTaskReminderDailyLimit(): Int
}
