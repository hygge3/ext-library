package ext.library.holidays.config;

import ext.library.holidays.core.HolidaysApi;
import ext.library.holidays.core.HolidaysApiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 配置
 */
@AutoConfiguration
@EnableConfigurationProperties(HolidaysProperties.class)
public class HolidaysAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());


    @Bean
    public HolidaysApi holidaysApi(HolidaysProperties properties) {
        log.info("[📅] 节假日模块载入成功");
        return new HolidaysApiImpl(properties);
    }

}