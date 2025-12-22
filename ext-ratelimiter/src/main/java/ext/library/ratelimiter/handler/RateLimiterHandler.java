package ext.library.ratelimiter.handler;

import com.google.common.util.concurrent.RateLimiter;
import ext.library.ratelimiter.annotation.RateLimit;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.core.Logs;
import org.aspectj.lang.JoinPoint;
import org.springframework.boot.convert.DurationStyle;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guava 速率限制器处理程序
 *
 * @since 2025.08.29
 */
public class RateLimiterHandler implements IRateLimitHandler {

    /**
     * 不同的方法存放不同的令牌桶
     */
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Override
    public boolean proceed(RateLimit rateLimit, JoinPoint point) {
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
            return true;
        }
        Logs.debug(EmojiSymbol.RATELIMITER, "限流规则已触发");
        return false;
    }

}
