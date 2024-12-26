package ext.library.holidays.config;

import ext.library.holidays.core.HolidaysApi;
import ext.library.holidays.core.HolidaysApiImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 配置
 */
@AutoConfiguration
@EnableConfigurationProperties(HolidaysProperties.class)
public class HolidaysAutoConfiguration {

    @Bean
    public HolidaysApi holidaysApi(HolidaysProperties properties) {
        return new HolidaysApiImpl(properties);
    }

}
