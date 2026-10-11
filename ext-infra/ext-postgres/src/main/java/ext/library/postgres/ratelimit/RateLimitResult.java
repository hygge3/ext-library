package ext.library.postgres.ratelimit;

/**
 * 限流检查结果
 *
 * @param allowed      是否允许请求
 * @param currentCount 当前请求数
 * @param remaining    剩余可用请求数
 * @param limit        限制阈值
 * @param resetAt      窗口重置时间戳（毫秒）
 * @since 4.0.0
 */
public record RateLimitResult(
        boolean allowed,
        int currentCount,
        int remaining,
        int limit,
        long resetAt
) {

    /**
     * 是否被拒绝
     */
    public boolean isDenied() {
        return !allowed;
    }

    /**
     * 获取距离窗口重置的剩余毫秒数
     */
    public long getResetInMillis() {
        return Math.max(0, resetAt - System.currentTimeMillis());
    }

    /**
     * 获取距离窗口重置的剩余秒数
     */
    public long getResetInSeconds() {
        return getResetInMillis() / 1000;
    }
}
