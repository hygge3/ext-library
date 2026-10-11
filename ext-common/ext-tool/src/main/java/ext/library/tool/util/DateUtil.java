package ext.library.tool.util;


import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.TimeUnit;

/**
 * 日期时间工具类，提供各种日期时间处理方法
 *
 * <p>设计目的：封装Java 8+日期时间API，提供简洁易用的日期时间操作方法，提高开发效率</p>
 *
 * <p>核心功能：
 * <ul>
 *   <li>日期时间的解析和格式化</li>
 *   <li>时间戳与日期时间的相互转换</li>
 *   <li>时间段计算和比较</li>
 *   <li>各种时间边界的获取（当天、当周、当月等）</li>
 *   <li>时区转换和处理</li>
 * </ul>
 * </p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>数据转换：将字符串或时间戳转换为日期时间对象</li>
 *   <li>格式处理：将日期时间对象格式化为指定格式的字符串</li>
 *   <li>时间计算：计算两个日期之间的时间差</li>
 *   <li>边界获取：获取当天、当周、当月的开始和结束时间</li>
 *   <li>时间比较：判断某个时间是否在指定时间段内</li>
 * </ul>
 * </p>
 *
 * <p>注意事项：
 * <ul>
 *   <li>默认使用GMT+8时区</li>
 *   <li>所有方法均为静态方法，无需实例化</li>
 *   <li>使用Java 8+的日期时间API，线程安全</li>
 * </ul>
 * </p>
 */
public final class DateUtil {

    private DateUtil() {
    }

    // region Constants

    /** 默认时区偏移 */
    public static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.of("+8");

    /** 默认时区 */
    public static final ZoneId DEFAULT_ZONE_ID = DEFAULT_ZONE_OFFSET.normalized();

    /** 日期时间格式化字符串 YMD HMS */
    public static final String STRING_FORMATTER_YMD_HMS = "yyyy-MM-dd HH:mm:ss";

    /** 日期时间格式化 YMD HMS */
    public static final DateTimeFormatter FORMATTER_YMD_HMS = DateTimeFormatter.ofPattern(STRING_FORMATTER_YMD_HMS);

    /** 日期格式化字符串 YMD */
    public static final String STRING_FORMATTER_YMD = "yyyy-MM-dd";

    /** 日期格式化 YMD */
    public static final DateTimeFormatter FORMATTER_YMD = DateTimeFormatter.ofPattern(STRING_FORMATTER_YMD);

    /** 时间格式化字符串 HMS */
    public static final String STRING_FORMATTER_HMS = "HH:mm:ss";

    /** 时间格式化 HMS */
    public static final DateTimeFormatter FORMATTER_HMS = DateTimeFormatter.ofPattern(STRING_FORMATTER_HMS);

    // endregion Constants

    // region LocalDateTime

    /**
     * 字符串转时间
     *
     * @param str yyyy-MM-dd HH:mm:ss 格式字符串
     *
     * @return java.time.LocalDateTime 时间
     */
    public static LocalDateTime parse(String str) {
        return LocalDateTime.parse(str, FORMATTER_YMD_HMS);
    }

    /**
     * 时间戳转时间，使用 GMT+8 时区
     *
     * @param timestamp 时间戳 - 毫秒
     *
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime parse(Long timestamp) {
        return parse(timestamp, DEFAULT_ZONE_ID);
    }

    /**
     * 时间戳转时间
     *
     * @param timestamp 时间戳 - 毫秒
     * @param zoneId    时区
     *
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime parse(Long timestamp, ZoneId zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId);
    }

    /**
     * 时间转时间戳 (毫秒)
     *
     * @param dateTime 日期时间
     *
     * @return {@code Long }
     */
    public static Long toTimestamp(LocalDateTime dateTime) {
        return toTimestamp(dateTime, DEFAULT_ZONE_OFFSET);
    }

    /**
     * 时间转时间戳 (毫秒)
     *
     * @param dateTime 日期时间
     * @param offset   时区
     *
     * @return {@code Long }
     */
    public static Long toTimestamp(LocalDateTime dateTime, ZoneOffset offset) {
        return dateTime.toInstant(offset).toEpochMilli();
    }

    /**
     * 日期时间格式化
     *
     * @param dateTime 日期时间
     *
     * @return {@code String }
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, FORMATTER_YMD_HMS);
    }

    /**
     * 日期时间格式化
     *
     * @param dateTime  日期时间
     * @param formatter 格式化模板
     *
     * @return {@code String }
     */
    public static String format(LocalDateTime dateTime, String formatter) {
        return format(dateTime, DateTimeFormatter.ofPattern(formatter));
    }

    // endregion LocalDateTime

    // region LocalDate

    /**
     * 字符串转日期
     *
     * @param str yyyy-MM-dd 格式字符串
     *
     * @return java.time.LocalDate 日期
     */
    public static LocalDate parseDate(String str) {
        return LocalDate.parse(str, FORMATTER_YMD);
    }

    /**
     * 日期格式化
     *
     * @param date 日期
     *
     * @return {@code String }
     */
    public static String format(LocalDate date) {
        return format(date, FORMATTER_YMD);
    }

    /**
     * 日期格式化
     *
     * @param date      日期
     * @param formatter 格式化模板
     *
     * @return {@code String }
     */
    public static String format(LocalDate date, String formatter) {
        return format(date, DateTimeFormatter.ofPattern(formatter));
    }

    // endregion LocalDate

    // region LocalTime

    /**
     * 字符串转时间
     *
     * @param str HH:mm:ss 格式字符串
     *
     * @return java.time.LocalTime 日期
     */
    public static LocalTime parseTime(String str) {
        return LocalTime.parse(str, FORMATTER_HMS);
    }

    /**
     * 时间格式化
     *
     * @param time 时间
     *
     * @return {@code String }
     */
    public static String format(LocalTime time) {
        return format(time, FORMATTER_HMS);
    }

    /**
     * 时间格式化
     *
     * @param time      时间
     * @param formatter 格式化模板
     *
     * @return {@code String }
     */
    public static String format(LocalTime time, String formatter) {
        return format(time, DateTimeFormatter.ofPattern(formatter));
    }

    // endregion LocalTime

    // region Duration

    /**
     * 格式化 Duration 为天时分秒毫秒
     *
     * @param duration 持续时间
     *
     * @return 格式化后的字符串
     */
    public static String format(Duration duration) {
        if (duration.isZero()) {
            return "0毫秒";
        }
        if (duration.isNegative()) {
            return "-" + format(duration.negated());
        }

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        long millis = duration.toMillis() % 1000;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (seconds > 0) {
            sb.append(seconds).append("秒");
        }
        if (millis > 0) {
            sb.append(millis).append("毫秒");
        }

        return sb.toString();
    }

    /**
     * 计算相差时间
     *
     * @param start 开始时间
     * @param end   结束时间
     *
     * @return 持续时间
     */
    public static Duration between(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end);
    }

    /**
     * 将时间间隔和时间单位转换为 Duration 对象
     *
     * @param interval 时间间隔
     * @param timeUnit 时间单位
     *
     * @return 转换后的 Duration 对象
     */
    public static Duration convert(long interval, TimeUnit timeUnit) {
        return Duration.of(interval, timeUnit.toChronoUnit());
    }

    // endregion Duration

    // region Boundary

    /**
     * 获取当天的开始时间
     *
     * @param time 时间
     *
     * @return 当天的开始时间
     */
    public static LocalDateTime getDayStart(LocalDateTime time) {
        return time.with(LocalTime.MIN);
    }

    /**
     * 获取当天的结束时间
     *
     * @param time 时间
     *
     * @return 当天的结束时间
     */
    public static LocalDateTime getDayEnd(LocalDateTime time) {
        return time.with(LocalTime.MAX);
    }

    /**
     * 获取当周的开始时间
     *
     * @param time 时间
     *
     * @return 当周的开始时间
     */
    public static LocalDateTime getWeekStart(LocalDateTime time) {
        return time.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
    }

    /**
     * 获取当周的结束时间
     *
     * @param time 时间
     *
     * @return 当周的结束时间
     */
    public static LocalDateTime getWeekEnd(LocalDateTime time) {
        return time.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
    }

    /**
     * 获取当月的开始时间
     *
     * @param time 时间
     *
     * @return 当月的开始时间
     */
    public static LocalDateTime getMonthStart(LocalDateTime time) {
        return time.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
    }

    /**
     * 获取当月的结束时间
     *
     * @param time 时间
     *
     * @return 当月的结束时间
     */
    public static LocalDateTime getMonthEnd(LocalDateTime time) {
        return time.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
    }

    // endregion Boundary

    // region Comparison

    /**
     * 判断某个时间是否在某个时间段
     *
     * @param startTime 起始时间
     * @param dateTime  比较时间
     * @param endTime   结束时间
     *
     * @return 是否在…之间
     */
    public static boolean isBetween(LocalDateTime startTime, LocalDateTime dateTime, LocalDateTime endTime) {
        return dateTime.isBefore(endTime) && dateTime.isAfter(startTime);
    }

    /**
     * 判断某个时间在时间段内的位置
     * 前:-1，中:0，后:1
     *
     * @param startTime 起始时间
     * @param dateTime  比较时间
     * @param endTime   结束时间
     *
     * @return 是否在…之间
     */
    public static int position(LocalDateTime startTime, LocalDateTime dateTime, LocalDateTime endTime) {
        // 未开始
        if (startTime.isAfter(dateTime)) {
            return -1;
            // 在内
        } else if (endTime.isAfter(dateTime)) {
            return 0;
            // 已结束
        } else {
            return 1;
        }
    }

    // endregion Comparison

    // region Common

    /**
     * 使用指定的格式化器格式化时间对象
     *
     * @param temporal  需要格式化的时间对象
     * @param formatter 时间格式化器
     *
     * @return 格式化后的时间字符串
     */
    public static String format(Temporal temporal, DateTimeFormatter formatter) {
        return formatter.format(temporal);
    }

    // endregion Common

}
