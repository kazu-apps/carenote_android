package com.carenote.app.config

/**
 * ドメイン機能の設定値
 */
object DomainConfigs {

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
}
