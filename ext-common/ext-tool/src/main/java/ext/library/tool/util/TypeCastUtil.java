package ext.library.tool.util;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.function.Function;

/**
 * 简单的数据类型转换工具类
 */
public final class TypeCastUtil {

    private TypeCastUtil() {
    }

    // region String

    /**
     * 以 null 安全的方式从 Object 获取 String
     * <p>
     * 通过 {@code toString()} 方法获取字符串
     *
     * @param obj 源对象
     *
     * @return 对象的字符串值，输入为 null 时返回 {@code null}
     */
    public static String getAsString(@Nullable Object obj) {
        return obj == null ? null : obj.toString();
    }

    /**
     * 以 null 安全的方式从 Object 获取 String（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的字符串值，输入为 null 时返回 {@code defaultValue}
     */
    public static String getAsString(@Nullable Object obj, String defaultValue) {
        return obj == null ? defaultValue : obj.toString();
    }

    // endregion

    // region Number

    /**
     * 以 null 安全的方式从 Object 获取 Number
     * <p>
     * 如果值为 {@code Number} 则直接返回；如果值为 {@code String} 则通过
     * {@link NumberFormat#parse(String)} 转换，转换失败时抛出异常；
     * 如果值为 {@code Boolean} 则 true 返回 1，false 返回 0；
     * 其他类型抛出 {@link UnsupportedOperationException}
     *
     * @param obj 源对象
     *
     * @return 对象的 Number 值，输入为 null 时返回 {@code null}
     */
    public static Number getAsNumber(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return switch (obj) {
            case Number number -> number;
            case Boolean bool -> bool ? 1 : 0;
            case String s -> {
                try {
                    yield NumberFormat.getInstance().parse(s);
                } catch (ParseException e) {
                    throw new NumberFormatException(obj + " 不是有效的数字格式");
                }
            }
            default -> throw new UnsupportedOperationException("不支持的类型: " + obj.getClass());
        };
    }

    /**
     * 以 null 安全的方式从 Object 获取 Number（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 Number 值，输入为 null 或转换失败时返回 {@code defaultValue}
     */
    @SuppressWarnings("unchecked")
    public static <R extends Number> R getAsNumber(@Nullable Object obj, R defaultValue) {
        return (R) getWithDefault(TypeCastUtil::getAsNumber, obj, defaultValue);
    }

    // endregion

    // region Boolean

    /**
     * 以 null 安全的方式从 Object 获取 Boolean
     * <p>
     * 如果值为 {@code Boolean} 则直接返回；如果值为 {@code String} 则忽略大小写判断是否等于 "true"；
     * 如果值为 {@code Number} 则整数值为 0 返回 false，非 0 返回 true；
     * 其他类型抛出 {@link UnsupportedOperationException}
     *
     * @param obj 源对象
     *
     * @return 对象的 Boolean 值，输入为 null 时返回 {@code null}
     */
    public static Boolean getAsBoolean(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return switch (obj) {
            case Boolean bool -> bool;
            case String s -> Boolean.valueOf(s);
            case Number n -> n.intValue() != 0;
            default -> throw new UnsupportedOperationException("不支持的类型: " + obj.getClass());
        };
    }

    /**
     * 以 null 安全的方式从 Object 获取 Boolean（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 Boolean 值，输入为 null 时返回 {@code defaultValue}
     */
    public static Boolean getAsBoolean(@Nullable Object obj, Boolean defaultValue) {
        return getWithDefault(TypeCastUtil::getAsBoolean, obj, defaultValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 boolean 基本类型
     *
     * @param obj 源对象
     *
     * @return 对象的 boolean 值，输入为 null 时返回 {@code false}
     */
    public static boolean getAsBooleanValue(@Nullable Object obj) {
        return getAsBoolean(obj, false);
    }

    /**
     * 以 null 安全的方式从 Object 获取 boolean 基本类型（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 boolean 值，输入为 null 时返回 {@code defaultValue}
     */
    public static boolean getAsBooleanValue(@Nullable Object obj, boolean defaultValue) {
        return getAsBoolean(obj, defaultValue);
    }

    // endregion

    // region Byte

    /**
     * 以 null 安全的方式从 Object 获取 Byte
     *
     * @param obj 源对象
     *
     * @return 对象的 Byte 值，输入为 null 时返回 {@code null}
     */
    public static Byte getAsByte(@Nullable Object obj) {
        return fromNumber(obj, Byte.class, Number::byteValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 Byte（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 Byte 值，输入为 null 时返回 {@code defaultValue}
     */
    public static Byte getAsByte(@Nullable Object obj, Byte defaultValue) {
        return getWithDefault(TypeCastUtil::getAsByte, obj, defaultValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 byte 基本类型
     *
     * @param obj 源对象
     *
     * @return 对象的 byte 值，输入为 null 时返回 {@code 0}
     */
    public static byte getAsByteValue(@Nullable Object obj) {
        return getAsByte(obj, (byte) 0);
    }

    /**
     * 以 null 安全的方式从 Object 获取 byte 基本类型（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 byte 值，输入为 null 时返回 {@code defaultValue}
     */
    public static byte getAsByteValue(@Nullable Object obj, byte defaultValue) {
        return getAsByte(obj, defaultValue);
    }

    // endregion

    // region Short

    /**
     * 以 null 安全的方式从 Object 获取 Short
     *
     * @param obj 源对象
     *
     * @return 对象的 Short 值，输入为 null 时返回 {@code null}
     */
    public static Short getAsShort(@Nullable Object obj) {
        return fromNumber(obj, Short.class, Number::shortValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 Short（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 Short 值，输入为 null 时返回 {@code defaultValue}
     */
    public static Short getAsShort(@Nullable Object obj, Short defaultValue) {
        return getWithDefault(TypeCastUtil::getAsShort, obj, defaultValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 short 基本类型
     *
     * @param obj 源对象
     *
     * @return 对象的 short 值，输入为 null 时返回 {@code 0}
     */
    public static short getAsShortValue(@Nullable Object obj) {
        return getAsShort(obj, (short) 0);
    }

    /**
     * 以 null 安全的方式从 Object 获取 short 基本类型（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 short 值，输入为 null 时返回 {@code defaultValue}
     */
    public static short getAsShortValue(@Nullable Object obj, short defaultValue) {
        return getAsShort(obj, defaultValue);
    }

    // endregion

    // region Integer

    /**
     * 以 null 安全的方式从 Object 获取 Integer
     *
     * @param obj 源对象
     *
     * @return 对象的 Integer 值，输入为 null 时返回 {@code null}
     */
    public static Integer getAsInteger(@Nullable Object obj) {
        return fromNumber(obj, Integer.class, Number::intValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 Integer（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 Integer 值，输入为 null 时返回 {@code defaultValue}
     */
    public static Integer getAsInteger(@Nullable Object obj, Integer defaultValue) {
        return getWithDefault(TypeCastUtil::getAsInteger, obj, defaultValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 int 基本类型
     *
     * @param obj 源对象
     *
     * @return 对象的 int 值，输入为 null 时返回 {@code 0}
     */
    public static int getAsIntValue(@Nullable Object obj) {
        return getAsInteger(obj, 0);
    }

    /**
     * 以 null 安全的方式从 Object 获取 int 基本类型（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 int 值，输入为 null 时返回 {@code defaultValue}
     */
    public static int getAsIntValue(@Nullable Object obj, int defaultValue) {
        return getAsInteger(obj, defaultValue);
    }

    // endregion

    // region Long

    /**
     * 以 null 安全的方式从 Object 获取 Long
     *
     * @param obj 源对象
     *
     * @return 对象的 Long 值，输入为 null 时返回 {@code null}
     */
    public static Long getAsLong(@Nullable Object obj) {
        return fromNumber(obj, Long.class, Number::longValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 Long（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 Long 值，输入为 null 时返回 {@code defaultValue}
     */
    public static Long getAsLong(@Nullable Object obj, Long defaultValue) {
        return getWithDefault(TypeCastUtil::getAsLong, obj, defaultValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 long 基本类型
     *
     * @param obj 源对象
     *
     * @return 对象的 long 值，输入为 null 时返回 {@code 0L}
     */
    public static long getAsLongValue(@Nullable Object obj) {
        return getAsLong(obj, 0L);
    }

    /**
     * 以 null 安全的方式从 Object 获取 long 基本类型（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 long 值，输入为 null 时返回 {@code defaultValue}
     */
    public static long getAsLongValue(@Nullable Object obj, long defaultValue) {
        return getAsLong(obj, defaultValue);
    }

    // endregion

    // region Float

    /**
     * 以 null 安全的方式从 Object 获取 Float
     *
     * @param obj 源对象
     *
     * @return 对象的 Float 值，输入为 null 时返回 {@code null}
     */
    public static Float getAsFloat(@Nullable Object obj) {
        return fromNumber(obj, Float.class, Number::floatValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 Float（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 Float 值，输入为 null 时返回 {@code defaultValue}
     */
    public static Float getAsFloat(@Nullable Object obj, Float defaultValue) {
        return getWithDefault(TypeCastUtil::getAsFloat, obj, defaultValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 float 基本类型
     *
     * @param obj 源对象
     *
     * @return 对象的 float 值，输入为 null 时返回 {@code 0.0F}
     */
    public static float getAsFloatValue(@Nullable Object obj) {
        return getAsFloat(obj, 0F);
    }

    /**
     * 以 null 安全的方式从 Object 获取 float 基本类型（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 float 值，输入为 null 时返回 {@code defaultValue}
     */
    public static float getAsFloatValue(@Nullable Object obj, float defaultValue) {
        return getAsFloat(obj, defaultValue);
    }

    // endregion

    // region Double

    /**
     * 以 null 安全的方式从 Object 获取 Double
     *
     * @param obj 源对象
     *
     * @return 对象的 Double 值，输入为 null 时返回 {@code null}
     */
    public static Double getAsDouble(@Nullable Object obj) {
        return fromNumber(obj, Double.class, Number::doubleValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 Double（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 Double 值，输入为 null 时返回 {@code defaultValue}
     */
    public static Double getAsDouble(@Nullable Object obj, Double defaultValue) {
        return getWithDefault(TypeCastUtil::getAsDouble, obj, defaultValue);
    }

    /**
     * 以 null 安全的方式从 Object 获取 double 基本类型
     *
     * @param obj 源对象
     *
     * @return 对象的 double 值，输入为 null 时返回 {@code 0.0}
     */
    public static double getAsDoubleValue(@Nullable Object obj) {
        return getAsDouble(obj, 0D);
    }

    /**
     * 以 null 安全的方式从 Object 获取 double 基本类型（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 double 值，输入为 null 时返回 {@code defaultValue}
     */
    public static double getAsDoubleValue(@Nullable Object obj, double defaultValue) {
        return getAsDouble(obj, defaultValue);
    }

    // endregion

    // region BigInteger

    /**
     * 以 null 安全的方式从 Object 获取 BigInteger
     *
     * @param obj 源对象
     *
     * @return 对象的 BigInteger 值，输入为 null 时返回 {@code null}
     */
    public static BigInteger getAsBigInteger(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return switch (obj) {
            case BigInteger bi -> bi;
            case String s -> new BigInteger(s);
            case Number n -> BigInteger.valueOf(n.longValue());
            case Boolean b -> BigInteger.valueOf(b ? 1L : 0L);
            default -> throw new UnsupportedOperationException("不支持的类型: " + obj.getClass());
        };
    }

    /**
     * 以 null 安全的方式从 Object 获取 BigInteger（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 BigInteger 值，输入为 null 时返回 {@code defaultValue}
     */
    @SuppressWarnings("unchecked")
    public static <R extends BigInteger> R getAsBigInteger(@Nullable Object obj, R defaultValue) {
        return (R) getWithDefault(TypeCastUtil::getAsBigInteger, obj, defaultValue);
    }

    // endregion

    // region BigDecimal

    /**
     * 以 null 安全的方式从 Object 获取 BigDecimal
     *
     * @param obj 源对象
     *
     * @return 对象的 BigDecimal 值，输入为 null 时返回 {@code null}
     */
    public static BigDecimal getAsBigDecimal(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return switch (obj) {
            case BigDecimal bd -> bd;
            case String s -> new BigDecimal(s);
            case Number n -> new BigDecimal(n.toString());
            case Boolean b -> BigDecimal.valueOf(b ? 1L : 0L);
            default -> throw new UnsupportedOperationException("不支持的类型: " + obj.getClass());
        };
    }

    /**
     * 以 null 安全的方式从 Object 获取 BigDecimal（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 对象的 BigDecimal 值，输入为 null 时返回 {@code defaultValue}
     */
    @SuppressWarnings("unchecked")
    public static <R extends BigDecimal> R getAsBigDecimal(@Nullable Object obj, R defaultValue) {
        return (R) getWithDefault(TypeCastUtil::getAsBigDecimal, obj, defaultValue);
    }

    // endregion

    // region cast

    /**
     * 数据类型转换
     *
     * @param obj 源对象
     * @param clz 目标类型
     *
     * @return 转换后的值，如果 obj 为 null 则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <R> R cast(@Nullable Object obj, Class<R> clz) {
        if (obj == null) {
            return null;
        }
        if (clz.isAssignableFrom(obj.getClass())) {
            return (R) obj;
        }
        if (Boolean.class.equals(clz) || boolean.class.equals(clz)) {
            return (R) getAsBoolean(obj);
        } else if (Byte.class.equals(clz) || byte.class.equals(clz)) {
            return (R) getAsByte(obj);
        } else if (Short.class.equals(clz) || short.class.equals(clz)) {
            return (R) getAsShort(obj);
        } else if (Integer.class.equals(clz) || int.class.equals(clz)) {
            return (R) getAsInteger(obj);
        } else if (Long.class.equals(clz) || long.class.equals(clz)) {
            return (R) getAsLong(obj);
        } else if (Float.class.equals(clz) || float.class.equals(clz)) {
            return (R) getAsFloat(obj);
        } else if (Double.class.equals(clz) || double.class.equals(clz)) {
            return (R) getAsDouble(obj);
        } else if (String.class.equals(clz)) {
            return (R) getAsString(obj);
        } else if (BigInteger.class.isAssignableFrom(clz)) {
            return (R) getAsBigInteger(obj);
        } else if (BigDecimal.class.isAssignableFrom(clz)) {
            return (R) getAsBigDecimal(obj);
        } else if (Number.class.isAssignableFrom(clz)) {
            return (R) getAsNumber(obj);
        }
        throw new UnsupportedOperationException("不支持的目标类型: " + clz);
    }

    /**
     * 数据类型转换（带默认值）
     *
     * @param obj          源对象
     * @param defaultValue 值为 null 或转换失败时的默认值
     *
     * @return 转换后的值，如果转换失败则返回 defaultValue
     */
    @SuppressWarnings("unchecked")
    public static <R> R cast(@Nullable Object obj, R defaultValue) {
        if (defaultValue == null) {
            throw new IllegalArgumentException("defaultValue 不能为 null");
        }
        try {
            R result = (R) cast(obj, defaultValue.getClass());
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // endregion

    // region 辅助方法

    /**
     * 通用的带默认值转换：转换失败或结果为 null 时返回默认值
     */
    private static <T> T getWithDefault(Function<Object, T> converter, @Nullable Object obj, T defaultValue) {
        try {
            T result = converter.apply(obj);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 通用的数值类型转换：从 {@link #getAsNumber(Object)} 结果中提取目标类型
     */
    private static <T extends Number> T fromNumber(@Nullable Object obj, Class<T> type, Function<Number, T> converter) {
        Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        }
        return type.isInstance(answer) ? type.cast(answer) : converter.apply(answer);
    }

    // endregion

}
