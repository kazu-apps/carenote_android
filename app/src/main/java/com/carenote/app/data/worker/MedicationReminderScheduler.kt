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
) {
    /**
     * 特定の薬のリマインダーをスケジュール
     *
     * 指定された時刻に通知が発行されるようワーカーをスケジュールする。
     * 同じ薬・タイミングの既存リマインダーは上書きされる。
     *
     * @param medicationId 薬の ID
     * @param medicationName 薬の名前
     * @param timing 服用タイミング（朝/昼/夕）
     * @param time 通知時刻
     */
    fun scheduleReminder(
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
            MedicationReminderWorker.KEY_MEDICATION_NAME to medicationName
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

    /**
     * 全タイミングのリマインダーをまとめてスケジュール
     *
     * @param medicationId 薬の ID
     * @param medicationName 薬の名前
     * @param times タイミングごとの通知時刻マップ
     */
    fun scheduleAllReminders(
        medicationId: Long,
        medicationName: String,
        times: Map<MedicationTiming, LocalTime>
    ) {
        times.forEach { (timing, time) ->
            scheduleReminder(medicationId, medicationName, timing, time)
        }
    }

    /**
     * 薬に関連する全リマインダーをキャンセル
     *
     * @param medicationId 薬の ID
     */
    fun cancelReminders(medicationId: Long) {
        workManager.cancelAllWorkByTag(createMedicationTag(medicationId))
        Timber.d("Cancelled reminders for medication: $medicationId")
    }

    /**
     * 全てのリマインダーをキャンセル
     */
    fun cancelAllReminders() {
        workManager.cancelAllWorkByTag(AppConfig.Notification.REMINDER_WORK_TAG)
        Timber.d("Cancelled all medication reminders")
    }

    /**
     * 指定時刻までの遅延時間（ミリ秒）を計算
     *
     * @param time 目標時刻
     * @return 遅延時間（ミリ秒）。時刻が過ぎている場合は -1
     */
    private fun calculateDelay(time: LocalTime): Long {
        val now = LocalDateTime.now()
        val target = LocalDateTime.of(now.toLocalDate(), time)

        return if (target.isAfter(now)) {
            Duration.between(now, target).toMillis()
        } else {
            -1
        }
    }

    /**
     * 薬 ID 用のタグを生成
     */
    private fun createMedicationTag(medicationId: Long): String {
        return "medication_$medicationId"
    }

    /**
     * 一意なワーク名を生成
     */
    private fun createUniqueWorkName(medicationId: Long, timing: MedicationTiming): String {
        return "reminder_${medicationId}_${timing.name}"
    }
}
