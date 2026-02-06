package com.carenote.app.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知関連のヘルパークラス
 *
 * Android 8+ (API 26+) 向けの NotificationChannel 作成と管理を行う。
 * アプリ起動時に [createNotificationChannels] を呼び出して通知チャンネルを初期化する。
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 全通知チャンネルを作成
     *
     * アプリ起動時に呼び出す。Android 8+ では通知チャンネルが必須。
     * 既存のチャンネルがある場合は上書きされない（ユーザー設定は保持される）。
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                createMedicationReminderChannel(),
                createTaskReminderChannel(),
                createSyncStatusChannel(),
                createGeneralChannel()
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(channels)
            Timber.d("Notification channels created: ${channels.size}")
        }
    }

    /**
     * 服薬リマインダー用チャンネル（高重要度）
     *
     * 服薬時間の通知に使用。ユーザーの健康に直接関わるため HIGH 重要度。
     * バイブレーション・LED 有効。
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMedicationReminderChannel(): NotificationChannel {
        return NotificationChannel(
            AppConfig.Notification.CHANNEL_ID_MEDICATION_REMINDER,
            context.getString(R.string.notification_channel_medication_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_medication_description)
            enableVibration(true)
            enableLights(true)
        }
    }

    /**
     * タスクリマインダー用チャンネル（高重要度）
     *
     * タスク期限の通知に使用。ユーザーのスケジュールに直接関わるため HIGH 重要度。
     * バイブレーション・LED 有効。
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTaskReminderChannel(): NotificationChannel {
        return NotificationChannel(
            AppConfig.Notification.CHANNEL_ID_TASK_REMINDER,
            context.getString(R.string.notification_channel_task_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_task_description)
            enableVibration(true)
            enableLights(true)
        }
    }

    /**
     * 同期ステータス用チャンネル（低重要度）
     *
     * バックグラウンド同期完了通知に使用。
     * ユーザーの邪魔にならないよう LOW 重要度。バイブレーションなし、バッジなし。
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSyncStatusChannel(): NotificationChannel {
        return NotificationChannel(
            AppConfig.Notification.CHANNEL_ID_SYNC_STATUS,
            context.getString(R.string.notification_channel_sync_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_sync_description)
            enableVibration(false)
            setShowBadge(false)
        }
    }

    /**
     * 一般通知用チャンネル（デフォルト重要度）
     *
     * 分類されないその他の通知に使用。
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createGeneralChannel(): NotificationChannel {
        return NotificationChannel(
            AppConfig.Notification.CHANNEL_ID_GENERAL,
            context.getString(R.string.notification_channel_general_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_general_description)
        }
    }

    /**
     * 服薬リマインダー通知を表示
     *
     * @param medicationId 薬の ID（通知 ID の生成に使用）
     * @param medicationName 薬の名前（通知テキストに表示）
     */
    companion object {
        internal fun safeIntId(id: Long): Int = id.and(0x7FFFFFFF).toInt()
    }

    fun showMedicationReminder(
        medicationId: Long,
        medicationName: String
    ) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // PendingIntent: 通知タップでアプリを開く
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            safeIntId(medicationId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            AppConfig.Notification.CHANNEL_ID_MEDICATION_REMINDER
        )
            .setSmallIcon(R.drawable.ic_notification_medication)
            .setContentTitle(context.getString(R.string.notification_medication_reminder_title))
            .setContentText(
                context.getString(R.string.notification_medication_reminder_text, medicationName)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE + safeIntId(medicationId)
        notificationManager.notify(notificationId, notification)
        Timber.d("Medication reminder shown: id=$medicationId, name=$medicationName")
    }

    /**
     * タスクリマインダー通知を表示
     *
     * @param taskId タスクの ID（通知 ID の生成に使用）
     * @param taskTitle タスクのタイトル（通知テキストに表示）
     */
    fun showTaskReminder(
        taskId: Long,
        taskTitle: String
    ) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            safeIntId(taskId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            AppConfig.Notification.CHANNEL_ID_TASK_REMINDER
        )
            .setSmallIcon(R.drawable.ic_notification_task)
            .setContentTitle(context.getString(R.string.notification_task_reminder_title))
            .setContentText(
                context.getString(R.string.notification_task_reminder_text, taskTitle)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = AppConfig.Notification.NOTIFICATION_ID_TASK_BASE + safeIntId(taskId)
        notificationManager.notify(notificationId, notification)
        Timber.d("Task reminder shown: id=$taskId, title=$taskTitle")
    }
}
