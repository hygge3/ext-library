package ext.library.ratelimiter.handler;

import ext.library.ratelimiter.annotation.RateLimit;
import ext.library.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.data.redis.core.script.RedisScript;

import jakarta.annotation.Nonnull;
import java.util.Collections;

/**
 * Redis 速率限制处理器
 */
@Slf4j
public class RedisRateLimitHandler implements IRateLimitHandler {

    @Override
    public boolean proceed(@Nonnull RateLimit rateLimit, JoinPoint point) {
        long interval = DurationStyle.detectAndParse(rateLimit.interval()).getSeconds();
        return rateLimiter(getCombineKey(rateLimit, point), rateLimit.count(), interval);
    }

    /**
     * 限流
     *
     * @param key      限流 key
     * @param count    限定阈值，时间间隔 interval 范围内超过该数量会触发锁
     * @param interval 时间间隔，单位：秒
     *
     * @return false 表示失败
     */
    public boolean rateLimiter(String key, long count, long interval) {
        /* Redis 中的 Lua 脚本，用于实现限流功能。它通过指定的键（key）来记录访问次数，如果访问次数超过设定的阈值（count），则拒绝访问；否则增加访问计数并设置过期时间 */
        // language=redis
        RedisScript<Long> REDIS_SCRIPT_RATE_LIMIT = RedisScript.of("""
                local key = KEYS[1];
                local count = tonumber(ARGV[1]);
                local interval = tonumber(ARGV[2]);
                local current = tonumber(redis.call('get', key) or "0")
                if current + 1 > count then return 0
                else redis.call("INCRBY", key, "1") redis.call("expire", key, interval) return current + 1 end
                """, Long.class);
        Long currentCount = RedisUtil.execute(REDIS_SCRIPT_RATE_LIMIT, Collections.singletonList(key), String.valueOf(count),
                // 间隔时间解析为秒
                String.valueOf(interval));
        if (null != currentCount) {
            if (currentCount > 0 && currentCount <= count) {
                if (log.isDebugEnabled()) {
                    log.debug("[🚥] 限制期内的第 {} 次访问", currentCount);
                }
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("[🚥] 限流规则已触发");
        }
        return false;
    }

}