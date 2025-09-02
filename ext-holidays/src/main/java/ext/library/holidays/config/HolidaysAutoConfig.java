package ext.library.holidays.config;

import ext.library.holidays.core.HolidaysApi;
import ext.library.holidays.core.HolidaysApiImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 配置
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(HolidaysProperties.class)
public class HolidaysAutoConfig {

    @Bean
    public HolidaysApi holidaysApi(HolidaysProperties properties) {
        log.info("[📅] 节假日模块载入成功");
        return new HolidaysApiImpl(properties);
    }

}