package com.carenote.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationReminderSchedulerInterface
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.ui.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalTime

/**
 * 服薬リマインダー通知ワーカー
 *
 * WorkManager を使用して指定された時刻に服薬リマインダー通知を発行する。
 *
 * ## 動作フロー
 * 1. inputData から薬情報を取得
 * 2. 服薬済みチェック（TAKEN ログがあればスキップ）
 * 3. ユーザー設定で通知が有効か確認
 * 4. おやすみ時間中でないか確認
 * 5. 通知を表示
 * 6. フォローアップリマインダーをスケジュール
 *
 * ## InputData
 * - KEY_MEDICATION_ID: Long - 薬の ID
 * - KEY_MEDICATION_NAME: String - 薬の名前
 * - KEY_TIMING: String? - 服用タイミング名
 * - KEY_FOLLOW_UP_ATTEMPT: Int - フォローアップ試行回数
 */
@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val medicationLogRepository: MedicationLogRepository,
    private val reminderScheduler: MedicationReminderSchedulerInterface
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val medicationId = inputData.getLong(KEY_MEDICATION_ID, INVALID_MEDICATION_ID)
        val medicationName = inputData.getString(KEY_MEDICATION_NAME) ?: ""
        val timingName = inputData.getString(KEY_TIMING)
        val followUpAttempt = inputData.getInt(KEY_FOLLOW_UP_ATTEMPT, 0)

        val timing = timingName?.let {
            try {
                MedicationTiming.valueOf(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        Timber.d(
            "MedicationReminderWorker started: id=$medicationId, " +
                "timing=$timing, attempt=$followUpAttempt"
        )

        if (medicationId == INVALID_MEDICATION_ID) {
            Timber.w("MedicationReminderWorker: Invalid medication ID")
            return Result.failure()
        }

        if (medicationName.isBlank()) {
            Timber.w("MedicationReminderWorker: Empty medication name")
            return Result.failure()
        }

        // 服薬済みチェック
        val alreadyTaken = medicationLogRepository.hasLogForMedicationToday(
            medicationId,
            timing
        )
        if (alreadyTaken) {
            Timber.d("MedicationReminderWorker: Already taken, skipping notification")
            return Result.success()
        }

        // 設定で通知が有効か確認
        val settings = settingsRepository.getSettings().first()
        if (!settings.notificationsEnabled) {
            Timber.d("MedicationReminderWorker: Notifications disabled in settings")
            return Result.success()
        }

        // おやすみ時間チェック
        if (isQuietHours(settings)) {
            Timber.d("MedicationReminderWorker: Quiet hours active, skipping notification")
            return Result.success()
        }

        // 通知表示
        notificationHelper.showMedicationReminder(medicationId, medicationName)

        // フォローアップスケジュール
        reminderScheduler.scheduleFollowUp(
            medicationId = medicationId,
            medicationName = medicationName,
            timing = timing,
            attemptNumber = followUpAttempt + 1
        )

        return Result.success()
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
        const val KEY_MEDICATION_ID = "medication_id"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_TIMING = "medication_timing"
        const val KEY_FOLLOW_UP_ATTEMPT = "follow_up_attempt"
        private const val INVALID_MEDICATION_ID = -1L
    }
}
