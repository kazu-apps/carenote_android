package com.carenote.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.carenote.app.domain.util.Clock
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCountDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_FILE_NAME, Context.MODE_PRIVATE
    )

    fun getTaskReminderCountToday(clock: Clock): Int {
        val today = clock.today().toString()
        val savedDate = prefs.getString(KEY_TASK_REMINDER_DATE, null)
        if (savedDate != today) {
            prefs.edit()
                .putInt(KEY_TASK_REMINDER_COUNT, 0)
                .putString(KEY_TASK_REMINDER_DATE, today)
                .apply()
            return 0
        }
        return prefs.getInt(KEY_TASK_REMINDER_COUNT, 0)
    }

    fun incrementTaskReminderCount(clock: Clock) {
        val today = clock.today().toString()
        val savedDate = prefs.getString(KEY_TASK_REMINDER_DATE, null)
        val currentCount = if (savedDate != today) 0 else prefs.getInt(KEY_TASK_REMINDER_COUNT, 0)
        prefs.edit()
            .putInt(KEY_TASK_REMINDER_COUNT, currentCount + 1)
            .putString(KEY_TASK_REMINDER_DATE, today)
            .apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "carenote_notification_count"
        private const val KEY_TASK_REMINDER_COUNT = "task_reminder_count"
        private const val KEY_TASK_REMINDER_DATE = "task_reminder_count_date"
    }
}
