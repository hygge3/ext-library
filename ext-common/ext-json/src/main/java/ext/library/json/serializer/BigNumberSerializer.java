package ext.library.json.serializer;


import java.math.BigDecimal;
import java.math.BigInteger;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JacksonStdImpl;
import tools.jackson.databind.ser.jdk.NumberSerializer;

/**
 * 超出 JS 安全整数范围的处理
 * <p>
 * 超出 {@code Number.MAX_SAFE_INTEGER} / {@code Number.MIN_SAFE_INTEGER} 的整数序列化为字符串，
 * 防止前端解析时精度丢失。边界值本身 JS 可精确表示，序列化为数字。
 */
@JacksonStdImpl
public class BigNumberSerializer extends NumberSerializer {

    /**
     * 提供实例
     */
    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

    /**
     * 根据 JS Number.MAX_SAFE_INTEGER 与 Number.MIN_SAFE_INTEGER 得来
     */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator g, SerializationContext provider) throws JacksonException {
        // Jackson 对 null 走 serializeNull()，但保持防御：isSafe 会直接解引用 value
        if (value == null) {
            g.writeNull();
            return;
        }
        if (isSafe(value)) {
            super.serialize(value, g, provider);
        } else {
            g.writeString(value.toString());
        }
    }

    /**
     * 判断整数是否落在 JS 安全整数范围内
     * <p>
     * 超出 long 范围的大整数（如 BigInteger）无法用 {@code longValue()} 精确表示，
     * 这里用 {@code longValueExact()} 判断，超出 long 范围直接判定为不安全。
     */
    private static boolean isSafe(Number value) {
        final long longValue;
        try {
            if (value instanceof BigDecimal decimal) {
                longValue = decimal.longValueExact();
            } else if (value instanceof BigInteger integer) {
                longValue = integer.longValueExact();
            } else {
                longValue = value.longValue();
            }
        } catch (ArithmeticException e) {
            // 超出 long 范围，必然超出 JS 安全整数范围
            return false;
        }
        return longValue >= MIN_SAFE_INTEGER && longValue <= MAX_SAFE_INTEGER;
    }

}
