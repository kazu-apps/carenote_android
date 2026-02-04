package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata

/**
 * Firestore Document と Domain Model の双方向変換を行うインターフェース
 *
 * @param Domain Domain Model 型
 */
interface RemoteMapper<Domain> {
    /**
     * Firestore ドキュメントデータから Domain Model に変換
     *
     * @param data Firestore ドキュメントの Map 表現
     * @return Domain Model
     * @throws IllegalArgumentException 必須フィールドが欠落している場合
     */
    fun toDomain(data: Map<String, Any?>): Domain

    /**
     * Domain Model から Firestore ドキュメントデータに変換
     *
     * @param domain Domain Model
     * @param syncMetadata 同期メタデータ（null の場合はメタデータなし）
     * @return Firestore ドキュメント用 Map
     */
    fun toRemote(domain: Domain, syncMetadata: SyncMetadata? = null): Map<String, Any?>

    /**
     * Firestore ドキュメントデータのリストから Domain Model リストに変換
     *
     * @param dataList Firestore ドキュメントの Map 表現リスト
     * @return Domain Model リスト
     */
    fun toDomainList(dataList: List<Map<String, Any?>>): List<Domain> =
        dataList.map { toDomain(it) }

    /**
     * Firestore ドキュメントデータから同期メタデータを抽出
     *
     * @param data Firestore ドキュメントの Map 表現
     * @return 同期メタデータ
     * @throws IllegalArgumentException 必須フィールドが欠落している場合
     */
    fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata
}
