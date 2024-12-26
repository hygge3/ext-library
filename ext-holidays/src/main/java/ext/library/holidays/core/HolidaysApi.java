package ext.library.holidays.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.jetbrains.annotations.NotNull;

/**
 * 节假日接口
 */
public interface HolidaysApi {

    /**
     * 获取日期类型
     *
     * @param localDate LocalDate
     * @return DaysType
     */
    DaysType getDaysType(LocalDate localDate);

    /**
     * 获取日期类型
     *
     * @param localDateTime LocalDateTime
     * @return DaysType
     */
    default DaysType getDaysType(@NotNull LocalDateTime localDateTime) {
        return getDaysType(localDateTime.toLocalDate());
    }

    /**
     * 获取日期类型
     *
     * @param date Date
     * @return DaysType
     */
    default DaysType getDaysType(@NotNull Date date) {
        return getDaysType(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * 判断是否工作日
     *
     * @param localDate LocalDate
     * @return 是否工作日
     */
    default boolean isWeekdays(LocalDate localDate) {
        return DaysType.WEEKDAYS.equals(getDaysType(localDate));
    }

    /**
     * 判断是否工作日
     *
     * @param localDateTime LocalDateTime
     * @return 是否工作日
     */
    default boolean isWeekdays(LocalDateTime localDateTime) {
        return DaysType.WEEKDAYS.equals(getDaysType(localDateTime));
    }

    /**
     * 判断是否工作日
     *
     * @param date Date
     * @return 是否工作日
     */
    default boolean isWeekdays(Date date) {
        return DaysType.WEEKDAYS.equals(getDaysType(date));
    }

}
