package ext.library.ratelimiter.handler;

import com.google.common.util.concurrent.RateLimiter;
import ext.library.ratelimiter.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.boot.convert.DurationStyle;

import jakarta.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guava 速率限制器处理程序
 *
 * @since 2025.08.29
 */
@Slf4j
public class RateLimiterHandler implements IRateLimitHandler {

    /**
     * 不同的方法存放不同的令牌桶
     */
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Override
    public boolean proceed(@Nonnull RateLimit rateLimit, JoinPoint point) {
        String key = getCombineKey(rateLimit, point);
        Duration interval = DurationStyle.detectAndParse(rateLimit.interval());
        RateLimiter rateLimiter;
        // 判断 map 集合中是否有创建好的令牌桶
        if (!rateLimiterMap.containsKey(rateLimit.key())) {
            // 创建令牌桶，以 n r/s往桶中放入令牌
            rateLimiter = RateLimiter.create(rateLimit.count(), interval);
            rateLimiterMap.put(key, rateLimiter);
        }
        rateLimiter = rateLimiterMap.get(key);

        // 获取令牌
        boolean acquire = rateLimiter.tryAcquire(interval);
        if (acquire) {
            if (log.isDebugEnabled()) {
                log.debug("[🚥] {}", rateLimiter);
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("[🚥] 限流规则已触发");
        }

        return false;
    }

}