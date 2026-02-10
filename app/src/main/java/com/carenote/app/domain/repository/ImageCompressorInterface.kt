package com.carenote.app.domain.repository

import android.net.Uri

/**
 * 画像圧縮機能を定義するインターフェース
 *
 * テスト時に Fake 実装を注入できるようにするために導入。
 */
interface ImageCompressorInterface {
    suspend fun compress(sourceUri: Uri): Uri

    /** 期限切れ・サイズ超過のキャッシュファイルを削除する */
    suspend fun cleanupCache()
}
