package ext.library.json.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import ext.library.json.module.CustomModule;
import ext.library.json.util.CustomizeMapper;
import ext.library.tool.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;

import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自定义 Jackson 自动配置
 */
@Slf4j
@RequiredArgsConstructor
@AutoConfiguration(before = JacksonAutoConfiguration.class)
@EnableConfigurationProperties({JacksonProperties.class})
public class CustomJacksonAutoConfig {

    // 没有使用 {@link RequestBody} 反序列化时生效


    /**
     * 日期参数接收转换器，将 json 字符串转为日期类型
     *
     * @return MVC LocalDateTime 参数接收转换器
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConvert() {
        return new Converter<String, LocalDateTime>() {
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
    public Converter<String, LocalDate> localDateConvert() {
        return new Converter<String, LocalDate>() {
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
    public Converter<String, LocalTime> localTimeConvert() {
        return new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(@Nonnull String source) {
                return LocalTime.parse(source, DateUtil.FORMATTER_HMS);
            }
        };
    }

    /**
     * 自定义 objectMapper
     *
     * @return ObjectMapper
     */
    @Bean
    @Primary
    @ConditionalOnBean(JsonMapper.class)
    public JsonMapper jsonMapper(JsonMapper jsonMapper) {
        // 继承所有 spring.jackson 配置
        JsonMapper mapper = new JsonMapper.Builder(jsonMapper)
                // 添加 JSR310 模块（Java 8 时间）;显式添加（更安全）
                .addModules(new JavaTimeModule()).addModule(new Jdk8Module()).addModule(new ParameterNamesModule())
                // 添加自定义模块
                .addModule(new CustomModule())
                // 忽略未知字段
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 避免将 LocalDateTime 转为时间戳
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 只序列化非 null 字段
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                // 字段可见性：允许序列化所有字段（无需 getter）
                .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY).visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY).visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE).visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE).build();
        // 更新 JsonUtils 中的 ObjectMapper，保持容器和工具类中的 ObjectMapper 对象一致
        CustomizeMapper.setMAPPER(mapper);
        log.info("[🔁] JSON 模块载入成功");
        return mapper;
    }

}