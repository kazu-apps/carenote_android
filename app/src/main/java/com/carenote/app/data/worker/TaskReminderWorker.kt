package com.carenote.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.domain.repository.PremiumFeatureGuard
import com.carenote.app.domain.repository.TaskReminderSchedulerInterface
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.ui.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalTime

/**
 * タスクリマインダー通知ワーカー
 *
 * WorkManager を使用して指定された時刻にタスクリマインダー通知を発行する。
 *
 * ## 動作フロー
 * 1. inputData からタスク情報を取得
 * 2. タスク完了チェック（isCompleted なら skip）
 * 3. ユーザー設定で通知が有効か確認
 * 4. おやすみ時間中でないか確認
 * 5. 通知を表示
 * 6. フォローアップリマインダーをスケジュール
 *
 * ## InputData
 * - KEY_TASK_ID: Long - タスクの ID
 * - KEY_TASK_TITLE: String - タスクのタイトル
 * - KEY_FOLLOW_UP_ATTEMPT: Int - フォローアップ試行回数
 */
@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val calendarEventRepository: CalendarEventRepository,
    private val reminderScheduler: TaskReminderSchedulerInterface,
    private val premiumFeatureGuard: PremiumFeatureGuard
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, INVALID_TASK_ID)
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: ""
        val followUpAttempt = inputData.getInt(KEY_FOLLOW_UP_ATTEMPT, 0)

        Timber.d("TaskReminderWorker started: id=$taskId, attempt=$followUpAttempt")

        val skipReason = getSkipReason(taskId, taskTitle)
        if (skipReason != null) return skipReason

        // 通知表示
        notificationHelper.showTaskReminder(taskId, taskTitle)
        premiumFeatureGuard.recordTaskReminderSent()

        // フォローアップスケジュール
        reminderScheduler.scheduleFollowUp(
            taskId = taskId,
            taskTitle = taskTitle,
            attemptNumber = followUpAttempt + 1
        )

        return Result.success()
    }

    private suspend fun getSkipReason(taskId: Long, taskTitle: String): Result? {
        if (taskId == INVALID_TASK_ID || taskTitle.isBlank()) {
            Timber.w("TaskReminderWorker: Invalid input (id=$taskId)")
            return Result.failure()
        }

        val event = calendarEventRepository.getEventById(taskId).first()
        if (event == null || !event.isTask || event.completed) {
            Timber.d("TaskReminderWorker: Task not found or completed")
            return Result.success()
        }

        val settings = settingsRepository.getSettings().first()
        if (!settings.notificationsEnabled || isQuietHours(settings)) {
            Timber.d("TaskReminderWorker: Notifications disabled or quiet hours")
            return Result.success()
        }

        if (!premiumFeatureGuard.canSendTaskReminder()) {
            Timber.d("TaskReminderWorker: Daily limit reached")
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
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_FOLLOW_UP_ATTEMPT = "follow_up_attempt"
        private const val INVALID_TASK_ID = -1L
    }
}
