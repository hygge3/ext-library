package ext.library.resilience.degrade;

import ext.library.servlet.exception.HttpAware;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.BizCode;
import ext.library.tool.exception.ExtException;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * 原方法在隔离线程池里执行超时
 * <p>
 * 对外是 {@code 504}：请求已发出但没有及时拿到结果，与「服务熔断中」的 {@code 503} 区分开，
 * 便于调用方判断是超时还是被熔断器挡掉。
 */
public class IsolationTimeoutException extends ExtException implements HttpAware {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient long timeoutMillis;

    public IsolationTimeoutException(String target, long timeoutMillis) {
        super(EmojiSymbol.DEGRADE, "调用超时（{}ms）：{}", timeoutMillis, target);
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.GATEWAY_TIMEOUT;
    }

    @Override
    public BizCode bizCode() {
        return BizCode.SERVICE_UNAVAILABLE;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

}
