package com.carenote.app.config

/**
 * 通知関連の設定値
 */
object NotificationConfigs {

    /**
     * 通知関連の設定値
     */
    object Notification {
        /** おやすみ時間デフォルト開始 */
        const val DEFAULT_QUIET_HOURS_START = 22

        /** おやすみ時間デフォルト終了 */
        const val DEFAULT_QUIET_HOURS_END = 7

        /** 通知チャンネル ID: 服薬リマインダー */
        const val CHANNEL_ID_MEDICATION_REMINDER = "medication_reminder"

        /** 通知チャンネル ID: 同期ステータス */
        const val CHANNEL_ID_SYNC_STATUS = "sync_status"

        /** 通知チャンネル ID: 一般通知 */
        const val CHANNEL_ID_GENERAL = "general"

        /** 通知 ID ベース値: 服薬リマインダー */
        const val NOTIFICATION_ID_MEDICATION_BASE = 1000

        /** 通知 ID: 同期ステータス */
        const val NOTIFICATION_ID_SYNC = 2000

        /** リマインダーワーカーのタグ */
        const val REMINDER_WORK_TAG = "medication_reminder_work"

        /** フォローアップリマインダーの間隔（分） */
        const val FOLLOW_UP_INTERVAL_MINUTES = 30L

        /** フォローアップリマインダーの最大回数 */
        const val FOLLOW_UP_MAX_ATTEMPTS = 3

        /** フォローアップワーカーのタグ */
        const val FOLLOW_UP_WORK_TAG = "medication_follow_up_work"

        /** 通知チャンネル ID: タスクリマインダー */
        const val CHANNEL_ID_TASK_REMINDER = "task_reminder"

        /** 通知 ID ベース値: タスクリマインダー */
        const val NOTIFICATION_ID_TASK_BASE = 3000

        /** タスクリマインダーワーカーのタグ */
        const val TASK_REMINDER_WORK_TAG = "task_reminder_work"

        /** タスクフォローアップワーカーのタグ */
        const val TASK_FOLLOW_UP_WORK_TAG = "task_follow_up_work"

        /** 通知チャンネル ID: カレンダーイベントリマインダー */
        const val CHANNEL_ID_CALENDAR_REMINDER = "calendar_reminder"

        /** 通知 ID ベース値: カレンダーイベントリマインダー */
        const val NOTIFICATION_ID_CALENDAR_BASE = 4000

        /** カレンダーイベントリマインダーワーカーのタグ */
        const val CALENDAR_REMINDER_WORK_TAG = "calendar_reminder_work"

        /** カレンダーイベントフォローアップワーカーのタグ */
        const val CALENDAR_FOLLOW_UP_WORK_TAG = "calendar_follow_up_work"

        /** Deep Link URI スキーム */
        const val DEEP_LINK_SCHEME = "carenote"

        /** 無料ユーザーのタスクリマインダー1日あたりの上限回数 */
        const val TASK_REMINDER_FREE_DAILY_LIMIT = 3
    }
}
