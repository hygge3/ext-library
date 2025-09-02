package ext.library.tool.biz.exception;

import ext.library.tool.biz.response.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * http 状态码
 */
@Getter
@AllArgsConstructor
public enum BizCode implements ResponseCode {

    // region 2xx Success

    /**
     * 成功
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">HTTP/1.1:
     * Semantics and Content, section 6.3.1</a>
     */
    SUCCESS(200, "Success"),

    // endregion

    // region 4xx Client Error

    /**
     * 参数错误
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">HTTP/1.1:
     * Semantics and Content, section 6.5.1</a>
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * 未认证
     *
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1:
     * Authentication, section 3.1</a>
     */
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * 未授权
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1:
     * Semantics and Content, section 6.5.3</a>
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * {@code 404 Not Found}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1:
     * Semantics and Content, section 6.5.4</a>
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * {@code 405 Method Not Allowed}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">HTTP/1.1:
     * Semantics and Content, section 6.5.5</a>
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

    // endregion

    // region 5xx Server Error

    /**
     * 服务异常
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">HTTP/1.1:
     * Semantics and Content, section 6.6.1</a>
     */
    SERVER_ERROR(500, "Internal Server Error"),

    /**
     * {@code 502 Bad Gateway}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">HTTP/1.1:
     * Semantics and Content, section 6.6.3</a>
     */
    BAD_GATEWAY(502, "Bad Gateway"),

    // endregion

    // region 6xx Custom Error

    /** 自定义错误消息 */
    WARN(600, "Unknown"),

    /** 非法参数 */
    ILLEGAL_ARGUMENT(601, "Illegal Argument"),

    /**
     * 数据库执行异常
     */
    DATABASE_ERROR(602, "Database Error"),

    /**
     * 加/脱密异常
     */
    SENSITIVE_ERROR(603, "Sensitive Error"),

    /**
     * 国际化异常
     */
    I18N_ERROR(604, "Internationalization Error"),

    /**
     * 幂等异常
     */
    IDEMPOTENT_ERROR(605, "Idempotent Error"),

    /**
     * IP 定位异常
     */
    IP_LOCATION_ERROR(606, "IP Location Error"),

    /**
     * json 解析错误
     */
    JSON_PARSE_ERROR(607, "Json Serialization Error"),

    /**
     * 执行日志异常
     */
    OPERATION_LOG_ERROR(608, "Operation Log Error"),

    /**
     * 邮件发送异常
     */
    MAIL_SEND_ERROR(609, "Mail Send Error"),

    /**
     * Redis 操作异常
     */
    REDIS_ERROR(610, "Redis Error"),

    /**
     * 通用的逻辑校验异常
     */
    LOGIC_CHECK_ERROR(611, "Logic Check Error"),

    /**
     * 恶意请求
     */
    MALICIOUS_REQUEST(612, "Malicious Request"),

    /**
     * 重复执行
     */
    REPEATED_EXECUTE(613, "Repeated execute"),

    /**
     * IP 定位失败
     */
    IP_LOCATION_FAIL(614, "Ip location fail"),

    /** 调用第三方服务失败 */
    CELL_THIRD_ERROR(615, "Failed to call third-party service"),

    /**
     * 接口未实现
     */
    NOT_IMPLEMENTED(650, "Not Implemented");

    // endregion

    final int code;

    final String msg;

    /**
     * 创建异常
     *
     * @return {@code BizException }
     */
    public BizException create() {
        return new BizException(this);
    }

}