package ext.library.ratelimiter.handler;

import ext.library.ratelimiter.annotation.RateLimiter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import jakarta.annotation.Nonnull;
import java.util.Collections;

/**
 * Redis 速率限制处理器
 */
@Slf4j
public class RedisRateLimitHandler implements IRateLimitHandler {

    /** Redis 中的 Lua 脚本，用于实现限流功能。它通过指定的键（key）来记录访问次数，如果访问次数超过设定的阈值（count），则拒绝访问；否则增加访问计数并设置过期时间。 */
    // language=redis
    static final RedisScript<Long> REDIS_SCRIPT_RATE_LIMIT = RedisScript.of("""
            -- 获取 key
            local key = KEYS[1];
            local count = tonumber(ARGV[1]);
            local interval = tonumber(ARGV[2]);
            local current = tonumber(redis.call('get', key) or "0")
            -- 超过限流次数直接返回 0
            if current + 1 > count then return 0
            else redis.call("INCRBY", key, "1") redis.call("EXPIRE", key, interval) return current + 1 end
            """, Long.class);

    @Setter
    static RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean proceed(@Nonnull RateLimiter rateLimiter, JoinPoint point) {
        // 间隔时间解析为秒
        long interval = DurationStyle.detectAndParse(rateLimiter.interval()).getSeconds();
        final String key = getCombineKey(rateLimiter, point);
        if (log.isDebugEnabled()) {
            log.debug("[🚥] rate.limit.key = {}", key);
        }

        Long currentCount = redisTemplate.execute(REDIS_SCRIPT_RATE_LIMIT, Collections.singletonList(key), String.valueOf(rateLimiter.count()), String.valueOf(interval));
        if (currentCount > 0 && currentCount <= rateLimiter.count()) {
            if (log.isDebugEnabled()) {
                log.debug("[🚥] 限制期内的第 {} 次访问", currentCount);
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("[🚥] 触发限流");
        }
        return false;
    }

}