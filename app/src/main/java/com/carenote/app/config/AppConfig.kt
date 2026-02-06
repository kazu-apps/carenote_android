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
        /** 薬名の最大文字数 */
        const val NAME_MAX_LENGTH = 100

        /** 用量の最大文字数 */
        const val DOSAGE_MAX_LENGTH = 200

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

        /** Deep Link URI スキーム */
        const val DEEP_LINK_SCHEME = "carenote"
    }

    /**
     * メモ関連の設定値
     */
    object Note {
        /** タイトルの最大文字数 */
        const val TITLE_MAX_LENGTH = 100

        /** 内容の最大文字数 */
        const val CONTENT_MAX_LENGTH = 5000

        /** メモ内容プレビューの最大行数 */
        const val CONTENT_PREVIEW_MAX_LINES = 2

        /** タイトルの最大行数 */
        const val TITLE_MAX_LINES = 1

        /** 内容入力の最小行数 */
        const val CONTENT_MIN_LINES = 5
    }

    /**
     * 健康記録の入力値バリデーション範囲
     */
    object HealthRecord {
        /** 体調メモの最大文字数 */
        const val CONDITION_NOTE_MAX_LENGTH = 500

        const val TEMPERATURE_MIN = 34.0
        const val TEMPERATURE_MAX = 42.0
        const val BLOOD_PRESSURE_MIN = 40
        const val BLOOD_PRESSURE_MAX = 250
        const val PULSE_MIN = 30
        const val PULSE_MAX = 200
        const val WEIGHT_MIN = 20.0
        const val WEIGHT_MAX = 200.0
    }

    /**
     * グラフ表示の設定値
     */
    object Graph {
        /** チャートの高さ（dp） */
        const val CHART_HEIGHT_DP = 220

        /** 体温Y軸: 最小値（℃） */
        const val TEMPERATURE_Y_MIN = 35.0

        /** 体温Y軸: 最大値（℃） */
        const val TEMPERATURE_Y_MAX = 40.0

        /** 血圧Y軸: 最小値（mmHg） */
        const val BLOOD_PRESSURE_Y_MIN = 40

        /** 血圧Y軸: 最大値（mmHg） */
        const val BLOOD_PRESSURE_Y_MAX = 200

        /** 折れ線の太さ（dp） */
        const val LINE_STROKE_WIDTH_DP = 2.5f

        /** データポイントの半径（dp） */
        const val POINT_RADIUS_DP = 4f

        /** 異常値ポイントの半径（dp） */
        const val ABNORMAL_POINT_RADIUS_DP = 5f

        /** グリッド線の太さ（dp） */
        const val GRID_STROKE_WIDTH_DP = 0.5f

        /** 閾値線の太さ（dp） */
        const val THRESHOLD_STROKE_WIDTH_DP = 1f

        /** 閾値線の破線間隔（dp） */
        const val THRESHOLD_DASH_ON_DP = 8f

        /** 閾値線の破線ギャップ（dp） */
        const val THRESHOLD_DASH_OFF_DP = 4f

        /** Y軸ラベル領域の幅（dp） */
        const val Y_AXIS_LABEL_WIDTH_DP = 40

        /** X軸ラベル領域の高さ（dp） */
        const val X_AXIS_LABEL_HEIGHT_DP = 24

        /** チャート軸ラベルのフォントサイズ（sp） */
        const val AXIS_LABEL_FONT_SIZE_SP = 12

        /** 血圧グリッドステップ（mmHg） */
        const val BLOOD_PRESSURE_GRID_STEP = 40.0

        /** 体温グリッドステップ（℃） */
        const val TEMPERATURE_GRID_STEP = 1.0

        /** X軸の最大ラベル数 */
        const val X_AXIS_MAX_LABELS = 5

        /** 7日間 */
        const val DATE_RANGE_SEVEN_DAYS = 7L

        /** 30日間 */
        const val DATE_RANGE_THIRTY_DAYS = 30L
    }

    /**
     * カレンダーイベント関連の設定値
     */
    object Calendar {
        /** タイトルの最大文字数 */
        const val TITLE_MAX_LENGTH = 100

        /** 説明の最大文字数 */
        const val DESCRIPTION_MAX_LENGTH = 500

        /** タイトルの最大行数 */
        const val TITLE_MAX_LINES = 1

        /** 説明プレビューの最大行数 */
        const val DESCRIPTION_PREVIEW_MAX_LINES = 2

        /** 日セルの最小タッチサイズ（dp） */
        const val DAY_CELL_SIZE_DP = 48

        /** 月カレンダーの行数 */
        const val CALENDAR_ROWS = 6

        /** TimePicker デフォルト開始時刻（時） */
        const val DEFAULT_START_HOUR = 9

        /** TimePicker デフォルト開始時刻（分） */
        const val DEFAULT_START_MINUTE = 0

        /** TimePicker デフォルト終了時刻（時） */
        const val DEFAULT_END_HOUR = 10

        /** TimePicker デフォルト終了時刻（分） */
        const val DEFAULT_END_MINUTE = 0
    }

    /**
     * タスク関連の設定値
     */
    object Task {
        /** タイトルの最大文字数 */
        const val TITLE_MAX_LENGTH = 100

        /** 説明の最大文字数 */
        const val DESCRIPTION_MAX_LENGTH = 500

        /** タイトルの最大行数 */
        const val TITLE_MAX_LINES = 1

        /** 説明プレビューの最大行数 */
        const val DESCRIPTION_PREVIEW_MAX_LINES = 2

        /** 繰り返し間隔のデフォルト値 */
        const val DEFAULT_RECURRENCE_INTERVAL = 1

        /** 繰り返し間隔の最大値 */
        const val MAX_RECURRENCE_INTERVAL = 99
    }

    /**
     * 法的文書関連の設定値
     */
    object Legal {
        /** プライバシーポリシーの最終更新日 */
        const val PRIVACY_POLICY_LAST_UPDATED = "2026-02-02"

        /** 利用規約の最終更新日 */
        const val TERMS_OF_SERVICE_LAST_UPDATED = "2026-02-02"
    }

    /**
     * 時刻バリデーション範囲
     */
    object Time {
        const val HOUR_MIN = 0
        const val HOUR_MAX = 23
        const val MINUTE_MIN = 0
        const val MINUTE_MAX = 59
    }

    /**
     * テーマ関連の設定値
     */
    object Theme {
        /** デフォルトのテーマモード名 */
        const val DEFAULT_THEME_MODE = "SYSTEM"
    }

    /**
     * 認証関連の設定値
     */
    object Auth {
        /** メールアドレスの最大文字数 */
        const val EMAIL_MAX_LENGTH = 255

        /** パスワードの最小文字数 */
        const val PASSWORD_MIN_LENGTH = 8

        /** パスワードの最大文字数 */
        const val PASSWORD_MAX_LENGTH = 100

        /** 表示名の最大文字数 */
        const val DISPLAY_NAME_MAX_LENGTH = 50
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

        /** Flow の WhileSubscribed 停止タイムアウト（ミリ秒） */
        const val FLOW_STOP_TIMEOUT_MS = 5_000L

        /** 画面の水平 padding（dp） */
        const val SCREEN_HORIZONTAL_PADDING_DP = 16

        /** Auth 画面の水平 padding（dp） */
        const val AUTH_HORIZONTAL_PADDING_DP = 24

        /** セクション間の垂直スペース（dp） */
        const val CONTENT_SPACING_DP = 16

        /** リストアイテム間のスペース（dp） */
        const val ITEM_SPACING_DP = 8

        /** 小さな内部スペース（dp） */
        const val SMALL_SPACING_DP = 4

        /** ボタンの高さ（dp） */
        const val BUTTON_HEIGHT_DP = 56

        /** FAB 付きリストの下部 padding（dp） */
        const val LIST_BOTTOM_PADDING_DP = 80

        /** カード内小アイコンサイズ（dp） */
        const val ICON_SIZE_SMALL_DP = 16

        /** 標準アイコンサイズ（dp） */
        const val ICON_SIZE_MEDIUM_DP = 24

        /** ローディング/空状態アイコンサイズ（dp） */
        const val ICON_SIZE_LARGE_DP = 48

        /** 大きなアイコンサイズ（dp） */
        const val ICON_SIZE_XLARGE_DP = 64

        /** チェックボックスのサイズ（dp） */
        const val CHECKBOX_SIZE_DP = 20

        /** プログレスインジケーターの線幅（dp） */
        const val PROGRESS_STROKE_WIDTH_DP = 2

        /** 設定項目の垂直 padding（dp） */
        const val PREFERENCE_VERTICAL_PADDING_DP = 12

        /** カードの elevation（dp） */
        const val CARD_ELEVATION_DP = 1
    }

    /**
     * 同期関連の設定値
     */
    object Sync {
        /** Firestore 操作のタイムアウト（ミリ秒） */
        const val TIMEOUT_MS = 30_000L

        /** バッチ同期のサイズ */
        const val BATCH_SIZE = 100

        /** 同期エンティティタイプ数（進捗計算用） */
        const val ENTITY_TYPE_COUNT = 6

        /** 定期同期間隔（分） - WorkManager 最小値 15分 */
        const val SYNC_INTERVAL_MINUTES = 15L

        /** 初回同期遅延（分） */
        const val SYNC_INITIAL_DELAY_MINUTES = 1L

        /** 同期リトライ初期バックオフ（ミリ秒） */
        const val SYNC_BACKOFF_INITIAL_MS = 30_000L

        /** 同期最大リトライ回数 */
        const val MAX_RETRIES = 3
    }

    /**
     * 生体認証関連の設定値
     */
    object Biometric {
        /** バックグラウンドタイムアウト（ミリ秒）— この時間を超えて復帰したら再認証を要求 */
        const val BACKGROUND_TIMEOUT_MS = 30_000L
    }

    /**
     * Firebase Cloud Messaging 関連の設定値
     */
    object Fcm {
        /** 全ユーザー向けトピック */
        const val TOPIC_ALL_USERS = "all_users"
    }
}
