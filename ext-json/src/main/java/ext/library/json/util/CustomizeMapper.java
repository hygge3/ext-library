package ext.library.json.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ext.library.json.serializer.BigNumberSerializer;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 自定义映射器
 */
public class CustomizeMapper {

    @Setter
    protected static ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            // 是否字符数组输出 json 数组
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).enable(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS).registerModule(getModule());

    public static SimpleModule getModule() {
        return new JavaTimeModule().addSerializer(Long.class, BigNumberSerializer.INSTANCE).addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE).addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE).addSerializer(BigDecimal.class, ToStringSerializer.instance);
    }
}