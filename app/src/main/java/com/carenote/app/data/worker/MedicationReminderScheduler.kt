package com.carenote.app.data.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.MedicationTiming
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服薬リマインダーのスケジューラー
 *
 * WorkManager を使用して指定時刻に服薬リマインダー通知をスケジュールする。
 *
 * ## 使用例
 * ```
 * // リマインダーをスケジュール
 * scheduler.scheduleReminder(
 *     medicationId = 1,
 *     medicationName = "ロキソニン",
 *     timing = MedicationTiming.MORNING,
 *     time = LocalTime.of(8, 0)
 * )
 *
 * // リマインダーをキャンセル
 * scheduler.cancelReminders(medicationId = 1)
 * ```
 */
@Singleton
class MedicationReminderScheduler @Inject constructor(
    private val workManager: WorkManager
) : MedicationReminderSchedulerInterface {

    override fun scheduleReminder(
        medicationId: Long,
        medicationName: String,
        timing: MedicationTiming,
        time: LocalTime
    ) {
        val delay = calculateDelay(time)
        if (delay < 0) {
            Timber.d("Reminder time has passed for today: id=$medicationId, timing=$timing")
            return
        }

        val inputData = workDataOf(
            MedicationReminderWorker.KEY_MEDICATION_ID to medicationId,
            MedicationReminderWorker.KEY_MEDICATION_NAME to medicationName,
            MedicationReminderWorker.KEY_TIMING to timing.name,
            MedicationReminderWorker.KEY_FOLLOW_UP_ATTEMPT to 0
        )

        val workRequest = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(AppConfig.Notification.REMINDER_WORK_TAG)
            .addTag(createMedicationTag(medicationId))
            .build()

        val uniqueWorkName = createUniqueWorkName(medicationId, timing)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Timber.d(
            "Scheduled reminder: id=$medicationId, timing=$timing, time=$time, delay=${delay}ms"
        )
    }

    override fun scheduleAllReminders(
        medicationId: Long,
        medicationName: String,
        times: Map<MedicationTiming, LocalTime>
    ) {
        times.forEach { (timing, time) ->
            scheduleReminder(medicationId, medicationName, timing, time)
        }
    }

    override fun cancelReminders(medicationId: Long) {
        workManager.cancelAllWorkByTag(createMedicationTag(medicationId))
        Timber.d("Cancelled reminders for medication: $medicationId")
    }

    override fun cancelAllReminders() {
        workManager.cancelAllWorkByTag(AppConfig.Notification.REMINDER_WORK_TAG)
        Timber.d("Cancelled all medication reminders")
    }

    override fun scheduleFollowUp(
        medicationId: Long,
        medicationName: String,
        timing: MedicationTiming?,
        attemptNumber: Int
    ) {
        if (attemptNumber >= AppConfig.Notification.FOLLOW_UP_MAX_ATTEMPTS) {
            Timber.d("Max follow-up attempts reached: id=$medicationId, timing=$timing")
            return
        }

        val inputData = workDataOf(
            MedicationReminderWorker.KEY_MEDICATION_ID to medicationId,
            MedicationReminderWorker.KEY_MEDICATION_NAME to medicationName,
            MedicationReminderWorker.KEY_TIMING to timing?.name,
            MedicationReminderWorker.KEY_FOLLOW_UP_ATTEMPT to attemptNumber
        )

        val workRequest = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
            .setInitialDelay(AppConfig.Notification.FOLLOW_UP_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag(AppConfig.Notification.FOLLOW_UP_WORK_TAG)
            .addTag(createMedicationTag(medicationId))
            .addTag(createFollowUpTag(medicationId, timing))
            .build()

        val uniqueWorkName = createFollowUpWorkName(medicationId, timing)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Timber.d(
            "Scheduled follow-up: id=$medicationId, timing=$timing, attempt=$attemptNumber"
        )
    }

    override fun cancelFollowUp(medicationId: Long, timing: MedicationTiming?) {
        val tag = createFollowUpTag(medicationId, timing)
        workManager.cancelAllWorkByTag(tag)
        Timber.d("Cancelled follow-up: id=$medicationId, timing=$timing")
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

    private fun createMedicationTag(medicationId: Long): String {
        return "medication_$medicationId"
    }

    private fun createUniqueWorkName(medicationId: Long, timing: MedicationTiming): String {
        return "reminder_${medicationId}_${timing.name}"
    }

    private fun createFollowUpTag(medicationId: Long, timing: MedicationTiming?): String {
        return "follow_up_${medicationId}_${timing?.name ?: "ALL"}"
    }

    private fun createFollowUpWorkName(medicationId: Long, timing: MedicationTiming?): String {
        return "follow_up_${medicationId}_${timing?.name ?: "ALL"}"
    }
}
