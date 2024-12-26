package ext.library.holidays.core;

import java.io.FileInputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import ext.library.holidays.config.HolidaysProperties;
import ext.library.json.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ResourceUtils;

/**
 * 节假日实现
 */
@Slf4j
@RequiredArgsConstructor
public class HolidaysApiImpl implements HolidaysApi, InitializingBean {

    /**
     * 存储节假日
     */
    private static final Map<Integer, Map<String, Byte>> YEAR_DATA_MAP = Maps.newHashMap();

    private final HolidaysProperties properties;

    @Override
    public DaysType getDaysType(@NotNull LocalDate localDate) {
        int year = localDate.getYear();
        Map<String, Byte> dataMap = YEAR_DATA_MAP.get(year);
        // 对于没有数据的，我们按正常的周六日来判断，
        if (dataMap == null) {
            log.error("没有相应年份的数据：[{}]，请自行升级或维护数据！", year);
            return isWeekDay(localDate);
        }
        // 日期信息
        int monthValue = localDate.getMonthValue();
        int dayOfMonth = localDate.getDayOfMonth();
        // 月份和日期
        String monthAndDay = String.format("%02d%02d", monthValue, dayOfMonth);
        Byte result = dataMap.get(monthAndDay);
        if (result != null) {
            return DaysType.from(result);
        } else {
            return isWeekDay(localDate);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int[] years = new int[]{2019, 2020, 2021, 2022, 2023, 2024, 2025};
        for (int year : years) {
            Map<String, Byte> dataMap = JsonUtil.readMap(new FileInputStream(ResourceUtils.getFile("classpath:data/" + year + "_data.json")), Byte.class);
            YEAR_DATA_MAP.put(year, dataMap);
        }
        List<HolidaysProperties.ExtData> extDataList = properties.getExtData();
        for (HolidaysProperties.ExtData extData : extDataList) {
            Map<String, Byte> dataMap = JsonUtil.readMap(new FileInputStream(ResourceUtils.getFile(extData.getDataPath())), Byte.class);
            YEAR_DATA_MAP.put(extData.getYear(), dataMap);
        }
    }

    /**
     * 判断是否工作日
     *
     * @param localDate LocalDate
     * @return DaysType
     */
    private static DaysType isWeekDay(@NotNull LocalDate localDate) {
        int week = localDate.getDayOfWeek().getValue();
        return week == DayOfWeek.SATURDAY.getValue() || week == DayOfWeek.SUNDAY.getValue() ? DaysType.REST_DAYS : DaysType.WEEKDAYS;
    }

}
