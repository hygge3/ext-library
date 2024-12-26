package ext.library.redis.config.properties;

import jakarta.annotation.Resource;

import org.jetbrains.annotations.Contract;

/**
 * 缓存配置持有者，方便静态获取配置信息
 */
public final class RedisPropertiesHolder {

    private static final RedisPropertiesHolder INSTANCE = new RedisPropertiesHolder();

    private RedisProperties redisProperties;

    @Resource
    public void setRedisProperties(RedisProperties redisProperties) {
        INSTANCE.redisProperties = redisProperties;
    }

    @Contract(pure = true)
    private static RedisProperties redisProperties() {
        return INSTANCE.redisProperties;
    }

    public static String keyPrefix() {
        return redisProperties().getKeyPrefix();
    }

    public static String lockKeySuffix() {
        return redisProperties().getLockKeySuffix();
    }

    public static String delimiter() {
        return redisProperties().getDelimiter();
    }

    public static String nullValue() {
        return redisProperties().getNullValue();
    }

    public static long expireTime() {
        return redisProperties().getExpireTime();
    }

    public static long defaultLockTimeout() {
        return redisProperties().getDefaultLockTimeout();
    }

}
