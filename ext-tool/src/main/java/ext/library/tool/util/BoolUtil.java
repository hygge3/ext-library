package ext.library.tool.util;

import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Boolean 工具类
 */
@UtilityClass
public class BoolUtil {

    private final List<String> STR_TRUE = List.of("1", "true", "yes", "ok", "y");

    private final List<String> STR_FALSE = List.of("0", "false", "no", "n");

    /**
     * 判断给定对象是否表示“真”值
     * 该方法通过检查不同类型的对象来确定它们是否代表一个“真”值
     * 对于字符串，它检查是否包含在预定义的表示真的字符串集合中
     * 对于数字，它检查值是否大于零
     * 对于布尔值，它检查是否为 true
     * 其他类型的对象默认返回 false
     *
     * @param obj 要检查的对象
     *
     * @return 如果对象表示“真”值，则返回 true；否则返回 false
     */
    public boolean isTrue(Object obj) {
        if (obj == null) {
            return false;
        }
        // 根据对象类型判断其是否表示“真”值
        return switch (obj) {
            // 如果是字符串，检查是否在表示真的字符串集合中
            case String str -> STR_TRUE.contains(str);
            // 如果是数字，检查其双精度值是否大于零
            case Number num -> num.doubleValue() > 0;
            // 如果是布尔值，检查是否为 true
            case Boolean bool -> bool;
            // 默认情况下返回 false
            default -> false;
        };
    }

    /**
     * 判断给定对象是否在某种意义上表示“假”或无效的状态
     * 此方法用于检查传递的对象是否符合特定条件，以决定其是否表示一个“假”或无效的状态
     * 它通过检查对象的类型和值来确定这一点
     *
     * @param obj 要进行检查的对象，可以是任何类型
     *
     * @return 如果对象表示“假”或无效状态，则返回 true；否则返回 false
     */
    public boolean isFalse(Object obj) {
        if (obj == null) {
            return false;
        }
        // 根据对象类型，判断其是否表示“假”或无效状态
        return switch (obj) {
            // 当对象为字符串时，检查其是否包含在预定义的表示“假”的字符串集合中
            case String str -> STR_FALSE.contains(str);
            // 当对象为数字时，检查其是否小于等于 0，因为这在许多上下文中表示失败或无效
            case Number num -> num.doubleValue() <= 0;
            // 当对象为布尔值时，直接检查其是否为 false
            case Boolean bool -> !bool;
            // 对于所有其他情况，默认返回 false，表示对象不表示“假”或无效状态
            default -> false;
        };
    }

}