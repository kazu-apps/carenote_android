package com.carenote.app.data.mapper

/**
 * Entity と Domain Model の双方向変換を行うインターフェース
 *
 * @param Entity Room Entity 型
 * @param Domain Domain Model 型
 */
interface Mapper<Entity, Domain> {
    /**
     * Entity から Domain Model に変換
     */
    fun toDomain(entity: Entity): Domain

    /**
     * Domain Model から Entity に変換
     */
    fun toEntity(domain: Domain): Entity

    /**
     * Entity リストから Domain Model リストに変換
     */
    fun toDomainList(entities: List<Entity>): List<Domain> = entities.map { toDomain(it) }

    /**
     * Domain Model リストから Entity リストに変換
     */
    fun toEntityList(domains: List<Domain>): List<Entity> = domains.map { toEntity(it) }
}
