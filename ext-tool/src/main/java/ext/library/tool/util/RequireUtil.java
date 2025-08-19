package ext.library.tool.util;

import lombok.experimental.UtilityClass;

/**
 * 断言获取工具
 *
 * @see org.springframework.util.Assert
 * @since 2025.08.15
 */
@UtilityClass
public class RequireUtil {

    /**
     * 断言对象不是 {@code null}。
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     *
     * @param object  要检查的对象
     * @param message 断言失败时要使用的异常消息
     *
     * @throws IllegalArgumentException 如果对象是 {@code null}
     */
    public <T> T requireNotNull(T object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    /**
     * 断言布尔表达式，如果表达式的计算结果为 {@code false}，则抛出 {@code IllegalArgumentException}。
     *
     * <pre class="code">AssertUtil.requireTrue(i > 0, "The value must be greater than zero");</pre>
     *
     * @param expression 布尔表达式
     * @param message    断言失败时要使用的异常消息
     *
     * @throws IllegalArgumentException 如果 {@code expression} 是 {@code false}
     */
    public boolean requireTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
        return true;
    }

}