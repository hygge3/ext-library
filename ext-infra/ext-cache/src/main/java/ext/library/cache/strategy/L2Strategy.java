package ext.library.cache.strategy;

import ext.library.cache.enums.L2Backend;
import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.util.ClassUtils;

import java.time.Duration;

/**
 * 二级缓存策略
 * <p>
 * 结合 Caffeine 本地缓存和分布式缓存（Redis 或 PostgreSQL），实现多级缓存架构。
 * 读取时先查询本地缓存，未命中则查询分布式缓存并回填本地缓存。
 * <p>
 * 分布式缓存后端通过 {@code ext.cache.l2-backend} 配置项指定：
 * <ul>
 *     <li>{@link L2Backend#AUTO} - 自动检测，优先 Redis，其次 PostgreSQL（默认）</li>
 *     <li>{@link L2Backend#REDIS} - 使用 Redis</li>
 *     <li>{@link L2Backend#POSTGRES} - 使用 PostgreSQL</li>
 * </ul>
 *
 * @since 2025.08.29
 */
public class L2Strategy implements CacheStrategy {

    private final CacheStrategy caffeineStrategy = new CaffeineStrategy();

    private volatile CacheStrategy distributedStrategy;
    private volatile L2Backend backendType;

    /**
     * 获取分布式缓存策略（延迟初始化）
     */
    private CacheStrategy getDistributedStrategy() {
        if (distributedStrategy == null) {
            synchronized (this) {
                if (distributedStrategy == null) {
                    CacheProperties properties = SpringUtil.getBean(CacheProperties.class);
                    distributedStrategy = createDistributedStrategy(properties.getL2Backend());
                    Logs.info(EmojiSymbol.CACHE, "L2 缓存后端: {}", backendType);
                }
            }
        }
        return distributedStrategy;
    }

    /**
     * 根据后端类型创建分布式缓存策略
     * <p>
     * {@link L2Backend#AUTO} 时通过类路径检测自动选择，优先级：Redis > PostgreSQL。
     */
    private CacheStrategy createDistributedStrategy(L2Backend configured) {
        return switch (configured) {
            case AUTO -> {
                ClassLoader cl = getClass().getClassLoader();
                if (ClassUtils.isPresent("ext.library.redis.util.RedisUtil", cl)) {
                    backendType = L2Backend.REDIS;
                    yield new RedisStrategy();
                }
                if (ClassUtils.isPresent("ext.library.postgres.util.PostgresUtil", cl)) {
                    backendType = L2Backend.POSTGRES;
                    yield new PostgresStrategy();
                }
                throw new IllegalStateException(
                        "L2 后端自动检测失败：类路径中未找到 ext-redis 或 ext-postgres 依赖，" +
                        "请引入相应模块或将 ext.cache.cache-storage 改为 CAFFEINE");
            }
            case REDIS -> {
                backendType = L2Backend.REDIS;
                yield new RedisStrategy();
            }
            case POSTGRES -> {
                backendType = L2Backend.POSTGRES;
                yield new PostgresStrategy();
            }
        };
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        // 先查询 Caffeine 本地缓存
        T caffeineCache = caffeineStrategy.get(cacheName, key, clazz);
        if (caffeineCache != null) {
            Logs.debug(EmojiSymbol.CACHE, "从 Caffeine 中获取数据");
            return caffeineCache;
        }

        // 本地未命中，查询分布式缓存
        CacheStrategy distributed = getDistributedStrategy();
        T distributedCache = distributed.get(cacheName, key, clazz);
        if (distributedCache != null) {
            Logs.debug(EmojiSymbol.CACHE, "从 {} 获取数据", backendType);
            // 回填到 Caffeine 本地缓存
            caffeineStrategy.put(cacheName, key, distributedCache);
            return distributedCache;
        }

        return null;
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        getDistributedStrategy().put(cacheName, key, value, expireTime);
        caffeineStrategy.put(cacheName, key, value, expireTime);
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        getDistributedStrategy().put(cacheName, key, value);
        caffeineStrategy.put(cacheName, key, value);
        return value;
    }

    @Override
    public void evict(String cacheName, String key) {
        getDistributedStrategy().evict(cacheName, key);
        caffeineStrategy.evict(cacheName, key);
    }

    @Override
    public void clear(String cacheName) {
        getDistributedStrategy().clear(cacheName);
        caffeineStrategy.clear(cacheName);
    }

}
