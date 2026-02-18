package com.carenote.app.data.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.CalendarEventReminderSchedulerInterface
import com.carenote.app.domain.util.Clock
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * カレンダーイベントリマインダーのスケジューラー
 *
 * WorkManager を使用して指定時刻にカレンダーイベントリマインダー通知をスケジュールする。
 *
 * ## 使用例
 * ```
 * // リマインダーをスケジュール
 * scheduler.scheduleReminder(
 *     eventId = 1,
 *     eventTitle = "通院",
 *     time = LocalTime.of(9, 0)
 * )
 *
 * // リマインダーをキャンセル
 * scheduler.cancelReminder(eventId = 1)
 * ```
 */
@Singleton
class CalendarEventReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val clock: Clock
) : CalendarEventReminderSchedulerInterface {

    override fun scheduleReminder(
        eventId: Long,
        eventTitle: String,
        time: LocalTime
    ) {
        val delay = calculateDelay(time)
        if (delay <= 0) {
            Timber.d("Calendar event reminder time has passed for today: id=$eventId")
            return
        }

        val inputData = workDataOf(
            CalendarEventReminderWorker.KEY_EVENT_ID to eventId,
            CalendarEventReminderWorker.KEY_EVENT_TITLE to eventTitle,
            CalendarEventReminderWorker.KEY_FOLLOW_UP_ATTEMPT to 0
        )

        val workRequest = OneTimeWorkRequestBuilder<CalendarEventReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(AppConfig.Notification.CALENDAR_REMINDER_WORK_TAG)
            .addTag(createEventTag(eventId))
            .build()

        val uniqueWorkName = createUniqueWorkName(eventId)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Timber.d("Scheduled calendar event reminder: id=$eventId, time=$time, delay=${delay}ms")
    }

    override fun cancelReminder(eventId: Long) {
        workManager.cancelAllWorkByTag(createEventTag(eventId))
        Timber.d("Cancelled reminder for calendar event: $eventId")
    }

    override fun cancelAllReminders() {
        workManager.cancelAllWorkByTag(AppConfig.Notification.CALENDAR_REMINDER_WORK_TAG)
        Timber.d("Cancelled all calendar event reminders")
    }

    override fun scheduleFollowUp(
        eventId: Long,
        eventTitle: String,
        attemptNumber: Int
    ) {
        if (attemptNumber >= AppConfig.Notification.FOLLOW_UP_MAX_ATTEMPTS) {
            Timber.d("Max follow-up attempts reached: eventId=$eventId")
            return
        }

        val inputData = workDataOf(
            CalendarEventReminderWorker.KEY_EVENT_ID to eventId,
            CalendarEventReminderWorker.KEY_EVENT_TITLE to eventTitle,
            CalendarEventReminderWorker.KEY_FOLLOW_UP_ATTEMPT to attemptNumber
        )

        val workRequest = OneTimeWorkRequestBuilder<CalendarEventReminderWorker>()
            .setInitialDelay(AppConfig.Notification.FOLLOW_UP_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag(AppConfig.Notification.CALENDAR_FOLLOW_UP_WORK_TAG)
            .addTag(createEventTag(eventId))
            .addTag(createFollowUpTag(eventId))
            .build()

        val uniqueWorkName = createFollowUpWorkName(eventId)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Timber.d("Scheduled calendar event follow-up: id=$eventId, attempt=$attemptNumber")
    }

    override fun cancelFollowUp(eventId: Long) {
        val tag = createFollowUpTag(eventId)
        workManager.cancelAllWorkByTag(tag)
        Timber.d("Cancelled calendar event follow-up: id=$eventId")
    }

    internal fun calculateDelay(time: LocalTime): Long {
        val now = clock.now()
        val today = LocalDateTime.of(now.toLocalDate(), time)
        val target = if (today.isAfter(now)) today else today.plusDays(1)
        return Duration.between(now, target).toMillis()
    }

    private fun createEventTag(eventId: Long): String {
        return "calendar_reminder_$eventId"
    }

    private fun createUniqueWorkName(eventId: Long): String {
        return "calendar_reminder_$eventId"
    }

    private fun createFollowUpTag(eventId: Long): String {
        return "calendar_follow_up_$eventId"
    }

    private fun createFollowUpWorkName(eventId: Long): String {
        return "calendar_follow_up_$eventId"
    }
}
