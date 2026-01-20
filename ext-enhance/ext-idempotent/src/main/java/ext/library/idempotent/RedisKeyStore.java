package ext.library.idempotent;

import ext.library.redis.util.RedisUtil;

import java.time.Duration;

/**
 * 基于 Redis 的幂等 Key 存储实现
 * <p>
 * 使用 Redis SETNX + EXPIRE 原子操作实现，适用于分布式部署场景。
 */
public class RedisKeyStore implements KeyStore {

    @Override
    public boolean saveIfAbsent(String key, Duration duration) {
        return RedisUtil.setNxEx(key, String.valueOf(System.currentTimeMillis()), duration);
    }

    @Override
    public void remove(String key) {
        RedisUtil.del(key);
    }

}
