package com.carenote.app.config

/**
 * UI・テーマ・表示関連の設定値
 */
object UiConfigs {

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
     * テーマ関連の設定値
     */
    object Theme {
        /** デフォルトのテーマモード名 */
        const val DEFAULT_THEME_MODE = "SYSTEM"
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
}
