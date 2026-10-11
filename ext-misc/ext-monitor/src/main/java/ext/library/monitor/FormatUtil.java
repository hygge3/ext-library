package ext.library.monitor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 格式化工具类
 */
final class FormatUtil {

    private FormatUtil() {}

    /**
     * 格式化字节数为可读字符串
     *
     * @param bytes 字节数
     * @return 格式化后的字符串（如 "1.5GB"）
     */
    static String formatBytes(long bytes) {
        double format = 1024.0;
        double kb = bytes / format;
        if (kb < format) {
            return decimalFormat("#.##KB", kb);
        }
        double mb = kb / format;
        if (mb < format) {
            return decimalFormat("#.##MB", mb);
        }
        double gb = mb / format;
        if (gb < format) {
            return decimalFormat("#.##GB", gb);
        }
        return decimalFormat("#.##TB", gb / format);
    }

    /**
     * 格式化数字
     *
     * @param pattern 格式模式
     * @param number 数字
     * @return 格式化后的字符串
     */
    static String decimalFormat(String pattern, double number) {
        return new DecimalFormat(pattern).format(number);
    }

    /**
     * 格式化 double 值，保留两位小数
     *
     * @param value 原始值
     * @return 格式化后的值
     */
    static double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
