package ext.library.resilience.degrade;

import ext.library.servlet.exception.HttpAware;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.BizCode;
import ext.library.tool.exception.ExtException;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * 熔断打开且没有可用的降级方法时抛出
 * <p>
 * 属于「服务暂不可用」而不是业务失败，对外映射为 {@code 503}。
 */
public class CircuitOpenException extends ExtException implements HttpAware {

    @Serial
    private static final long serialVersionUID = 1L;

    public CircuitOpenException(String message) {
        super(EmojiSymbol.DEGRADE, message);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    @Override
    public BizCode bizCode() {
        return BizCode.SERVICE_UNAVAILABLE;
    }

}
