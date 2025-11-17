package ext.library.json.module;

import ext.library.json.serializer.BigDecimalPlainSerializer;
import ext.library.json.serializer.BigNumberSerializer;
import ext.library.tool.util.DateUtil;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CustomModule extends SimpleModule {
    public CustomModule() {
        super("CustomModule");
        // BigDecimal 处理
        addSerializer(BigDecimal.class, new BigDecimalPlainSerializer());
        // 添加超出 JS 精度大数字处理
        addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
        // 时间
        addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_YMD_HMS)));
        addSerializer(new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_HMS)));
        addSerializer(new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_YMD)));
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_YMD_HMS)));
        addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_YMD)));
        addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_HMS)));
    }
}