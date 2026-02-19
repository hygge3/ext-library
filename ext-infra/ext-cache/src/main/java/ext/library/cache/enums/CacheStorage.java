package ext.library.cache.enums;

/**
 * 缓存存储方式
 *
 * @since 2025.08.29
 */
public enum CacheStorage {

    /**
     * 自动检测（默认）
     * <p>
     * 根据类路径中存在的依赖自动选择缓存策略：
     * <ul>
     *     <li>存在 ext-redis 或 ext-postgres → {@link #L2}（Caffeine + 分布式缓存）</li>
     *     <li>仅有 Caffeine → {@link #CAFFEINE}</li>
     * </ul>
     * 未配置 {@code ext.cache.cache-storage} 时默认使用此模式。
     */
    AUTO,

    /**
     * Caffeine 本地内存缓存
     */
    CAFFEINE,

    /**
     * Redis 分布式缓存
     * <p>
     * 需要引入 ext-redis 模块
     */
    REDIS,

    /**
     * PostgreSQL 分布式缓存
     * <p>
     * 需要引入 ext-postgres 模块
     */
    POSTGRES,

    /**
     * 二级缓存（Caffeine + 分布式缓存）
     * <p>
     * 先查本地缓存，未命中再查分布式缓存（Redis 或 PostgreSQL），并回填本地缓存。
     * 分布式缓存后端通过 {@code ext.cache.l2-backend} 配置指定。
     */
    L2

}
