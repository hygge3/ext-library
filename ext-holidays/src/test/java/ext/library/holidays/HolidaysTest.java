package ext.library.holidays;

import ext.library.holidays.config.HolidaysAutoConfig;
import ext.library.holidays.config.HolidaysProperties;
import ext.library.holidays.core.DaysType;
import ext.library.holidays.core.HolidaysApi;
import ext.library.holidays.core.HolidaysApiImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

class HolidaysTest {

    private HolidaysApi holidaysApi;

    @BeforeEach
    public void setup() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        HolidaysAutoConfig configuration = new HolidaysAutoConfig();
        holidaysApi = configuration.holidaysApi(new HolidaysProperties());
        ((HolidaysApiImpl) holidaysApi).afterPropertiesSet();
    }

    @Test
    void test() {
        DaysType daysType = holidaysApi.getDaysType(LocalDate.of(2025, 1, 1));
        Assertions.assertEquals(DaysType.HOLIDAYS, daysType);
        Assertions.assertFalse(holidaysApi.isWeekdays(LocalDate.of(2023, 9, 29)));
        Assertions.assertTrue(holidaysApi.isWeekdays(LocalDate.of(2023, 10, 7)));
    }

}