package ext.library.tool.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 简单的数据类型转换工具类
 */
public final class TypeCastUtil {

    private TypeCastUtil() {
    }

    /**
     * Gets a String from an Object in a null-safe manner.
     * <p>
     * The String is obtained via <code>toString</code>.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a String, <code>null</code> if null object input
     */
    public static String getAsString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    /**
     * Gets a String from an Object in a null-safe manner.
     * <p>
     * The String is obtained via <code>toString</code>.
     *
     * @param obj          the object to use
     * @param defaultValue what to return if the value is null or if the conversion fails
     *
     * @return the value of the Object as a String, <code>defaultValue</code> if null
     * object input
     */
    public static String getAsString(Object obj, String defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        return obj.toString();
    }

    /**
     * Gets a Number from an Object in a null-safe manner.
     * <p>
     * If the value is a <code>Number</code> it is returned directly. If the value is a
     * <code>String</code> it is converted using {@link NumberFormat#parse(String)} on the
     * system default formatter returning <code>null</code> if the conversion fails.
     * Otherwise, <code>null</code> is returned.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a Number, <code>null</code> if null object input
     */
    public static Number getAsNumber(Object obj) {
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
     * Converting the Object into a number, using the default value if the conversion
     * fails.
     *
     * @param obj          the object to use
     * @param defaultValue what to return if the value is null or if the conversion fails
     *
     * @return the value of the object as a number, or defaultValue if the original value
     * is null, the object is null or the number conversion fails
     */
    @SuppressWarnings("unchecked")
    public static <R extends Number> R getAsNumber(Object obj, R defaultValue) {
        try {
            Number answer = getAsNumber(obj);
            return answer != null ? (R) answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a Boolean from an Object in a null-safe manner.
     * <p>
     * If the value is a <code>Boolean</code> it is returned directly. If the value is a
     * <code>String</code> and it equals 'true' ignoring case then <code>true</code> is
     * returned, otherwise <code>false</code>. If the value is a <code>Number</code> an
     * integer zero value returns <code>false</code> and non-zero returns
     * <code>true</code>. Otherwise, <code>null</code> is returned.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a Boolean, <code>null</code> if null object
     * input
     */
    public static Boolean getAsBoolean(Object obj) {
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
     * Gets a Boolean from an Object in a null-safe manner.
     * <p>
     * If the value is a <code>Boolean</code> it is returned directly. If the value is a
     * <code>String</code> and it equals 'true' ignoring case then <code>true</code> is
     * returned, otherwise <code>false</code>. If the value is a <code>Number</code> an
     * integer zero value returns <code>false</code> and non-zero returns
     * <code>true</code>. Otherwise, <code>null</code> is returned.
     *
     * @param obj          the object to use
     * @param defaultValue what to return if the value is null or if the conversion fails
     *
     * @return the value of the Object as a Boolean, <code>defaultValue</code> if null
     * object input
     */
    public static Boolean getAsBoolean(Object obj, Boolean defaultValue) {
        try {
            Boolean answer = getAsBoolean(obj);
            return answer != null ? answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a boolean from an Object in a null-safe manner.
     * <p>
     * If the value is a <code>Boolean</code> its value is returned. If the value is a
     * <code>String</code> and it equals 'true' ignoring case then <code>true</code> is
     * returned, otherwise <code>false</code>. If the value is a <code>Number</code> an
     * integer zero value returns <code>false</code> and non-zero returns
     * <code>true</code>. Otherwise, <code>false</code> is returned.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a Boolean, <code>false</code> if null object
     * input
     */
    public static boolean getAsBooleanValue(Object obj) {
        return getAsBoolean(obj, false);
    }

    /**
     * Gets a boolean from an Object in a null-safe manner.
     * <p>
     * If the value is a <code>Boolean</code> its value is returned. If the value is a
     * <code>String</code> and it equals 'true' ignoring case then <code>true</code> is
     * returned, otherwise <code>false</code>. If the value is a <code>Number</code> an
     * integer zero value returns <code>false</code> and non-zero returns
     * <code>true</code>. Otherwise, <code>false</code> is returned.
     *
     * @param obj          the object to use
     * @param defaultValue what to return if the value is null or if the conversion fails
     *
     * @return the value in the Map as a Boolean, <code>defaultValue</code> if null object
     * input
     */
    public static boolean getAsBooleanValue(Object obj, boolean defaultValue) {
        return getAsBoolean(obj, defaultValue);
    }

    /**
     * Gets a Byte from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The Byte is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of Object as a Byte, <code>null</code> if null object input
     */
    public static Byte getAsByte(Object obj) {
        Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        }
        return answer instanceof Byte b ? b : answer.byteValue();
    }

    /**
     * Gets a Byte from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The Byte is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a Byte, <code>defaultValue</code> if null object
     * input
     */
    public static Byte getAsByte(Object obj, Byte defaultValue) {
        try {
            Byte answer = getAsByte(obj);
            return answer != null ? answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a byte from an Object in a null-safe manner.
     * <p>
     * The byte is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a byte, <code>0</code> if null object input
     */
    public static byte getAsByteValue(Object obj) {
        return getAsByte(obj, (byte) 0);
    }

    /**
     * Gets a byte from an Object in a null-safe manner.
     * <p>
     * The byte is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of the Object as a byte, <code>defaultValue</code> if null object
     * input
     */
    public static byte getAsByteValue(Object obj, byte defaultValue) {
        return getAsByte(obj, defaultValue);
    }

    /**
     * Gets a Short from an Object in a null-safe manner.
     * <p>
     * The Short is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a Short, <code>null</code> if null object input
     */
    public static Short getAsShort(Object obj) {
        Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        }
        return answer instanceof Short s ? s : answer.shortValue();
    }

    /**
     * Gets a Short from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The Short is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a Short, <code>defaultValue</code> if null object
     * input
     */
    public static Short getAsShort(Object obj, Short defaultValue) {
        try {
            Short answer = getAsShort(obj);
            return answer != null ? answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a short from an Object in a null-safe manner.
     * <p>
     * The short is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a short, <code>0</code> if null object input
     */
    public static short getAsShortValue(Object obj) {
        return getAsShort(obj, (short) 0);
    }

    /**
     * Gets a short from an Object in a null-safe manner.
     * <p>
     * The short is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of the Object as a short, <code>defaultValue</code> if null
     * object input
     */
    public static short getAsShortValue(Object obj, short defaultValue) {
        return getAsShort(obj, defaultValue);
    }

    /**
     * Gets an Integer from an Object in a null-safe manner.
     * <p>
     * The Integer is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as an Integer, <code>null</code> if null object
     * input
     */
    public static Integer getAsInteger(Object obj) {
        Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        }
        return answer instanceof Integer i ? i : answer.intValue();
    }

    /**
     * Gets an Integer from an Object in a null-safe manner, using the default value if
     * the conversion fails.
     * <p>
     * The Integer is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as an Integer, <code>defaultValue</code> if null object
     * input
     */
    public static Integer getAsInteger(Object obj, Integer defaultValue) {
        try {
            Integer answer = getAsInteger(obj);
            return answer != null ? answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets an int from an Object in a null-safe manner.
     * <p>
     * The int is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as an int, <code>0</code> if null object input
     */
    public static int getAsIntValue(Object obj) {
        return getAsInteger(obj, 0);
    }

    /**
     * Gets an int from an Object in a null-safe manner.
     * <p>
     * The int is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of the Object as an int, <code>defaultValue</code> if null object
     * input
     */
    public static int getAsIntValue(Object obj, int defaultValue) {
        return getAsInteger(obj, defaultValue);
    }

    /**
     * Gets a Long from an Object in a null-safe manner.
     * <p>
     * The Long is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a Long, <code>null</code> if null object input
     */
    public static Long getAsLong(Object obj) {
        Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        }
        return answer instanceof Long l ? l : answer.longValue();
    }

    /**
     * Gets a Long from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The Long is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a Long, <code>defaultValue</code> if null object
     * input
     */
    public static Long getAsLong(Object obj, Long defaultValue) {
        try {
            Long answer = getAsLong(obj);
            return answer != null ? answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a long from an Object in a null-safe manner.
     * <p>
     * The long is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a long, <code>0L</code> if null object input
     */
    public static long getAsLongValue(Object obj) {
        return getAsLong(obj, 0L);
    }

    /**
     * Gets a long from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The long is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a long, <code>defaultValue</code> if null object
     * input
     */
    public static long getAsLongValue(Object obj, long defaultValue) {
        return getAsLong(obj, defaultValue);
    }

    /**
     * Gets a Float from an Object in a null-safe manner.
     * <p>
     * The Float is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a Float, <code>null</code> if null object input
     */
    public static Float getAsFloat(Object obj) {
        Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        }
        return answer instanceof Float f ? f : answer.floatValue();
    }

    /**
     * Gets a Float from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The Float is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a Float, <code>defaultValue</code> if null object
     * input
     */
    public static Float getAsFloat(Object obj, Float defaultValue) {
        try {
            Float answer = getAsFloat(obj);
            return answer != null ? answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a float from an Object in a null-safe manner.
     * <p>
     * The float is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of Object as a float, <code>0.0F</code> if null object input
     */
    public static float getAsFloatValue(Object obj) {
        return getAsFloat(obj, 0F);
    }

    /**
     * Gets a float from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The float is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a float, <code>defaultValue</code> if null object
     * input
     */
    public static float getAsFloatValue(Object obj, float defaultValue) {
        return getAsFloat(obj, defaultValue);
    }

    /**
     * Gets a Double from an Object in a null-safe manner.
     * <p>
     * The Double is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a Double, <code>null</code> if null object input
     */
    public static Double getAsDouble(Object obj) {
        Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        }
        return answer instanceof Double d ? d : answer.doubleValue();
    }

    /**
     * Gets a Double from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The Double is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a Double, <code>defaultValue</code> if null object
     * input
     */
    public static Double getAsDouble(Object obj, Double defaultValue) {
        try {
            Double answer = getAsDouble(obj);
            return answer != null ? answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a double from an Object in a null-safe manner.
     * <p>
     * The double is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of Object as a double, <code>0.0</code> if null object input
     */
    public static double getAsDoubleValue(Object obj) {
        return getAsDouble(obj, 0D);
    }

    /**
     * Gets a double from an Object in a null-safe manner, using the default value if the
     * conversion fails.
     * <p>
     * The double is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a double, <code>defaultValue</code> if null object
     * input
     */
    public static double getAsDoubleValue(Object obj, double defaultValue) {
        return getAsDouble(obj, defaultValue);
    }

    /**
     * Gets a BigInteger from an Object in a null-safe manner.
     * <p>
     * The BigInteger is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of Object as a BigInteger, <code>null</code> if null object input
     */
    public static BigInteger getAsBigInteger(Object obj) {
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
     * Gets a BigInteger from an Object in a null-safe manner, using the default value if
     * the conversion fails.
     * <p>
     * The BigInteger is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a BigInteger, <code>defaultValue</code> if null
     * object input
     */
    @SuppressWarnings("unchecked")
    public static <R extends BigInteger> R getAsBigInteger(Object obj, R defaultValue) {
        try {
            BigInteger answer = getAsBigInteger(obj);
            return answer != null ? (R) answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a BigDecimal from an Object in a null-safe manner.
     * <p>
     * The BigDecimal is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of Object as a BigDecimal, <code>null</code> if null object input
     */
    public static BigDecimal getAsBigDecimal(Object obj) {
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
     * Gets a BigDecimal from an Object in a null-safe manner, using the default value if
     * the conversion fails.
     * <p>
     * The BigDecimal is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return the value of Object as a BigDecimal, <code>defaultValue</code> if null
     * object input
     */
    @SuppressWarnings("unchecked")
    public static <R extends BigDecimal> R getAsBigDecimal(Object obj, R defaultValue) {
        try {
            BigDecimal answer = getAsBigDecimal(obj);
            return answer != null ? (R) answer : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 数据类型转换
     *
     * @param obj the object to use
     * @param clz the type for conversion
     *
     * @return 转换后的值，如果 obj 为 null 则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <R> R cast(Object obj, Class<R> clz) {
        if (obj == null) {
            return null;
        }
        // 类型已匹配，直接返回
        if (clz.isAssignableFrom(obj.getClass())) {
            return (R) obj;
        }
        // 类型转换
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
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return 转换后的值，如果转换失败则返回 defaultValue
     */
    @SuppressWarnings("unchecked")
    public static <R> R cast(Object obj, R defaultValue) {
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

}
