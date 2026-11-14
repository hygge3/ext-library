package ext.library.logback.mask;

import ext.library.desensitize.DesensitizeStrategy;

import java.util.Map;

/**
 * 内置的日志脱敏规则集
 * <p>
 * 每条规则由「正则」与 {@link DesensitizeStrategy} 组成：正则在日志正文里定位敏感串，
 * 遮蔽函数直接取 JSON 脱敏那套策略，口径只有一份。
 * <p>
 * 正则均带前后向断言，避免把订单号等长数字串误伤成手机号或银行卡号。
 * 配置项 {@code ext.logback.mask.strategies} 用的就是这里的 key。
 */
public final class LogMaskRules {

    /**
     * 策略 -> 规则
     * <p>
     * 只有能在自由文本里被正则定位的策略才在表里；{@code PASSWORD} 在日志侧按
     * {@code password=xxx} 这类键值对形式匹配，而不是遮蔽整段文本。
     */
    private static final Map<DesensitizeStrategy, LogMaskRule> RULES = Map.of(
            // 手机号
            DesensitizeStrategy.PHONE,
            LogMaskRule.of("PHONE", "(?<!\\d)1[3-9]\\d{9}(?!\\d)", DesensitizeStrategy.PHONE),
            // 身份证号
            DesensitizeStrategy.ID_CARD,
            LogMaskRule.of("ID_CARD", "(?<![\\dXx])\\d{17}[\\dXx](?![\\dXx])", DesensitizeStrategy.ID_CARD),
            // 邮箱
            DesensitizeStrategy.EMAIL,
            LogMaskRule.of("EMAIL", "[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+", DesensitizeStrategy.EMAIL),
            // 银行卡号
            DesensitizeStrategy.BANK_CARD,
            LogMaskRule.of("BANK_CARD", "(?<!\\d)\\d{16,19}(?!\\d)", DesensitizeStrategy.BANK_CARD),
            // IPv4 地址
            DesensitizeStrategy.IP,
            LogMaskRule.of("IP", "(?<!\\d)(?:\\d{1,3}\\.){3}\\d{1,3}(?!\\d)", DesensitizeStrategy.IP),
            // 键值对形式的凭据：password=xxx / "token": "xxx"，只遮蔽值、保留键名
            DesensitizeStrategy.PASSWORD,
            LogMaskRule.ofGroup(
                    "PASSWORD",
                    // 键名两侧可选引号，以兼容 JSON；字符类里用 \\s 才是正则空白
                    "(?i)([\"']?(?:password|passwd|pwd|secret|token|accessToken|refreshToken)[\"']?\\s*[:=]\\s*)([\"']?[^\\s,;\\]}\"']+[\"']?)",
                    2,
                    DesensitizeStrategy.PASSWORD)
    );

    private LogMaskRules() {
    }

    /**
     * 取策略对应的日志脱敏规则
     *
     * @param strategy 脱敏策略
     * @return 规则，该策略无法在自由文本中定位时返回 null
     */
    public static LogMaskRule of(DesensitizeStrategy strategy) {
        return RULES.get(strategy);
    }

}
