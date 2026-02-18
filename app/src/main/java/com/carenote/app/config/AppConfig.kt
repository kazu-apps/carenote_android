package com.carenote.app.config

/**
 * アプリケーション全体の設定値を集約
 * マジックナンバーを避け、一元管理することで保守性を向上
 */
object AppConfig {

    /**
     * ページング関連の設定値
     */
    object Paging {
        /** 1ページあたりの件数 */
        const val PAGE_SIZE = 20
    }

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

        /** デフォルトの在庫少アラートしきい値 */
        const val DEFAULT_LOW_STOCK_THRESHOLD = 5

        /** 在庫数の最大値 */
        const val MAX_STOCK = 9999
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

        /** 無料ユーザーのタスクリマインダー1日あたりの上限回数 */
        const val TASK_REMINDER_FREE_DAILY_LIMIT = 3
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

        /** 繰り返し展開の最大オカレンス数 */
        const val MAX_EXPANDED_OCCURRENCES = 365
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
     * ホーム画面関連の設定値
     */
    object Home {
        /** 各セクションの最大表示件数 */
        const val MAX_SECTION_ITEMS = 5
    }

    /**
     * タイムライン関連の設定値
     */
    object Timeline {
        /** コンテンツプレビューの最大行数 */
        const val PREVIEW_MAX_LINES = 2
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
     * サポート関連の設定値
     */
    object Support {
        const val CONTACT_EMAIL = "support-carenote@ks-apps.org"
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

        /** セクション間の大きなスペース（dp） */
        const val SECTION_SPACING_DP = 32

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

        /** バッジの最大表示数（これを超えると "99+" 表示） */
        const val BADGE_MAX_COUNT = 99
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
     * セッション管理の設定値
     */
    object Session {
        /** デフォルトのセッションタイムアウト（分） */
        const val DEFAULT_TIMEOUT_MINUTES = 5

        /** セッションタイムアウトの最小値（分） */
        const val MIN_TIMEOUT_MINUTES = 1

        /** セッションタイムアウトの最大値（分） */
        const val MAX_TIMEOUT_MINUTES = 60
    }

    /**
     * セキュリティ関連の設定値
     */
    object Security {
        /** PBKDF2 イテレーション回数 */
        const val PBKDF2_ITERATIONS = 100_000

        /** PBKDF2 鍵長（ビット） */
        const val PBKDF2_KEY_LENGTH_BITS = 256

        /** PBKDF2 ソルト長（バイト） */
        const val PBKDF2_SALT_LENGTH = 32

        /** Root 検出時の短縮セッションタイムアウト（ミリ秒） */
        const val ROOT_SESSION_TIMEOUT_MS = 60_000L
    }

    /**
     * 写真添付関連の設定値
     */
    object Photo {
        /** 1 つの親エンティティに添付可能な最大写真数 */
        const val MAX_PHOTOS_PER_PARENT = 5

        /** 画像ファイルの最大サイズ（バイト）— 5MB */
        const val MAX_IMAGE_SIZE_BYTES = 5_242_880L

        /** JPEG 圧縮品質 (0–100) */
        const val COMPRESSION_QUALITY = 80

        /** リサイズ後の最大辺（px） */
        const val MAX_DIMENSION = 1920

        /** Firebase Storage のパスプレフィックス */
        const val STORAGE_PATH_PREFIX = "photos"

        /** キャッシュディレクトリの最大サイズ（バイト）— 100MB */
        const val CACHE_MAX_SIZE_BYTES = 104_857_600L

        /** キャッシュファイルの最大保持期間（ミリ秒）— 7日 */
        const val CACHE_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000

        /** キャッシュディレクトリ名 */
        const val CACHE_DIR_NAME = "photos"
    }

    /**
     * エクスポート関連の設定値
     */
    object Export {
        const val CSV_FILE_PREFIX = "health_records_"
        const val PDF_FILE_PREFIX = "health_records_"
        const val PDF_PAGE_WIDTH = 595
        const val PDF_PAGE_HEIGHT = 842
        const val PDF_MARGIN = 40
        const val PDF_FONT_SIZE_TITLE = 18f
        const val PDF_FONT_SIZE_HEADER = 10f
        const val PDF_FONT_SIZE_BODY = 9f
        const val PDF_LINE_HEIGHT = 16f
        const val PDF_HEADER_LINE_HEIGHT = 20f
        const val CACHE_DIR_NAME = "exports"
        const val MEDICATION_LOG_CSV_FILE_PREFIX = "medication_logs_"
        const val MEDICATION_LOG_PDF_FILE_PREFIX = "medication_logs_"
        const val TASK_CSV_FILE_PREFIX = "tasks_"
        const val TASK_PDF_FILE_PREFIX = "tasks_"
        const val NOTE_CSV_FILE_PREFIX = "notes_"
        const val NOTE_PDF_FILE_PREFIX = "notes_"

        /** エクスポートキャッシュファイルの最大保持期間（ミリ秒）— 1時間 */
        const val CACHE_MAX_AGE_MS = 3_600_000L
    }

    /**
     * 緊急連絡先関連の設定値
     */
    object EmergencyContact {
        /** 名前の最大文字数 */
        const val NAME_MAX_LENGTH = 100

        /** 電話番号の最大文字数 */
        const val PHONE_MAX_LENGTH = 20

        /** メモの最大文字数 */
        const val MEMO_MAX_LENGTH = 500
    }

    /**
     * ウィジェット関連の設定値
     */
    object Widget {
        /** 服薬セクションの最大表示件数 */
        const val MAX_MEDICATION_ITEMS = 5

        /** タスクセクションの最大表示件数 */
        const val MAX_TASK_ITEMS = 5
    }

    /**
     * Analytics 画面名定数
     */
    object Analytics {
        const val SCREEN_MEDICATION = "medication"
        const val SCREEN_CALENDAR = "calendar"
        const val SCREEN_TASKS = "tasks"
        const val SCREEN_HEALTH_RECORDS = "health_records"
        const val SCREEN_NOTES = "notes"
        const val SCREEN_SETTINGS = "settings"
        const val SCREEN_LOGIN = "login"
        const val SCREEN_REGISTER = "register"
        const val SCREEN_HOME = "home"

        // Auth
        const val EVENT_SIGN_IN = "sign_in"
        const val EVENT_SIGN_UP = "sign_up"
        const val EVENT_SIGN_OUT = "sign_out"
        const val EVENT_PASSWORD_RESET_SENT = "password_reset_sent"
        const val EVENT_PASSWORD_CHANGED = "password_changed"
        const val EVENT_ACCOUNT_DELETED = "account_deleted"

        // Medication
        const val EVENT_MEDICATION_CREATED = "medication_created"
        const val EVENT_MEDICATION_UPDATED = "medication_updated"
        const val EVENT_MEDICATION_DELETED = "medication_deleted"
        const val EVENT_MEDICATION_LOG_RECORDED = "medication_log_recorded"
        const val EVENT_MEDICATION_LOG_EXPORT_CSV = "medication_log_export_csv"
        const val EVENT_MEDICATION_LOG_EXPORT_PDF = "medication_log_export_pdf"

        // Calendar
        const val EVENT_CALENDAR_EVENT_CREATED = "calendar_event_created"
        const val EVENT_CALENDAR_EVENT_UPDATED = "calendar_event_updated"
        const val EVENT_CALENDAR_EVENT_DELETED = "calendar_event_deleted"
        const val EVENT_CALENDAR_EVENT_COMPLETED = "calendar_event_completed"
        const val PARAM_EVENT_TYPE = "event_type"

        // Task
        const val EVENT_TASK_CREATED = "task_created"
        const val EVENT_TASK_UPDATED = "task_updated"
        const val EVENT_TASK_COMPLETED = "task_completed"
        const val EVENT_TASK_UNCOMPLETED = "task_uncompleted"
        const val EVENT_TASK_DELETED = "task_deleted"
        const val EVENT_TASK_EXPORT_CSV = "task_export_csv"
        const val EVENT_TASK_EXPORT_PDF = "task_export_pdf"

        // Health Record
        const val EVENT_HEALTH_RECORD_CREATED = "health_record_created"
        const val EVENT_HEALTH_RECORD_UPDATED = "health_record_updated"
        const val EVENT_HEALTH_RECORD_DELETED = "health_record_deleted"
        const val EVENT_HEALTH_RECORD_EXPORT_CSV = "health_record_export_csv"
        const val EVENT_HEALTH_RECORD_EXPORT_PDF = "health_record_export_pdf"

        // Note
        const val EVENT_NOTE_CREATED = "note_created"
        const val EVENT_NOTE_UPDATED = "note_updated"
        const val EVENT_NOTE_DELETED = "note_deleted"
        const val EVENT_NOTE_EXPORT_CSV = "note_export_csv"
        const val EVENT_NOTE_EXPORT_PDF = "note_export_pdf"

        // Emergency Contact
        const val EVENT_EMERGENCY_CONTACT_CREATED = "emergency_contact_created"
        const val EVENT_EMERGENCY_CONTACT_UPDATED = "emergency_contact_updated"
        const val EVENT_EMERGENCY_CONTACT_DELETED = "emergency_contact_deleted"

        // Care Recipient
        const val EVENT_CARE_RECIPIENT_SAVED = "care_recipient_saved"

        // Sync
        const val EVENT_MANUAL_SYNC = "manual_sync"

        // Params
        const val PARAM_STATUS = "status"

        // Search
        const val SCREEN_SEARCH = "search"
        const val EVENT_SEARCH_PERFORMED = "search_performed"
        const val EVENT_SEARCH_RESULT_CLICKED = "search_result_clicked"

        // Home
        const val EVENT_HOME_SEE_ALL_CLICKED = "home_see_all_clicked"
        const val EVENT_HOME_SECTION_CLICKED = "home_section_clicked"
        const val PARAM_SECTION = "section"

        // Member
        const val SCREEN_MEMBER_MANAGEMENT = "member_management"
        const val EVENT_INVITATION_SENT = "invitation_sent"
        const val EVENT_INVITATION_ACCEPTED = "invitation_accepted"
        const val EVENT_INVITATION_CANCELLED = "invitation_cancelled"
        const val EVENT_MEMBER_DELETED = "member_deleted"
    }

    /**
     * ケア対象者関連の設定値
     */
    object CareRecipient {
        const val NAME_MAX_LENGTH = 100
        const val NICKNAME_MAX_LENGTH = 50
        const val CARE_LEVEL_MAX_LENGTH = 50
        const val MEDICAL_HISTORY_MAX_LENGTH = 1000
        const val ALLERGIES_MAX_LENGTH = 500
        const val MEMO_MAX_LENGTH = 500
    }

    /**
     * Google Play Billing 関連の設定値
     */
    object Billing {
        const val MONTHLY_PRODUCT_ID = "carenote_premium_monthly"
        const val YEARLY_PRODUCT_ID = "carenote_premium_yearly"
        const val CONNECTION_RETRY_DELAY_MS = 3_000L
        const val MAX_CONNECTION_RETRIES = 3
    }

    /**
     * Firebase Cloud Messaging 関連の設定値
     */
    object Fcm {
        /** 全ユーザー向けトピック */
        const val TOPIC_ALL_USERS = "all_users"
    }

    /**
     * メンバー管理関連の設定値
     */
    object Member {
        const val INVITATION_TOKEN_LENGTH = 32
        const val INVITATION_VALID_DAYS = 30L
        const val EMAIL_MAX_LENGTH = 254
        const val MESSAGE_MAX_LENGTH = 500
        const val DEEP_LINK_HOST = "carenote.example.com"
        const val DEEP_LINK_PATH_PREFIX = "/invite"
    }
}
