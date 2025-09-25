package ext.library.json.util;

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

/**
 * 自定义映射器
 */
public class CustomizeMapper {

    protected static JsonMapper MAPPER = JsonMapper.builder()
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

    public static void setMAPPER(JsonMapper MAPPER) {
        CustomizeMapper.MAPPER = MAPPER;
    }

}