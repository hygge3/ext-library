package ext.library.ratelimit.store;

import ext.library.ratelimit.RateLimitResult;
import ext.library.redis.util.RedisUtil;

import java.time.Duration;

/**
 * Redis 限流存储
 * <p>
 * 基于 {@code INCRBY + EXPIRE} 实现滑动窗口计数，集群环境下天然可用。
 */
public class RedisRateLimitStore implements RateLimitStore {

    @Override
    public RateLimitResult increment(String key, int limit, Duration window) {
        long count = RedisUtil.incrByAndExpire(key, 1, window.toSeconds());
        Long ttl = RedisUtil.ttl(key);
        Duration resetIn = ttl == null || ttl < 0 ? window : Duration.ofSeconds(ttl);
        return new RateLimitResult(count <= limit, (int) count, (int) Math.max(0, limit - count), limit, resetIn);
    }

    @Override
    public boolean reset(String key) {
        return RedisUtil.del(key);
    }

    @Override
    public Type getType() {
        return Type.REDIS;
    }

}
