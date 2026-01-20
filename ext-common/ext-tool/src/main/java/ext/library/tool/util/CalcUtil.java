package ext.library.tool.util;

import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数学计算工具类
 * <p>
 * 由于 Java 的简单类型不能够精确的对浮点数进行运算，
 * 此工具类提供基于 {@link BigDecimal} 的精确浮点数运算，包括加减乘除和舍入。
 *
 * @since 2025.01.01
 */
public final class CalcUtil {

    /** 默认除法精度：小数点后 10 位 */
    private static final int DEFAULT_DIV_SCALE = 10;

    private CalcUtil() {
    }

    /**
     * 精确加法运算
     *
     * @param augend  被加数
     * @param addends 加数（可变参数）
     * @return 所有参数的和
     */
    public static BigDecimal add(Object augend, Object... addends) {
        BigDecimal result = TypeCastUtil.getAsBigDecimal(augend);
        for (Object addend : addends) {
            result = result.add(TypeCastUtil.getAsBigDecimal(addend));
        }
        return result;
    }

    /**
     * 精确减法运算
     *
     * @param minuend     被减数
     * @param subtrahends 减数（可变参数）
     * @return 所有参数的差
     */
    public static BigDecimal sub(Object minuend, Object... subtrahends) {
        BigDecimal result = TypeCastUtil.getAsBigDecimal(minuend);
        for (Object subtrahend : subtrahends) {
            result = result.subtract(TypeCastUtil.getAsBigDecimal(subtrahend));
        }
        return result;
    }

    /**
     * 精确乘法运算
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mul(Object v1, Object v2) {
        BigDecimal b1 = TypeCastUtil.getAsBigDecimal(v1);
        BigDecimal b2 = TypeCastUtil.getAsBigDecimal(v2);
        return b1.multiply(b2);
    }

    /**
     * 精确除法运算（默认精度）
     * <p>
     * 当除不尽时，精确到小数点后 10 位，其余数字四舍五入。
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static BigDecimal div(Object v1, Object v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 精确除法运算（指定精度）
     * <p>
     * 当除不尽时，按指定精度四舍五入。
     *
     * @param v1    被除数
     * @param v2    除数（不能为零）
     * @param scale 精度（小数点后位数，不能小于 0）
     * @return 两个参数的商
     */
    public static BigDecimal div(Object v1, Object v2, int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 精确除法运算（指定精度和舍入模式）
     *
     * @param v1           被除数
     * @param v2           除数（不能为零）
     * @param scale        精度（小数点后位数，不能小于 0）
     * @param roundingMode 舍入模式
     * @return 两个参数的商
     */
    public static BigDecimal div(Object v1, Object v2, int scale, RoundingMode roundingMode) {
        Assert.isTrue(scale >= 0, "精确度不能小于 0");
        BigDecimal b1 = TypeCastUtil.getAsBigDecimal(v1);
        BigDecimal b2 = TypeCastUtil.getAsBigDecimal(v2);
        return b1.divide(b2, scale, roundingMode);
    }

    /**
     * 四舍五入
     *
     * @param v     需要舍入的数字
     * @param scale 精度（小数点后位数）
     * @return 四舍五入后的结果
     */
    public static BigDecimal round(Object v, int scale) {
        return roundingMode(v, scale, RoundingMode.HALF_UP);
    }

    /**
     * 按指定模式舍入
     *
     * @param v            需要舍入的数字
     * @param scale        精度（小数点后位数，不能小于 0）
     * @param roundingMode 舍入模式
     * @return 舍入后的结果
     */
    public static BigDecimal roundingMode(Object v, int scale, RoundingMode roundingMode) {
        Assert.isTrue(scale >= 0, "精确度不能小于 0");
        BigDecimal b = TypeCastUtil.getAsBigDecimal(v);
        return b.divide(BigDecimal.ONE, scale, roundingMode);
    }

    /**
     * 转换为百分比
     * <p>
     * 将小数乘以 100 并按指定精度舍入。
     *
     * @param value 小数值（如 0.1234）
     * @param scale 精度（小数点后位数）
     * @return 百分比值（如 12.34）
     */
    public static BigDecimal percentage(Object value, int scale) {
        return TypeCastUtil.getAsBigDecimal(value)
                .multiply(BigDecimal.valueOf(100))
                .setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 分转元
     *
     * @param cents 金额（单位：分）
     * @return 金额（单位：元）
     */
    public static BigDecimal centToYuan(long cents) {
        return div(cents, 100, 2);
    }

    /**
     * 元转分
     *
     * @param yuan 金额（单位：元）
     * @return 金额（单位：分）
     */
    public static long yuanToCent(Object yuan) {
        return mul(yuan, 100).setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
