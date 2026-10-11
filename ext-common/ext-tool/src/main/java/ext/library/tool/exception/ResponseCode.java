package ext.library.tool.exception;

/**
 * 响应码接口
 * <p>
 * 定义业务响应码的标准接口，所有业务错误码枚举应实现此接口。
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
