package com.carenote.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.SyncRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * バックグラウンド同期ワーカー
 *
 * WorkManager を使用して定期的に Room と Firestore 間のデータ同期を実行する。
 *
 * ## 動作フロー
 * 1. ユーザー認証状態を確認
 * 2. Firestore から careRecipientId を取得
 * 3. SyncRepository.syncAll() を実行
 * 4. 結果に応じて WorkManager Result を返す
 *
 * ## リトライ戦略
 * - NetworkError → Retry（ネットワーク復帰時に再実行）
 * - DatabaseError → Retry（一時的エラーの可能性）
 * - UnknownError → Retry（リトライ回数制限あり）
 * - UnauthorizedError → Failure（再認証が必要）
 * - NotFoundError → Failure（データが存在しない）
 * - ValidationError → Failure（再実行しても成功しない）
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("SyncWorker started, attempt: $runAttemptCount")

        // 1. Check authentication
        val user = authRepository.getCurrentUser()
        if (user == null) {
            Timber.w("SyncWorker: User not authenticated, skipping sync")
            return Result.failure()
        }

        // 2. Get careRecipientId from Firestore
        val careRecipientId = getCareRecipientId(user.uid)
        if (careRecipientId == null) {
            Timber.w("SyncWorker: No careRecipientId found for current user")
            return Result.failure()
        }

        // 3. Execute sync
        return try {
            val syncResult = syncRepository.syncAll(careRecipientId)
            mapSyncResultToWorkerResult(syncResult)
        } catch (e: Exception) {
            Timber.e("SyncWorker: Unexpected error: $e")
            handleRetryOrFailure()
        }
    }

    /**
     * Firestore から careRecipientId を取得
     *
     * careRecipientMembers コレクションをクエリして、
     * 現在のユーザーが所属する careRecipient の ID を取得する。
     */
    private suspend fun getCareRecipientId(userId: String): String? {
        if (userId.isBlank()) {
            Timber.w("SyncWorker: Invalid empty userId")
            return null
        }

        return try {
            val querySnapshot = firestore.collection("careRecipientMembers")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Timber.d("SyncWorker: No careRecipientMember found")
                null
            } else {
                val document = querySnapshot.documents.first()
                document.getString("careRecipientId").also {
                    Timber.d("SyncWorker: Found careRecipientId")
                }
            }
        } catch (e: CancellationException) {
            throw e // Let cancellation propagate
        } catch (e: Exception) {
            Timber.e("SyncWorker: Failed to get careRecipientId: $e")
            null
        }
    }

    /**
     * SyncResult を WorkManager Result にマッピング
     */
    private fun mapSyncResultToWorkerResult(syncResult: SyncResult): Result {
        return when (syncResult) {
            is SyncResult.Success -> {
                Timber.d(
                    "SyncWorker: Sync successful - uploaded=${syncResult.uploadedCount}, " +
                        "downloaded=${syncResult.downloadedCount}, conflicts=${syncResult.conflictCount}"
                )
                Result.success()
            }

            is SyncResult.PartialSuccess -> {
                Timber.w(
                    "SyncWorker: Partial success - success=${syncResult.successCount}, " +
                        "failed=${syncResult.failedEntities.size}"
                )
                // 部分成功は成功として扱う（失敗したエンティティは次回同期で再試行）
                Result.success()
            }

            is SyncResult.Failure -> {
                Timber.e("SyncWorker: Sync failed - error=${syncResult.error}")
                mapDomainErrorToWorkerResult(syncResult.error)
            }
        }
    }

    /**
     * DomainError を WorkManager Result にマッピング
     *
     * リトライ可能なエラーと不可能なエラーを区別する。
     */
    private fun mapDomainErrorToWorkerResult(error: DomainError): Result {
        return when (error) {
            is DomainError.NetworkError -> {
                Timber.d("SyncWorker: Network error, will retry")
                Result.retry()
            }

            is DomainError.DatabaseError -> {
                Timber.d("SyncWorker: Database error, will retry")
                handleRetryOrFailure()
            }

            is DomainError.UnauthorizedError -> {
                Timber.w("SyncWorker: Unauthorized error, failing")
                Result.failure()
            }

            is DomainError.NotFoundError -> {
                Timber.w("SyncWorker: NotFound error, failing")
                Result.failure()
            }

            is DomainError.ValidationError -> {
                Timber.w("SyncWorker: Validation error, failing")
                Result.failure()
            }

            is DomainError.UnknownError -> {
                Timber.d("SyncWorker: Unknown error, will retry")
                handleRetryOrFailure()
            }
        }
    }

    /**
     * リトライ回数を確認して Retry または Failure を返す
     */
    private fun handleRetryOrFailure(): Result {
        return if (runAttemptCount < AppConfig.Sync.MAX_RETRIES) {
            Timber.d("SyncWorker: Retrying, attempt $runAttemptCount of ${AppConfig.Sync.MAX_RETRIES}")
            Result.retry()
        } else {
            Timber.w("SyncWorker: Max retries reached, failing")
            Result.failure()
        }
    }

    companion object {
        /** WorkManager の一意な Work 名 */
        const val WORK_NAME = "sync_work"

        /** 即時同期用の一意な Work 名 */
        const val IMMEDIATE_WORK_NAME = "immediate_sync_work"
    }
}
