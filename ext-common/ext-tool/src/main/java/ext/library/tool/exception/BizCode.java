package ext.library.tool.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 业务错误码枚举
 * <p>
 * 采用「大类 + 序号」的 4 位编码：首位表示错误大类，后三位为该类下的顺序号。
 * 这样既能一眼看出错误性质，又能在大类内无限扩展而不破坏既有码值。
 * <ul>
 *   <li><b>1xxx</b> 参数/请求类：入参不合法、缺少参数、恶意请求、限流等</li>
 *   <li><b>2xxx</b> 认证/授权类：未认证、无权限、令牌失效、签名错误等</li>
 *   <li><b>3xxx</b> 资源类：资源不存在、数据冲突、唯一键重复等</li>
 *   <li><b>4xxx</b> 业务规则类：逻辑校验、幂等、重复执行、状态不允许等</li>
 *   <li><b>5xxx</b> 系统/服务端类：未知异常、未实现、数据库、缓存、加解密等</li>
 *   <li><b>6xxx</b> 外部依赖/工具类：工具、JSON、i18n、文件、邮件、短信、第三方/RPC 等</li>
 * </ul>
 * <p>
 * 所有错误码在类加载时会校验唯一性，避免因重复码值引发歧义。
 *
 * @since 2025.01.01
 */
public enum BizCode implements ResponseCode {

    // region 1xxx 参数/请求类

    /** 参数不合法 */
    INVALID_PARAMETER(1000, "不符合预期或不合法的参数"),

    /** 缺少必需参数 */
    MISSING_PARAMETER(1001, "缺少必需的参数"),

    /** 恶意请求 */
    MALICIOUS_REQUEST(1002, "恶意请求"),

    /** 请求过于频繁（限流） */
    RATE_LIMITED(1003, "请求过于频繁，请稍后再试"),

    // endregion

    // region 2xxx 认证/授权类

    /** 未认证或登录状态失效 */
    UNAUTHORIZED(2000, "未认证或登录状态已失效"),

    /** 无访问权限 */
    FORBIDDEN(2001, "无访问权限"),

    /** 令牌无效或已过期 */
    TOKEN_INVALID(2002, "令牌无效或已过期"),

    /** 签名校验失败 */
    SIGNATURE_ERROR(2003, "签名校验失败"),

    // endregion

    // region 3xxx 资源类

    /** 资源不存在 */
    NOT_FOUND(3000, "请求的资源不存在"),

    /** 数据冲突 */
    DATA_CONFLICT(3001, "数据冲突"),

    /** 数据重复（唯一键冲突） */
    DATA_DUPLICATE(3002, "数据已存在（唯一键冲突）"),

    // endregion

    // region 4xxx 业务规则类

    /** 通用的逻辑校验异常 */
    LOGIC_CHECK_ERROR(4000, "逻辑校验异常"),

    /** 幂等异常 */
    IDEMPOTENT_ERROR(4001, "幂等异常"),

    /** 重复执行 */
    REPEATED_EXECUTE(4002, "重复执行"),

    /** 当前状态不允许该操作 */
    OPERATION_NOT_ALLOWED(4003, "当前状态不允许该操作"),

    // endregion

    // region 5xxx 系统/服务端类

    /** 未知的内部异常 */
    UNKNOWN(5000, "未知的内部异常"),

    /** 接口未实现 */
    NOT_IMPLEMENTED(5001, "接口未实现"),

    /** 数据库执行异常 */
    DATABASE_ERROR(5002, "数据库错误"),

    /** Redis 操作异常 */
    REDIS_ERROR(5003, "Redis 操作异常"),

    /** 缓存序列化异常 */
    CACHE_SERIALIZE_ERROR(5004, "缓存序列化异常"),

    /** 执行日志异常 */
    OPERATION_LOG_ERROR(5005, "执行日志异常"),

    /** 加解密异常 */
    CRYPTO_ERROR(5006, "加解密错误"),

    // endregion

    // region 6xxx 外部依赖/工具类

    /** 工具异常 */
    TOOL_ERROR(6000, "工具异常"),

    /** JSON 解析错误 */
    JSON_PARSE_ERROR(6001, "JSON 解析错误"),

    /** 国际化异常 */
    I18N_ERROR(6002, "国际化异常"),

    /** IP 定位异常 */
    IP_LOCATION_ERROR(6003, "IP 定位异常"),

    /** 文件 IO 异常 */
    FILE_IO_ERROR(6004, "文件读写异常"),

    /** 邮件发送异常 */
    MAIL_SEND_ERROR(6005, "邮件发送异常"),

    /** 短信发送异常 */
    SMS_SEND_ERROR(6006, "短信发送异常"),

    /** 调用第三方服务失败 */
    THIRD_PARTY_ERROR(6007, "调用第三方服务失败"),

    /** 远程调用失败 */
    RPC_ERROR(6008, "远程调用失败");

    // endregion

    /** 码值到枚举的索引，类加载时构建并保证码值唯一 */
    private static final Map<Integer, BizCode> CODE_LOOKUP;

    static {
        BizCode[] values = values();
        Map<Integer, BizCode> lookup = HashMap.newHashMap(values.length);
        for (BizCode bizCode : values) {
            if (lookup.putIfAbsent(bizCode.code, bizCode) != null) {
                throw new IllegalStateException("重复的业务错误码: " + bizCode.code);
            }
        }
        CODE_LOOKUP = Map.copyOf(lookup);
    }

    /** 业务码 */
    private final int code;

    /** 默认错误消息 */
    private final String msg;

    BizCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 根据业务码获取对应的枚举，码值不存在时抛出异常
     *
     * @param code 业务码
     *
     * @return 对应的业务错误码枚举
     *
     * @throws IllegalArgumentException 业务码不存在时抛出
     */
    public static BizCode of(int code) {
        return find(code).orElseThrow(() -> new IllegalArgumentException("未知的业务错误码: " + code));
    }

    /**
     * 根据业务码查找对应的枚举，码值不存在时返回空
     *
     * @param code 业务码
     *
     * @return 对应的业务错误码枚举，不存在时为空
     */
    public static Optional<BizCode> find(int code) {
        return Optional.ofNullable(CODE_LOOKUP.get(code));
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
