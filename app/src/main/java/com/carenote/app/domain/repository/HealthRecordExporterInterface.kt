package com.carenote.app.domain.repository

import android.net.Uri
import com.carenote.app.domain.model.HealthRecord

/**
 * 健康記録 CSV エクスポート機能を定義するインターフェース
 */
interface HealthRecordCsvExporterInterface {
    suspend fun export(records: List<HealthRecord>): Uri
}

/**
 * 健康記録 PDF エクスポート機能を定義するインターフェース
 */
interface HealthRecordPdfExporterInterface {
    suspend fun export(records: List<HealthRecord>): Uri
}
