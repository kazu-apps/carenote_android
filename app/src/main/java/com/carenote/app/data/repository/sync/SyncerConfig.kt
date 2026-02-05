package com.carenote.app.data.repository.sync

import com.carenote.app.data.remote.model.SyncMetadata
import java.time.LocalDateTime

/**
 * 標準 Syncer の全委譲パターンを設定で表現する data class
 *
 * 5つの標準 Syncer（Medication, Note, HealthRecord, CalendarEvent, Task）は
 * EntitySyncer の抽象メソッドを DAO / EntityMapper / RemoteMapper に委譲するだけの
 * ボイラープレートコードである。SyncerConfig はこの委譲パターンを設定として表現し、
 * ConfigDrivenEntitySyncer と組み合わせてボイラープレートを排除する。
 *
 * @param Entity Room Entity 型
 * @param Domain Domain Model 型
 */
data class SyncerConfig<Entity, Domain>(
    /** エンティティタイプ識別子 */
    val entityType: String,
    /** Firestore コレクションパスを生成 */
    val collectionPath: (careRecipientId: String) -> String,
    /** 全ローカルエンティティを取得 */
    val getAllLocal: suspend () -> List<Entity>,
    /** ID でローカルエンティティを取得 */
    val getLocalById: suspend (id: Long) -> Entity?,
    /** ローカルエンティティを保存（挿入または更新） */
    val saveLocal: suspend (entity: Entity) -> Long,
    /** ローカルエンティティを削除 */
    val deleteLocal: suspend (id: Long) -> Unit,
    /** Entity → Domain 変換 */
    val entityToDomain: (entity: Entity) -> Domain,
    /** Domain → Entity 変換 */
    val domainToEntity: (domain: Domain) -> Entity,
    /** Domain → Firestore Map 変換 */
    val domainToRemote: (domain: Domain, syncMetadata: SyncMetadata) -> Map<String, Any?>,
    /** Firestore Map → Domain 変換 */
    val remoteToDomain: (data: Map<String, Any?>) -> Domain,
    /** SyncMetadata を Firestore Map から抽出 */
    val extractSyncMetadata: (data: Map<String, Any?>) -> SyncMetadata,
    /** Entity から localId を取得 */
    val getLocalId: (entity: Entity) -> Long,
    /** Entity から updatedAt を取得 */
    val getUpdatedAt: (entity: Entity) -> LocalDateTime
)
