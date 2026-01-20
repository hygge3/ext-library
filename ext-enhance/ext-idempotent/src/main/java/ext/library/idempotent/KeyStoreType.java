package ext.library.idempotent;

/**
 * 幂等 Key 存储类型
 */
public enum KeyStoreType {

    /**
     * 内存存储（基于 Caffeine）
     * <p>
     * 适用于单机部署场景
     */
    MEMORY,

    /**
     * Redis 存储
     * <p>
     * 适用于分布式部署场景
     */
    REDIS

}
