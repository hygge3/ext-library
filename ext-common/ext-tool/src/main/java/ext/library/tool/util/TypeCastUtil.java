package ext.library.tool.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 简单的数据类型转换工具类
 */
public final class TypeCastUtil {

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
        return obj.toString();
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
        String answer = getAsString(obj);
        return ObjectUtil.defaultIfNull(answer, defaultValue);
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
        switch (obj) {
            case Number number -> {
                return number;
            }
            case Boolean aBoolean -> {
                return aBoolean ? 1 : 0;
            }
            case String s -> {
                try {
                    return NumberFormat.getInstance().parse(s);
                } catch (ParseException e) {
                    throw new NumberFormatException(obj + "不是有效的数字格式");
                }
            }
            default -> throw new UnsupportedOperationException();
        }
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
        Number answer = getAsNumber(obj);
        return (R) ObjectUtil.defaultIfNull(answer, defaultValue);
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
        return switch (obj) {
            case Boolean bool -> bool;
            case String s -> Boolean.valueOf(s);
            case Number n -> (n.intValue() != 0) ? Boolean.TRUE : Boolean.FALSE;
            default -> throw new UnsupportedOperationException();
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
        Boolean answer = getAsBoolean(obj);
        return ObjectUtil.defaultIfNull(answer, defaultValue);
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
        Boolean booleanObject = getAsBoolean(obj);
        return ObjectUtil.defaultIfNull(booleanObject, false);
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
        if (answer instanceof Byte) {
            return (Byte) answer;
        }
        return answer.byteValue();
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
        Byte answer = getAsByte(obj);
        return ObjectUtil.defaultIfNull(answer, defaultValue);
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
        Byte byteObject = getAsByte(obj);
        return ObjectUtil.defaultIfNull(byteObject, (byte) 0);
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
        if (answer instanceof Short) {
            return (Short) answer;
        }
        return answer.shortValue();
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
        Short answer = getAsShort(obj);
        return ObjectUtil.defaultIfNull(answer, defaultValue);
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
        Short shortObject = getAsShort(obj);
        return ObjectUtil.defaultIfNull(shortObject, (short) 0);
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
        if (answer instanceof Integer) {
            return (Integer) answer;
        }
        return answer.intValue();
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
        Integer answer = getAsInteger(obj);
        return ObjectUtil.defaultIfNull(answer, defaultValue);
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
        Integer integerObject = getAsInteger(obj);
        return ObjectUtil.defaultIfNull(integerObject, 0);
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
        if (answer instanceof Long) {
            return (Long) answer;
        }
        return answer.longValue();
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
        Long answer = getAsLong(obj);
        return ObjectUtil.defaultIfNull(answer, defaultValue);
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
        Long longObject = getAsLong(obj);
        return ObjectUtil.defaultIfNull(longObject, 0L);
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
        if (answer instanceof Float) {
            return (Float) answer;
        }
        return answer.floatValue();
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
        Float answer = getAsFloat(obj);
        return ObjectUtil.defaultIfNull(answer, defaultValue);
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
        Float floatObject = getAsFloat(obj);
        return ObjectUtil.defaultIfNull(floatObject, 0F);
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
        if (answer instanceof Double) {
            return (Double) answer;
        }
        return answer.doubleValue();
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
        Double answer = getAsDouble(obj);
        return ObjectUtil.defaultIfNull(answer, defaultValue);
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
        Double doubleObject = getAsDouble(obj);
        return ObjectUtil.defaultIfNull(doubleObject, 0D);
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
     * @return the value of Object as a BigInteger, <code>0</code> if null object input
     */
    public static BigInteger getAsBigInteger(Object obj) {
        if (obj instanceof BigInteger) {
            return (BigInteger) obj;
        } else if (obj instanceof String) {
            return new BigInteger((String) obj);
        } else if (obj instanceof Number || obj instanceof Boolean) {
            Number answer = getAsNumber(obj);
            return BigInteger.valueOf(answer.longValue());
        } else {
            throw new UnsupportedOperationException();
        }
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
        BigInteger answer = getAsBigInteger(obj);
        return (R) ObjectUtil.defaultIfNull(answer, defaultValue);
    }

    /**
     * Gets a BigDecimal from an Object in a null-safe manner.
     * <p>
     * The BigDecimal is obtained from the results of {@link #getAsNumber(Object)}.
     *
     * @param obj the object to use
     *
     * @return the value of Object as a BigDecimal, <code>0</code> if null object input
     */
    public static BigDecimal getAsBigDecimal(Object obj) {
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        } else if (obj instanceof String str) {
            return new BigDecimal(str);
        } else if (obj instanceof Number || obj instanceof Boolean) {
            Number answer = getAsNumber(obj);
            return new BigDecimal(String.valueOf(answer));
        } else {
            throw new UnsupportedOperationException();
        }
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
        BigDecimal answer = getAsBigDecimal(obj);
        return (R) ObjectUtil.defaultIfNull(answer, defaultValue);
    }

    /**
     * This method is mainly used to provide data type conversion services.
     *
     * @param obj the object to use
     * @param clz the type for conversion
     *
     * @return R
     */
    @SuppressWarnings("unchecked")
    public static <R> R cast(Object obj, Class<R> clz) {
        R result;
        if (obj.getClass().isAssignableFrom(clz)) {
            result = (R) obj;
        } else if (Boolean.class.equals(clz) || boolean.class.equals(clz)) {
            result = (R) getAsBoolean(obj);
        } else if (Byte.class.equals(clz) || byte.class.equals(clz)) {
            result = (R) getAsByte(obj);
        } else if (Short.class.equals(clz) || short.class.equals(clz)) {
            result = (R) getAsShort(obj);
        } else if (Integer.class.equals(clz) || int.class.equals(clz)) {
            result = (R) getAsInteger(obj);
        } else if (Long.class.equals(clz) || long.class.equals(clz)) {
            result = (R) getAsLong(obj);
        } else if (Float.class.equals(clz) || float.class.equals(clz)) {
            result = (R) getAsFloat(obj);
        } else if (Double.class.equals(clz) || double.class.equals(clz)) {
            result = (R) getAsDouble(obj);
        } else if (String.class.equals(clz)) {
            result = (R) getAsString(obj);
        } else if (BigInteger.class.isAssignableFrom(clz)) {
            result = (R) getAsBigInteger(obj);
        } else if (BigDecimal.class.isAssignableFrom(clz)) {
            result = (R) getAsBigDecimal(obj);
        } else if (Number.class.isAssignableFrom(clz)) {
            result = (R) getAsNumber(obj);
        } else {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    /**
     * This method is mainly used to provide data type conversion services.
     *
     * @param obj          the object to use
     * @param defaultValue return if the value is null or if the conversion fails
     *
     * @return R
     */
    @SuppressWarnings("unchecked")
    public static <R> R cast(Object obj, R defaultValue) {
        R result;
        Class<?> clz = defaultValue.getClass();
        if (Boolean.class.equals(clz)) {
            result = (R) getAsBoolean(obj, (Boolean) defaultValue);
        } else if (Byte.class.equals(clz)) {
            result = (R) getAsByte(obj, (Byte) defaultValue);
        } else if (Short.class.equals(clz)) {
            result = (R) getAsShort(obj, (Short) defaultValue);
        } else if (Integer.class.equals(clz)) {
            result = (R) getAsInteger(obj, (Integer) defaultValue);
        } else if (Long.class.equals(clz)) {
            result = (R) getAsLong(obj, (Long) defaultValue);
        } else if (Float.class.equals(clz)) {
            result = (R) getAsFloat(obj, (Float) defaultValue);
        } else if (Double.class.equals(clz)) {
            result = (R) getAsDouble(obj, (Double) defaultValue);
        } else if (String.class.equals(clz)) {
            result = (R) getAsString(obj, (String) defaultValue);
        } else if (BigInteger.class.isAssignableFrom(clz)) {
            result = (R) getAsBigInteger(obj, (BigInteger) defaultValue);
        } else if (BigDecimal.class.isAssignableFrom(clz)) {
            result = (R) getAsBigDecimal(obj, (BigDecimal) defaultValue);
        } else if (Number.class.isAssignableFrom(clz)) {
            result = (R) getAsNumber(obj, (Number) defaultValue);
        } else {
            throw new UnsupportedOperationException();
        }
        return result;
    }

}
