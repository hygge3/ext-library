package ext.library.servlet.exception;

import ext.library.tool.exception.BizCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * 自带 HTTP 语义的异常
 * <p>
 * 限流、熔断这类异常定义在业务层模块里，ext-web 依赖不到它们；由异常自己声明
 * 「HTTP 状态 + 业务码 + 额外响应头」，全局异常处理器照着填，各模块不用各写一份 advice。
 *
 * @since 4.1.0
 */
public interface HttpAware {

    /**
     * 对外返回的 HTTP 状态
     *
     * @return HTTP 状态
     */
    HttpStatus status();

    /**
     * 响应体里的业务码
     *
     * @return 业务码
     */
    BizCode bizCode();

    /**
     * 额外响应头，如 {@code Retry-After}
     *
     * @return 响应头，无则空
     */
    default Map<String, String> headers() {
        return Map.of();
    }

}
