package ext.library.ratelimiter.handler;

import ext.library.ratelimiter.annotation.RateLimiter;
import ext.library.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;

import jakarta.annotation.Nonnull;

/**
 * Redis 速率限制处理器
 */
@Slf4j
public class RedisRateLimitHandler implements IRateLimitHandler {

    @Override
    public boolean proceed(@Nonnull RateLimiter rateLimiter, JoinPoint point) {
        return RedisUtil.rateLimiter(getCombineKey(rateLimiter, point), rateLimiter.count(), rateLimiter.interval());
    }

}