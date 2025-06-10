package ext.library.json.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import ext.library.json.module.CustomJavaTimeModule;
import java.math.BigDecimal;
import lombok.Setter;

/**
 * 自定义映射器
 */
public class CustomizeMapper {

    @Setter
    protected static ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            // 是否字符数组输出 json 数组
            .enable(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)
            .registerModule(new CustomJavaTimeModule()
                    .addSerializer(BigDecimal.class, ToStringSerializer.instance));
}
