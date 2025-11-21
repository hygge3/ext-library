package ext.library.json.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import ext.library.json.module.CustomModule;
import ext.library.tool.util.DateUtil;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;

import jakarta.annotation.Nonnull;
import java.text.SimpleDateFormat;
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

    /**
     * 创建并返回一个 Jackson2ObjectMapperBuilderCustomizer Bean, 用于自定义 Jackson ObjectMapper 的配置。
     * <p>
     * 该自定义器配置包括：
     * <ul>
     * <li> 禁用空 Bean 和未知属性失败检查 </li>
     * <li> 禁用日期序列化为时间戳，使用自定义日期格式和 GMT+8 时区 </li>
     * <li> 通过 ServiceLoader 自动发现模块 </li>
     * <li> 为 BigInteger,Long 和 long 类型配置 BigNumberSerializer</li>
     * <li> 为 LocalDateTime,LocalDate 和 LocalTime 类型配置自定义反序列化器 </li>
     * <li> 添加 LocalDateTime,LocalTime 和 LocalDate 的自定义序列化器 </li>
     * </ul>
     *
     * @return Jackson2ObjectMapperBuilderCustomizer 配置自定义器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 这一部分也可以在 yaml 中配置
            builder
                    // 序列化时，对象为 null，是否抛异常
                    .failOnEmptyBeans(false)
                    // 反序列化时，json 中包含 pojo 不存在属性时，是否抛异常
                    .failOnUnknownProperties(false)
                    // 禁止将 java.util.Date, Calendar 序列化为数字 (时间戳)
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    // 设置 java.util.Date, Calendar 序列化、反序列化的格式
                    .dateFormat(new SimpleDateFormat(DateUtil.STRING_FORMATTER_YMD_HMS));
            builder.findModulesViaServiceLoader(true);
            builder.modules(new CustomModule());
        };
    }
}