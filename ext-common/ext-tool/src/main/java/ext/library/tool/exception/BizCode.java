package ext.library.tool.exception;

/**
 * 业务错误码枚举
 * <p>
 * 采用 6xx 段按模块分类的错误码体系：
 * <ul>
 *   <li>600-609: 通用错误（参数、未知异常等）</li>
 *   <li>610-619: 工具模块</li>
 *   <li>620-629: 数据库模块</li>
 *   <li>630-639: 缓存模块（Redis）</li>
 *   <li>640-649: 安全/加密模块</li>
 *   <li>650-659: 业务逻辑模块</li>
 *   <li>660-669: 外部服务模块（邮件、第三方调用）</li>
 *   <li>670-679: 日志/监控模块</li>
 *   <li>690-699: 保留/未实现</li>
 * </ul>
 *
 * @since 2025.01.01
 */
public enum BizCode implements ResponseCode {

    // region 600-609 通用错误

    /** 未知的内部异常 */
    UNKNOWN(600, "未知的内部异常"),

    /** 非法参数 */
    ILLEGAL_ARGUMENT(601, "不符合预期或不合法的参数"),

    /** 恶意请求 */
    MALICIOUS_REQUEST(602, "恶意请求"),

    /** 重复执行 */
    REPEATED_EXECUTE(603, "重复执行"),

    // endregion

    // region 610-619 工具模块

    /** 工具异常 */
    TOOL_ERROR(610, "工具异常"),

    /** JSON 解析错误 */
    JSON_PARSE_ERROR(611, "JSON 解析错误"),

    /** 国际化异常 */
    I18N_ERROR(612, "国际化异常"),

    /** IP 定位异常 */
    IP_LOCATION_ERROR(613, "IP 定位异常"),

    // endregion

    // region 620-629 数据库模块

    /** 数据库执行异常 */
    DATABASE_ERROR(620, "数据库错误"),

    // endregion

    // region 630-639 缓存模块

    /** Redis 操作异常 */
    REDIS_ERROR(630, "Redis 操作异常"),

    // endregion

    // region 640-649 安全/加密模块

    /** 加解密异常 */
    CRYPTO_ERROR(640, "加解密错误"),

    // endregion

    // region 650-659 业务逻辑模块

    /** 通用的逻辑校验异常 */
    LOGIC_CHECK_ERROR(650, "逻辑校验异常"),

    /** 幂等异常 */
    IDEMPOTENT_ERROR(651, "幂等异常"),

    // endregion

    // region 660-669 外部服务模块

    /** 邮件发送异常 */
    MAIL_SEND_ERROR(660, "邮件发送异常"),

    /** 调用第三方服务失败 */
    THIRD_PARTY_ERROR(661, "调用第三方服务失败"),

    // endregion

    // region 670-679 日志/监控模块

    /** 执行日志异常 */
    OPERATION_LOG_ERROR(670, "执行日志异常"),

    // endregion

    // region 690-699 保留/未实现

    /** 接口未实现 */
    NOT_IMPLEMENTED(690, "接口未实现");

    // endregion

    private final int code;

    private final String msg;

    BizCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
