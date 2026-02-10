package com.carenote.app.data.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.TaskReminderSchedulerInterface
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * タスクリマインダーのスケジューラー
 *
 * WorkManager を使用して指定時刻にタスクリマインダー通知をスケジュールする。
 *
 * ## 使用例
 * ```
 * // リマインダーをスケジュール
 * scheduler.scheduleReminder(
 *     taskId = 1,
 *     taskTitle = "買い物に行く",
 *     time = LocalTime.of(9, 0)
 * )
 *
 * // リマインダーをキャンセル
 * scheduler.cancelReminder(taskId = 1)
 * ```
 */
@Singleton
class TaskReminderScheduler @Inject constructor(
    private val workManager: WorkManager
) : TaskReminderSchedulerInterface {

    override fun scheduleReminder(
        taskId: Long,
        taskTitle: String,
        time: LocalTime
    ) {
        val delay = calculateDelay(time)
        if (delay < 0) {
            Timber.d("Task reminder time has passed for today: id=$taskId")
            return
        }

        val inputData = workDataOf(
            TaskReminderWorker.KEY_TASK_ID to taskId,
            TaskReminderWorker.KEY_TASK_TITLE to taskTitle,
            TaskReminderWorker.KEY_FOLLOW_UP_ATTEMPT to 0
        )

        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(AppConfig.Notification.TASK_REMINDER_WORK_TAG)
            .addTag(createTaskTag(taskId))
            .build()

        val uniqueWorkName = createUniqueWorkName(taskId)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Timber.d("Scheduled task reminder: id=$taskId, time=$time, delay=${delay}ms")
    }

    override fun cancelReminder(taskId: Long) {
        workManager.cancelAllWorkByTag(createTaskTag(taskId))
        Timber.d("Cancelled reminder for task: $taskId")
    }

    override fun cancelAllReminders() {
        workManager.cancelAllWorkByTag(AppConfig.Notification.TASK_REMINDER_WORK_TAG)
        Timber.d("Cancelled all task reminders")
    }

    override fun scheduleFollowUp(
        taskId: Long,
        taskTitle: String,
        attemptNumber: Int
    ) {
        if (attemptNumber >= AppConfig.Notification.FOLLOW_UP_MAX_ATTEMPTS) {
            Timber.d("Max follow-up attempts reached: taskId=$taskId")
            return
        }

        val inputData = workDataOf(
            TaskReminderWorker.KEY_TASK_ID to taskId,
            TaskReminderWorker.KEY_TASK_TITLE to taskTitle,
            TaskReminderWorker.KEY_FOLLOW_UP_ATTEMPT to attemptNumber
        )

        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(AppConfig.Notification.FOLLOW_UP_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag(AppConfig.Notification.TASK_FOLLOW_UP_WORK_TAG)
            .addTag(createTaskTag(taskId))
            .addTag(createFollowUpTag(taskId))
            .build()

        val uniqueWorkName = createFollowUpWorkName(taskId)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Timber.d("Scheduled task follow-up: id=$taskId, attempt=$attemptNumber")
    }

    override fun cancelFollowUp(taskId: Long) {
        val tag = createFollowUpTag(taskId)
        workManager.cancelAllWorkByTag(tag)
        Timber.d("Cancelled task follow-up: id=$taskId")
    }

    private fun calculateDelay(time: LocalTime): Long {
        val now = LocalDateTime.now()
        val target = LocalDateTime.of(now.toLocalDate(), time)

        return if (target.isAfter(now)) {
            Duration.between(now, target).toMillis()
        } else {
            -1
        }
    }

    private fun createTaskTag(taskId: Long): String {
        return "task_reminder_$taskId"
    }

    private fun createUniqueWorkName(taskId: Long): String {
        return "task_reminder_$taskId"
    }

    private fun createFollowUpTag(taskId: Long): String {
        return "task_follow_up_$taskId"
    }

    private fun createFollowUpWorkName(taskId: Long): String {
        return "task_follow_up_$taskId"
    }
}
