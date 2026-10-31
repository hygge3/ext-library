package ext.library.tool.exception;

/**
 * 响应码接口
 * <p>
 * 定义业务响应码的统一契约，所有业务错误码枚举（如 {@link BizCode}）以及业务异常
 * （如 {@link BizException}）都应实现此接口，从而可以统一地被序列化为响应体，
 * 例如 {@code R.failed(responseCode)}。
 *
 * @since 2025.01.01
 */
public interface ResponseCode {

    /**
     * 获取业务码
     *
     * @return 业务码（整数类型）
     */
    int getCode();

    /**
     * 获取错误消息
     *
     * @return 错误消息描述
     */
    String getMsg();
}
