package com.carenote.app.data.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Firebase Cloud Messaging サービス
 *
 * FCM からのプッシュ通知メッセージを受信し処理する。
 * - onNewToken: FCM トークンが更新された際に呼び出される
 * - onMessageReceived: プッシュ通知メッセージを受信した際に呼び出される
 */
@AndroidEntryPoint
class CareNoteMessagingService : FirebaseMessagingService() {

    /**
     * FCM トークンが更新された際に呼び出される
     *
     * 現在はログ記録のみ。サーバーへのトークン送信は v3.0 以降で実装予定。
     *
     * @param token 新しい FCM トークン
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM token refreshed")
    }

    /**
     * プッシュ通知メッセージを受信した際に呼び出される
     *
     * 現在はログ記録のみ。リモート通知の表示処理は v3.0 以降で実装予定。
     *
     * @param message 受信した RemoteMessage
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: ${message.messageId}")
    }
}
