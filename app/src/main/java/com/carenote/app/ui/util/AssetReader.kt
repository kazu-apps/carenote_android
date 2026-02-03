package com.carenote.app.ui.util

import android.content.Context

/**
 * assets/ ディレクトリからテキストファイルを読み込む
 */
fun readAssetText(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}
