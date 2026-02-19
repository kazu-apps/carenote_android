package com.carenote.app.config

/**
 * 外部サービス連携の設定値
 */
object IntegrationConfigs {

    /**
     * Google Play Billing 関連の設定値
     */
    object Billing {
        const val MONTHLY_PRODUCT_ID = "carenote_premium_monthly"
        const val YEARLY_PRODUCT_ID = "carenote_premium_yearly"
        const val CONNECTION_RETRY_DELAY_MS = 3_000L
        const val MAX_CONNECTION_RETRIES = 3
        const val GOOGLE_PLAY_SUBSCRIPTION_URL =
            "https://play.google.com/store/account/subscriptions"
        const val CLOUD_FUNCTIONS_REGION = "asia-northeast1"
        const val VERIFY_PURCHASE_FUNCTION_NAME = "verifyPurchase"
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
