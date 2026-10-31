package ext.library.json.serializer;


import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JacksonStdImpl;
import tools.jackson.databind.ser.jdk.NumberSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 超出 JS 最大最小值 处理
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
    private static final BigInteger MAX_SAFE_INTEGER = BigInteger.valueOf(9007199254740991L);
    private static final BigInteger MIN_SAFE_INTEGER = BigInteger.valueOf(-9007199254740991L);

    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator g, SerializationContext provider) throws JacksonException {
        // 在 JS 安全整数范围内，直接序列化为数字；超出范围则序列化为字符串
        if (inSafeRange(value)) {
            super.serialize(value, g, provider);
        } else {
            g.writeString(value.toString());
        }
    }

    /**
     * 判断是否在 JS 安全整数范围内
     * <p>
     * 不能用 {@code longValue()} 判：它会直接截断，{@code 2^64} 截成 0 就被当成安全值输出了裸数字，
     * 前端照样丢精度。
     *
     * @param value 数字
     *
     * @return 在安全范围内返回 {@code true}
     */
    private static boolean inSafeRange(Number value) {
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.compareTo(MIN_SAFE_INTEGER) >= 0 && bigInteger.compareTo(MAX_SAFE_INTEGER) <= 0;
        }
        if (value instanceof BigDecimal decimal) {
            // 小数没有精度问题，只有整数值才需要判范围
            return decimal.stripTrailingZeros().scale() > 0 || inSafeRange(decimal.toBigInteger());
        }
        long number = value.longValue();
        return number >= MIN_SAFE_INTEGER.longValue() && number <= MAX_SAFE_INTEGER.longValue();
    }

}