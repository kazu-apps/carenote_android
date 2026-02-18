package com.carenote.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.CalendarEventReminderSchedulerInterface
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.ui.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalTime

/**
 * カレンダーイベントリマインダー通知ワーカー
 *
 * WorkManager を使用して指定された時刻にカレンダーイベントリマインダー通知を発行する。
 *
 * ## 動作フロー
 * 1. inputData からイベント情報を取得
 * 2. イベント存在チェック（見つからない or isTask なら skip）
 * 3. reminderEnabled チェック（無効なら skip）
 * 4. ユーザー設定で通知が有効か確認
 * 5. おやすみ時間中でないか確認
 * 6. 通知を表示
 * 7. フォローアップリマインダーをスケジュール
 *
 * ## InputData
 * - KEY_EVENT_ID: Long - イベントの ID
 * - KEY_EVENT_TITLE: String - イベントのタイトル
 * - KEY_FOLLOW_UP_ATTEMPT: Int - フォローアップ試行回数
 */
@HiltWorker
class CalendarEventReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val calendarEventRepository: CalendarEventRepository,
    private val reminderScheduler: CalendarEventReminderSchedulerInterface
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getLong(KEY_EVENT_ID, INVALID_EVENT_ID)
        val eventTitle = inputData.getString(KEY_EVENT_TITLE) ?: ""
        val followUpAttempt = inputData.getInt(KEY_FOLLOW_UP_ATTEMPT, 0)

        Timber.d("CalendarEventReminderWorker started: id=$eventId, attempt=$followUpAttempt")

        val skipReason = getSkipReason(eventId, eventTitle)
        if (skipReason != null) return skipReason

        // 通知表示
        notificationHelper.showCalendarEventReminder(eventId, eventTitle)

        // フォローアップスケジュール
        reminderScheduler.scheduleFollowUp(
            eventId = eventId,
            eventTitle = eventTitle,
            attemptNumber = followUpAttempt + 1
        )

        return Result.success()
    }

    private suspend fun getSkipReason(eventId: Long, eventTitle: String): Result? {
        if (eventId == INVALID_EVENT_ID || eventTitle.isBlank()) {
            Timber.w("CalendarEventReminderWorker: Invalid input (id=$eventId)")
            return Result.failure()
        }

        val event = calendarEventRepository.getEventById(eventId).first()
        if (event == null || event.isTask || !event.reminderEnabled) {
            Timber.d("CalendarEventReminderWorker: Event not found, is task, or reminder disabled")
            return Result.success()
        }

        val settings = settingsRepository.getSettings().first()
        if (!settings.notificationsEnabled || isQuietHours(settings)) {
            Timber.d("CalendarEventReminderWorker: Notifications disabled or quiet hours")
            return Result.success()
        }

        return null
    }

    private fun isQuietHours(settings: UserSettings): Boolean {
        val currentHour = LocalTime.now().hour
        val start = settings.quietHoursStart
        val end = settings.quietHoursEnd

        return if (start < end) {
            currentHour in start until end
        } else {
            currentHour >= start || currentHour < end
        }
    }

    companion object {
        const val KEY_EVENT_ID = "event_id"
        const val KEY_EVENT_TITLE = "event_title"
        const val KEY_FOLLOW_UP_ATTEMPT = "follow_up_attempt"
        private const val INVALID_EVENT_ID = -1L
    }
}
