package ext.library.logback.mask;

import ext.library.desensitize.DesensitizeStrategy;

import java.util.List;

/**
 * 内置的日志脱敏规则集
 * <p>
 * 覆盖国内业务最常见的敏感数据类型，正则均带前后向断言，避免把订单号等长数字串
 * 误伤成手机号或银行卡号。
 */
public final class LogMaskRules {

    /** 手机号 */
    public static final LogMaskRule PHONE =
            LogMaskRule.of("PHONE", "(?<!\\d)1[3-9]\\d{9}(?!\\d)", DesensitizeStrategy.PHONE);

    /** 身份证号 */
    public static final LogMaskRule ID_CARD =
            LogMaskRule.of("ID_CARD", "(?<![\\dXx])\\d{17}[\\dXx](?![\\dXx])", DesensitizeStrategy.ID_CARD);

    /** 邮箱 */
    public static final LogMaskRule EMAIL =
            LogMaskRule.of("EMAIL", "[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+", DesensitizeStrategy.EMAIL);

    /** 银行卡号 */
    public static final LogMaskRule BANK_CARD =
            LogMaskRule.of("BANK_CARD", "(?<!\\d)\\d{16,19}(?!\\d)", DesensitizeStrategy.BANK_CARD);

    /** IPv4 地址 */
    public static final LogMaskRule IP =
            LogMaskRule.of("IP", "(?<!\\d)(?:\\d{1,3}\\.){3}\\d{1,3}(?!\\d)", DesensitizeStrategy.IP);

    /** 键值对形式的凭据：{@code password=xxx} / {@code "token": "xxx"}，只遮蔽值、保留键名 */
    public static final LogMaskRule CREDENTIAL = LogMaskRule.ofGroup(
            "CREDENTIAL",
            "(?i)((?:password|passwd|pwd|secret|token|accessToken|refreshToken)\\s*[:=]\\s*)(\"?'?[^\s,;\\]\\}&\"']+[\"']?)",
            2,
            DesensitizeStrategy.PASSWORD
    );

    private LogMaskRules() {
    }

    /**
     * @return 默认开启的规则集
     */
    public static List<LogMaskRule> defaults() {
        return List.of(BANK_CARD, ID_CARD, PHONE, EMAIL, IP, CREDENTIAL);
    }

}
