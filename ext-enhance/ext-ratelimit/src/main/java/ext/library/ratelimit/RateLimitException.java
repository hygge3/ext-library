package ext.library.ratelimit;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;

import java.io.Serial;

/**
 * 触发限流时抛出的异常
 * <p>
 * 携带完整的限流信息，便于全局异常处理器拼装 {@code Retry-After} 等响应头。
 */
public class RateLimitException extends ExtException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 限流 key */
    private final transient String rateLimitKey;
    /** 限流结果 */
    private final transient RateLimitResult result;

    /**
     * 构造限流异常
     *
     * @param message       提示信息
     * @param rateLimitKey  限流 key
     * @param result        限流结果
     */
    public RateLimitException(String message, String rateLimitKey, RateLimitResult result) {
        super(EmojiSymbol.RATELIMIT, message);
        this.rateLimitKey = rateLimitKey;
        this.result = result;
    }

    public String getRateLimitKey() {
        return rateLimitKey;
    }

    public RateLimitResult getResult() {
        return result;
    }

}
