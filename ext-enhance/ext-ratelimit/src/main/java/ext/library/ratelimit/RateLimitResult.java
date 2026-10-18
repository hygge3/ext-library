package ext.library.ratelimit;

import java.time.Duration;

/**
 * 限流检查结果
 *
 * @param allowed      是否放行
 * @param currentCount 当前窗口内的请求数
 * @param remaining    剩余可用请求数
 * @param limit        阈值
 * @param resetIn      距离窗口重置的剩余时长
 */
public record RateLimitResult(
        boolean allowed,
        int currentCount,
        int remaining,
        int limit,
        Duration resetIn
) {

    /**
     * 是否被拒绝
     */
    public boolean isDenied() {
        return !allowed;
    }

    /**
     * 建议的重试秒数，供 {@code Retry-After} 响应头使用
     */
    public long getRetryAfterSeconds() {
        return Math.max(1, resetIn.toSeconds());
    }

}
