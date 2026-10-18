package ext.library.ratelimit.store;

import ext.library.postgres.ratelimit.PostgresRateLimiter;
import ext.library.ratelimit.RateLimitResult;

import java.time.Duration;

/**
 * PostgreSQL 限流存储
 * <p>
 * 直接复用 {@link PostgresRateLimiter} 的固定窗口计数，仅做结果类型转换。
 * {@link PostgresRateLimiter} 由 ext-postgres 的自动配置提供。
 */
public class PostgresRateLimitStore implements RateLimitStore {

    /** PostgreSQL 限流器 */
    private final PostgresRateLimiter rateLimiter;

    /**
     * 构造存储
     *
     * @param rateLimiter PostgreSQL 限流器
     */
    public PostgresRateLimitStore(PostgresRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public RateLimitResult increment(String key, int limit, Duration window) {
        var result = rateLimiter.checkAndIncrement(key, limit, window);
        return new RateLimitResult(
                result.allowed(),
                result.currentCount(),
                result.remaining(),
                result.limit(),
                Duration.ofMillis(result.getResetInMillis())
        );
    }

    @Override
    public boolean reset(String key) {
        rateLimiter.reset(key);
        // PostgresRateLimiter#reset 无返回值，无法区分「命中并清除」与「key 本就不存在」
        return true;
    }

    @Override
    public Type getType() {
        return Type.POSTGRES;
    }

}
