package ext.library.idempotent.config;

/**
 * 幂等 Key 存储类型
 */
public enum KeyStoreType {

    /**
     * 自动检测（默认）
     * <p>
     * 根据类路径中存在的依赖自动选择存储实现，优先级：Redis > PostgreSQL > Memory。
     * 未配置 {@code ext.idempotent.key-store-type} 时默认使用此模式。
     */
    AUTO,

    /**
     * 内存存储（基于 Caffeine）
     * <p>
     * 适用于单机部署场景，需要 {@code caffeine} 依赖。
     */
    MEMORY,

    /**
     * Redis 存储
     * <p>
     * 适用于分布式部署场景，需要 {@code ext-redis} 依赖。
     */
    REDIS,

    /**
     * PostgreSQL 存储
     * <p>
     * 适用于 PostgreSQL 部署场景，使用 UNLOGGED 表实现。需要 {@code ext-postgres} 依赖。
     */
    POSTGRES

}
