package ext.library.tool.exception;

/**
 * http 状态码
 */
public enum BizCode implements ResponseCode {

    // region 6xx Custom Error

    /** 自定义错误消息 */
    WARN("600", "未知的内部异常"),

    /** 工具异常，表示在执行工具操作过程中发生的异常情况 */
    TOOL_EXCEPTION("610", "工具异常"),

    /** 非法参数 */
    ILLEGAL_ARGUMENT("601", "不符合预期或不合法的参数"),

    /**
     * 数据库执行异常
     */
    DATABASE_ERROR("602", "数据库错误"),

    /**
     * 加/脱密异常
     */
    SENSITIVE_ERROR("603", "加解密错误"),

    /**
     * 国际化异常
     */
    I18N_ERROR("604", "国际化异常"),

    /**
     * 幂等异常
     */
    IDEMPOTENT_ERROR("605", "幂等异常"),

    /**
     * IP 定位异常
     */
    IP_LOCATION_ERROR("606", "IP 定位异常"),

    /**
     * json 解析错误
     */
    JSON_PARSE_ERROR("607", "JSON 序列化 错误"),

    /**
     * 执行日志异常
     */
    OPERATION_LOG_ERROR("608", "执行日志异常"),

    /**
     * 邮件发送异常
     */
    MAIL_SEND_ERROR("609", "邮件发送异常"),

    /**
     * Redis 操作异常
     */
    REDIS_ERROR("610", "Redis 操作异常"),

    /**
     * 通用的逻辑校验异常
     */
    LOGIC_CHECK_ERROR("611", "通用的逻辑校验异常"),

    /**
     * 恶意请求
     */
    MALICIOUS_REQUEST("612", "恶意请求"),

    /**
     * 重复执行
     */
    REPEATED_EXECUTE("613", "重复执行"),

    /**
     * IP 定位失败
     */
    IP_LOCATION_FAIL("614", "IP 定位失败"),

    /** 调用第三方服务失败 */
    CELL_THIRD_ERROR("615", "调用第三方服务失败"),

    /**
     * 接口未实现
     */
    NOT_IMPLEMENTED("650", "接口未实现");

    // endregion

    private final String code;

    private final String msg;

    BizCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
