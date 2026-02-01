package com.carenote.app.config

/**
 * アプリケーション全体の設定値を集約
 * マジックナンバーを避け、一元管理することで保守性を向上
 */
object AppConfig {

    /**
     * 服薬関連の設定値
     */
    object Medication {
        /** デフォルトの朝食後時刻 */
        const val DEFAULT_MORNING_HOUR = 8
        const val DEFAULT_MORNING_MINUTE = 0

        /** デフォルトの昼食後時刻 */
        const val DEFAULT_NOON_HOUR = 12
        const val DEFAULT_NOON_MINUTE = 0

        /** デフォルトの夕食後時刻 */
        const val DEFAULT_EVENING_HOUR = 18
        const val DEFAULT_EVENING_MINUTE = 0
    }

    /**
     * 健康記録の異常値基準（設定で変更可能）
     */
    object HealthThresholds {
        /** 体温の異常値（℃以上） */
        const val TEMPERATURE_HIGH = 37.5

        /** 血圧上の異常値（mmHg以上） */
        const val BLOOD_PRESSURE_HIGH_UPPER = 140

        /** 血圧下の異常値（mmHg以上） */
        const val BLOOD_PRESSURE_HIGH_LOWER = 90

        /** 脈拍の異常値上限（回/分以上） */
        const val PULSE_HIGH = 100

        /** 脈拍の異常値下限（回/分以下） */
        const val PULSE_LOW = 50
    }

    /**
     * 通知関連の設定値
     */
    object Notification {
        /** 未記録リマインド間隔（分） - 無料版 */
        const val FREE_REMINDER_INTERVAL_MINUTES = 30

        /** 未記録リマインド回数 - 無料版 */
        const val FREE_REMINDER_COUNT = 1

        /** 未記録リマインド回数 - プレミアム版 */
        const val PREMIUM_REMINDER_COUNT = 3

        /** おやすみ時間デフォルト開始 */
        const val DEFAULT_QUIET_HOURS_START = 22

        /** おやすみ時間デフォルト終了 */
        const val DEFAULT_QUIET_HOURS_END = 7
    }

    /**
     * プレミアム関連の設定値
     */
    object Premium {
        /** 無料版の家族メンバー上限 */
        const val FREE_MEMBER_LIMIT = 2

        /** プレミアム版の家族メンバー上限 */
        const val PREMIUM_MEMBER_LIMIT = 5

        /** 無料版のデータ保存期間（日） */
        const val FREE_DATA_RETENTION_DAYS = 90

        /** 無料版のグラフ表示期間（日） */
        const val FREE_GRAPH_DAYS = 7
    }

    /**
     * メモ関連の設定値
     */
    object Note {
        /** メモ内容プレビューの最大行数 */
        const val CONTENT_PREVIEW_MAX_LINES = 2

        /** タイトルの最大行数 */
        const val TITLE_MAX_LINES = 1

        /** 内容入力の最小行数 */
        const val CONTENT_MIN_LINES = 5
    }

    /**
     * UI関連の設定値
     */
    object UI {
        /** 検索デバウンス時間（ミリ秒） */
        const val SEARCH_DEBOUNCE_MS = 300L

        /** アニメーション時間（ミリ秒） */
        const val ANIMATION_DURATION_MS = 300L

        /** スナックバー表示時間（ミリ秒） */
        const val SNACKBAR_DURATION_MS = 3000L

        /** 最小タッチターゲットサイズ（dp） */
        const val MIN_TOUCH_TARGET_DP = 48
    }
}
