package com.carenote.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carenote.app.domain.model.UserSettings
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
 * 2. ユーザー設定で通知が有効か確認
 * 3. おやすみ時間中でないか確認
 * 4. 通知を表示
 *
 * ## InputData
 * - KEY_MEDICATION_ID: Long - 薬の ID
 * - KEY_MEDICATION_NAME: String - 薬の名前
 */
@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val medicationId = inputData.getLong(KEY_MEDICATION_ID, INVALID_MEDICATION_ID)
        val medicationName = inputData.getString(KEY_MEDICATION_NAME) ?: ""

        Timber.d("MedicationReminderWorker started: id=$medicationId, name=$medicationName")

        if (medicationId == INVALID_MEDICATION_ID) {
            Timber.w("MedicationReminderWorker: Invalid medication ID")
            return Result.failure()
        }

        if (medicationName.isBlank()) {
            Timber.w("MedicationReminderWorker: Empty medication name")
            return Result.failure()
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
        return Result.success()
    }

    /**
     * 現在がおやすみ時間内かどうかを判定
     *
     * @param settings ユーザー設定
     * @return おやすみ時間内の場合は true
     */
    private fun isQuietHours(settings: UserSettings): Boolean {
        val currentHour = LocalTime.now().hour
        val start = settings.quietHoursStart
        val end = settings.quietHoursEnd

        return if (start < end) {
            // 例: 10:00 〜 18:00
            currentHour in start until end
        } else {
            // 例: 22:00 〜 7:00（日をまたぐケース）
            currentHour >= start || currentHour < end
        }
    }

    companion object {
        /** 薬の ID を渡す InputData キー */
        const val KEY_MEDICATION_ID = "medication_id"

        /** 薬の名前を渡す InputData キー */
        const val KEY_MEDICATION_NAME = "medication_name"

        /** 無効な薬 ID */
        private const val INVALID_MEDICATION_ID = -1L
    }
}
