package ext.library.logback.config;

import ext.library.logback.mask.LogMaskRule;
import ext.library.logback.mask.LogMaskRules;

import java.util.ArrayList;
import java.util.List;

/**
 * 按配置解析脱敏规则集
 * <p>
 * 拆出来单独放，是为了让 {@link LogMaskAutoConfiguration} 只负责触发时机。
 */
final class LogMaskRuleSets {

    private LogMaskRuleSets() {
    }

    /**
     * 按配置组装规则集
     *
     * @param properties 脱敏配置
     * @return 规则集
     */
    static List<LogMaskRule> resolve(LogMaskProperties properties) {
        List<LogMaskRule> rules = new ArrayList<>();
        if (properties.isPhone()) {
            rules.add(LogMaskRules.PHONE);
        }
        if (properties.isIdCard()) {
            rules.add(LogMaskRules.ID_CARD);
        }
        if (properties.isEmail()) {
            rules.add(LogMaskRules.EMAIL);
        }
        if (properties.isBankCard()) {
            rules.add(LogMaskRules.BANK_CARD);
        }
        if (properties.isIp()) {
            rules.add(LogMaskRules.IP);
        }
        if (properties.isCredential()) {
            rules.add(LogMaskRules.CREDENTIAL);
        }
        return List.copyOf(rules);
    }

}
