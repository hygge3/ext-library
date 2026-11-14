package ext.library.logback.config;

import ext.library.desensitize.DesensitizeStrategy;
import ext.library.logback.mask.LogMaskRule;
import ext.library.logback.mask.LogMaskRules;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

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
     * 按配置的策略列表组装规则集，无法在日志里定位的策略跳过并告警
     *
     * @param properties 脱敏配置
     * @return 规则集
     */
    static List<LogMaskRule> resolve(LogMaskProperties properties) {
        List<LogMaskRule> rules = new ArrayList<>();
        for (DesensitizeStrategy strategy : properties.getStrategies()) {
            LogMaskRule rule = LogMaskRules.of(strategy);
            if (rule == null) {
                Logs.warn(EmojiSymbol.LOGBACK, "策略 {} 无法在日志正文中定位，已跳过日志脱敏", strategy);
                continue;
            }
            rules.add(rule);
        }
        return List.copyOf(rules);
    }

}
