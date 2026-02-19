package com.carenote.app.config

/**
 * データ・同期・エクスポート関連の設定値
 */
object DataConfigs {

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
     * ウィジェット関連の設定値
     */
    object Widget {
        /** 服薬セクションの最大表示件数 */
        const val MAX_MEDICATION_ITEMS = 5

        /** タスクセクションの最大表示件数 */
        const val MAX_TASK_ITEMS = 5
    }
}
