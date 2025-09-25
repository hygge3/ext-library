package ext.library.tool.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 一个简单的数据类型转换工具类
 */
public class GeneralTypeCastUtil {

    /**
     * Gets a String from an Object in a null-safe manner.
     * <p>
     * The String is obtained via <code>toString</code>.
     *
     * @param obj the object to use
     *
     * @return the value of the Object as a String, <code>null</code> if null object input
     */
    public static String getAsString(final Object obj) {
        if (obj != null) {
            return obj.toString();
        }
        return null;
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
    public static String getAsString(final Object obj, final String defaultValue) {
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
    public static Number getAsNumber(final Object obj) {
        if (obj != null) {
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
                    } catch (final ParseException e) {
                        throw new NumberFormatException("For input string: \"" + obj + "\"");
                    }
                }
                default -> throw new UnsupportedOperationException();
            }
        }
        return null;
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
    public static <R extends Number> R getAsNumber(final Object obj, R defaultValue) {
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
    public static Boolean getAsBoolean(final Object obj) {
        if (obj != null) {
            return switch (obj) {
                case Boolean aBoolean -> aBoolean;
                case String s -> Boolean.valueOf(s);
                case Number n -> (n.intValue() != 0) ? Boolean.TRUE : Boolean.FALSE;
                default -> throw new UnsupportedOperationException();
            };
        }
        return null;
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
    public static Boolean getAsBoolean(final Object obj, final Boolean defaultValue) {
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
    public static boolean getAsBooleanValue(final Object obj) {
        final Boolean booleanObject = getAsBoolean(obj);
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
    public static boolean getAsBooleanValue(final Object obj, final boolean defaultValue) {
        final Boolean booleanObject = getAsBoolean(obj);
        return ObjectUtil.defaultIfNull(booleanObject, defaultValue);
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
    public static Byte getAsByte(final Object obj) {
        final Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        } else if (answer instanceof Byte) {
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
    public static Byte getAsByte(final Object obj, final Byte defaultValue) {
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
    public static byte getAsByteValue(final Object obj) {
        final Byte byteObject = getAsByte(obj);
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
    public static byte getAsByteValue(final Object obj, final byte defaultValue) {
        final Byte byteObject = getAsByte(obj);
        return ObjectUtil.defaultIfNull(byteObject, defaultValue);
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
    public static Short getAsShort(final Object obj) {
        final Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        } else if (answer instanceof Short) {
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
    public static Short getAsShort(final Object obj, final Short defaultValue) {
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
    public static short getAsShortValue(final Object obj) {
        final Short shortObject = getAsShort(obj);
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
    public static short getAsShortValue(final Object obj, final short defaultValue) {
        final Short shortObject = getAsShort(obj);
        return ObjectUtil.defaultIfNull(shortObject, defaultValue);
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
    public static Integer getAsInteger(final Object obj) {
        final Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        } else if (answer instanceof Integer) {
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
    public static Integer getAsInteger(final Object obj, final Integer defaultValue) {
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
    public static int getAsIntValue(final Object obj) {
        final Integer integerObject = getAsInteger(obj);
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
    public static int getAsIntValue(final Object obj, final int defaultValue) {
        final Integer integerObject = getAsInteger(obj);
        return ObjectUtil.defaultIfNull(integerObject, defaultValue);
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
    public static Long getAsLong(final Object obj) {
        final Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        } else if (answer instanceof Long) {
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
    public static Long getAsLong(final Object obj, final Long defaultValue) {
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
    public static long getAsLongValue(final Object obj) {
        final Long longObject = getAsLong(obj);
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
    public static long getAsLongValue(final Object obj, final long defaultValue) {
        final Long longObject = getAsLong(obj);
        return ObjectUtil.defaultIfNull(longObject, defaultValue);
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
    public static Float getAsFloat(final Object obj) {
        final Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        } else if (answer instanceof Float) {
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
    public static Float getAsFloat(final Object obj, final Float defaultValue) {
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
    public static float getAsFloatValue(final Object obj) {
        final Float floatObject = getAsFloat(obj);
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
    public static float getAsFloatValue(final Object obj, final float defaultValue) {
        final Float floatObject = getAsFloat(obj);
        return ObjectUtil.defaultIfNull(floatObject, defaultValue);
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
    public static Double getAsDouble(final Object obj) {
        final Number answer = getAsNumber(obj);
        if (answer == null) {
            return null;
        } else if (answer instanceof Double) {
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
    public static Double getAsDouble(final Object obj, final Double defaultValue) {
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
    public static double getAsDoubleValue(final Object obj) {
        final Double doubleObject = getAsDouble(obj);
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
    public static double getAsDoubleValue(final Object obj, final double defaultValue) {
        final Double doubleObject = getAsDouble(obj);
        return ObjectUtil.defaultIfNull(doubleObject, defaultValue);
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
    public static BigInteger getAsBigInteger(final Object obj) {
        if (obj != null) {
            if (obj instanceof BigInteger) {
                return (BigInteger) obj;
            } else if (obj instanceof String) {
                return new BigInteger((String) obj);
            } else if (obj instanceof Number || obj instanceof Boolean) {
                final Number answer = getAsNumber(obj);
                if (answer != null) {
                    return BigInteger.valueOf(answer.longValue());
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return null;
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
    public static <R extends BigInteger> R getAsBigInteger(final Object obj, final R defaultValue) {
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
    public static BigDecimal getAsBigDecimal(final Object obj) {
        if (obj != null) {
            if (obj instanceof BigDecimal) {
                return (BigDecimal) obj;
            } else if (obj instanceof String str) {
                return new BigDecimal(str);
            } else if (obj instanceof Number || obj instanceof Boolean) {
                final Number answer = getAsNumber(obj);
                if (answer != null) {
                    return new BigDecimal(String.valueOf(answer));
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return null;
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
    public static <R extends BigDecimal> R getAsBigDecimal(final Object obj, final R defaultValue) {
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
    public static <R> R cast(final Object obj, final Class<R> clz) {
        if (obj == null) {
            throw new IllegalArgumentException("'obj' must not be null");
        }
        if (clz == null) {
            throw new IllegalArgumentException("'clz' must not be null");
        }
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
    public static <R> R cast(final Object obj, final R defaultValue) {
        if (obj == null) {
            throw new IllegalArgumentException("'obj' must not be null");
        }
        if (defaultValue == null) {
            throw new IllegalArgumentException("'defaultValue' must not be null");
        }
        R result;
        final Class<?> clz = defaultValue.getClass();
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