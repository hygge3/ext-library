package ext.library.resilience.ratelimit;

import ext.library.servlet.exception.HttpAware;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.BizCode;
import ext.library.tool.exception.ExtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.Map;

/**
 * 触发限流时抛出的异常
 * <p>
 * 自带 {@code 429} 与 {@code Retry-After}：调用方拿到的是「被限流、多久后可重试」，
 * 而不是一个分不清原因的内部错误。
 */
public class RateLimitException extends ExtException implements HttpAware {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 限流 key */
    private final transient String rateLimitKey;
    /** 被拒时桶里还能立刻取出的令牌数 */
    private final transient long remaining;
    /** 建议等待秒数，向上取整：宁可多等一秒，也别让调用方做一次必然被拒的重试 */
    private final transient long retryAfterSeconds;

    /**
     * 构造限流异常
     *
     * @param message          提示信息
     * @param rateLimitKey     限流 key
     * @param remaining        剩余令牌数
     * @param retryAfterMillis 被拒时的等待毫秒数
     */
    public RateLimitException(String message, String rateLimitKey, long remaining, long retryAfterMillis) {
        super(EmojiSymbol.RATELIMIT, message);
        this.rateLimitKey = rateLimitKey;
        this.remaining = remaining;
        this.retryAfterSeconds = Math.max(1, (retryAfterMillis + 999) / 1_000);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }

    @Override
    public BizCode bizCode() {
        return BizCode.RATE_LIMITED;
    }

    @Override
    public Map<String, String> headers() {
        return Map.of(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
    }

    public String getRateLimitKey() {
        return rateLimitKey;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

}
