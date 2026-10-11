package ext.library.desensitize;

import java.util.function.Function;

/**
 * 空操作脱敏规则
 * <p>
 * 不进行任何脱敏处理，原样返回输入值。
 * 作为 {@link Sensitive#customRule()} 的默认值使用。
 */
public final class NoOpDesensitizeRule implements DesensitizeRule {

    @Override
    public Function<String, String> desensitize() {
        return Function.identity();
    }
}
