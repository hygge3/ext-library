package ext.library.logback.mask;

import ext.library.desensitize.DesensitizeRule;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志脱敏规则
 * <p>
 * 定义「用哪个正则去日志里找敏感串」以及「找到后如何遮蔽」，
 * 遮蔽动作直接复用 {@link DesensitizeRule}，与 JSON 序列化时的脱敏口径保持一致。
 *
 * @param name     规则名称，仅用于日志排查
 * @param pattern  敏感串匹配模式
 * @param group    遮蔽的捕获组下标，0 表示整个匹配项
 * @param replacer 遮蔽函数，入参为 {@code group} 号捕获组
 */
public record LogMaskRule(String name, Pattern pattern, int group, Function<String, String> replacer) {

    /**
     * 用正则 + 脱敏策略构造规则，遮蔽整个匹配项
     *
     * @param name     规则名称
     * @param regex    敏感串匹配模式
     * @param strategy 脱敏策略
     * @return 脱敏规则
     */
    public static LogMaskRule of(String name, String regex, DesensitizeRule strategy) {
        return new LogMaskRule(name, Pattern.compile(regex), 0, strategy.desensitize());
    }

    /**
     * 用正则 + 自定义遮蔽函数构造规则，遮蔽整个匹配项
     *
     * @param name     规则名称
     * @param regex    敏感串匹配模式
     * @param replacer 遮蔽函数
     * @return 脱敏规则
     */
    public static LogMaskRule of(String name, String regex, Function<String, String> replacer) {
        return new LogMaskRule(name, Pattern.compile(regex), 0, replacer);
    }

    /**
     * 用正则 + 脱敏策略构造规则，只遮蔽指定捕获组
     * <p>
     * 适用于「键值对」形式：正则把键名和值拆成两组，只遮蔽值那一组，日志仍可读。
     *
     * @param name     规则名称
     * @param regex    敏感串匹配模式
     * @param group    遮蔽的捕获组下标
     * @param strategy 脱敏策略
     * @return 脱敏规则
     */
    public static LogMaskRule ofGroup(String name, String regex, int group, DesensitizeRule strategy) {
        return new LogMaskRule(name, Pattern.compile(regex), group, strategy.desensitize());
    }

    /**
     * 对整段文本执行脱敏
     *
     * @param text 原始文本
     * @return 脱敏后的文本
     */
    public String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder(text.length());
        int last = 0;
        while (matcher.find()) {
            sb.append(text, last, matcher.start());
            // group 下标越界时 group() 返回 null，退回整个匹配项
            String target = matcher.group(group);
            if (target == null) {
                sb.append(replacer.apply(matcher.group()));
            } else {
                // 保留捕获组之前的键名与分隔符，只遮蔽值
                sb.append(text, matcher.start(), matcher.start(group));
                sb.append(replacer.apply(target));
            }
            last = matcher.end();
        }
        return sb.append(text, last, text.length()).toString();
    }

}
