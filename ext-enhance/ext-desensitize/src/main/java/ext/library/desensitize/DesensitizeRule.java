package ext.library.desensitize;

import java.util.function.Function;

/**
 * 脱敏规则接口
 * <p>
 * 自定义脱敏规则可实现此接口，或直接在 {@link DesensitizeStrategy} 中添加枚举值
 *
 * <pre>{@code
 * public class CustomRule implements DesensitizeRule {
 *     @Override
 *     public Function<String, String> desensitize() {
 *         return value -> value.substring(0, 2) + "****";
 *     }
 * }
 * }</pre>
 */
@FunctionalInterface
public interface DesensitizeRule {

    /**
     * 获取脱敏函数
     *
     * @return 脱敏转换函数
     */
    Function<String, String> desensitize();
}
