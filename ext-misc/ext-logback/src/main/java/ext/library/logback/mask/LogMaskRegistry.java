package ext.library.logback.mask;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 日志脱敏规则注册表
 * <p>
 * logback 的 converter 由 XML 实例化，拿不到 Spring 容器，
 * 因此用一个静态注册表在两者之间传递规则：
 * {@link ext.library.logback.config.LogMaskAutoConfiguration} 启动时写入规则，
 * {@link LogMaskConverter} 转换日志时读取规则。
 * <p>
 * 规则表为空时 converter 直接放行，等价于未启用脱敏。
 */
public final class LogMaskRegistry {

    /** 已启用的脱敏规则 */
    private static final List<LogMaskRule> RULES = new CopyOnWriteArrayList<>();

    private LogMaskRegistry() {
    }

    /**
     * 覆盖式写入规则集
     *
     * @param rules 规则集，传空表示关闭脱敏
     */
    public static void setRules(List<LogMaskRule> rules) {
        RULES.clear();
        if (rules != null && !rules.isEmpty()) {
            RULES.addAll(rules);
        }
    }

    /**
     * @return 当前生效的规则集
     */
    public static List<LogMaskRule> getRules() {
        return List.copyOf(RULES);
    }

    /**
     * 清空规则，恢复为不脱敏
     */
    public static void clear() {
        RULES.clear();
    }

}
