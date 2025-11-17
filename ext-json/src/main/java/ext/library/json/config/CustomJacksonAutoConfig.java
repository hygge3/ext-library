package ext.library.json.config;

import ext.library.json.module.CustomModule;
import ext.library.tool.util.DateUtil;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;

import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自定义 Jackson 自动配置
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class)
@EnableConfigurationProperties({JacksonProperties.class})
public class CustomJacksonAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // 没有使用 {@link RequestBody} 反序列化时生效

    /**
     * 日期参数接收转换器，将 json 字符串转为日期类型
     *
     * @return MVC LocalDateTime 参数接收转换器
     */
    @Bean
    public Converter<@NonNull String, @NonNull LocalDateTime> localDateTimeConvert() {
        return new Converter<@NonNull String, @NonNull LocalDateTime>() {
            @Override
            public LocalDateTime convert(@Nonnull String source) {
                return LocalDateTime.parse(source, DateUtil.FORMATTER_YMD_HMS);
            }
        };
    }

    /**
     * 日期参数接收转换器，将 json 字符串转为日期类型
     *
     * @return MVC LocalDate 参数接收转换器
     */
    @Bean
    public Converter<@NonNull String, @NonNull LocalDate> localDateConvert() {
        return new Converter<@NonNull String, @NonNull LocalDate>() {
            @Override
            public LocalDate convert(@Nonnull String source) {
                return LocalDate.parse(source, DateUtil.FORMATTER_YMD);
            }
        };
    }

    /**
     * 日期参数接收转换器，将 json 字符串转为日期类型
     *
     * @return MVC LocalTime 参数接收转换器
     */
    @Bean
    public Converter<@NonNull String, @NonNull LocalTime> localTimeConvert() {
        return new Converter<@NonNull String, @NonNull LocalTime>() {
            @Override
            public LocalTime convert(@Nonnull String source) {
                return LocalTime.parse(source, DateUtil.FORMATTER_HMS);
            }
        };
    }

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        log.info("[🔁] JSON 模块载入成功");
        return builder -> builder.findAndAddModules()
                // 添加自定义模块
                .addModules(new CustomModule());
    }

}