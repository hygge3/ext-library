package ext.library.redis.cache.manager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * redis cache 扩展 cache name 自动化配置
 */
public class ExtRedisCacheManager extends RedisCacheManager {

    @Contract("_,null->fail;null,!null->fail")
    public ExtRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    @NotNull
    protected RedisCache createRedisCache(@NotNull String name, RedisCacheConfiguration cacheConfig) {
        if ($.isBlank(name) || !name.contains(Symbol.SHARP)) {
            return super.createRedisCache(name, cacheConfig);
        }
        String[] cacheArray = name.split(Symbol.SHARP);
        if (cacheArray.length < 2) {
            return super.createRedisCache(name, cacheConfig);
        }
        String cacheName = cacheArray[0];
        if (cacheConfig != null) {
            // 转换时间，支持时间单位例如：300ms，第二个参数是默认单位
            Duration duration = DurationStyle.detectAndParse(cacheArray[1], ChronoUnit.SECONDS);
            cacheConfig = cacheConfig.entryTtl(duration);
        }
        return super.createRedisCache(cacheName, cacheConfig);
    }

}
