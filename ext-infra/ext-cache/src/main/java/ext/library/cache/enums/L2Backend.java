package ext.library.cache.enums;

/**
 * 二级缓存后端类型
 * <p>
 * 定义 L2 缓存策略中第二级（分布式）缓存的后端存储
 *
 * @since 4.0.0
 */
public enum L2Backend {

    /**
     * Redis 作为分布式缓存后端
     * <p>
     * 需要引入 ext-redis 模块
     */
    REDIS,

    /**
     * PostgreSQL 作为分布式缓存后端
     * <p>
     * 需要引入 ext-postgres 模块
     */
    POSTGRES
}
