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

/**
 * 扩展 Jackson 模块
 * <p>
 * 提供以下序列化/反序列化增强：
 * <ul>
 *     <li>BigDecimal 无科学计数法输出（保留 scale）</li>
 *     <li>Long/BigInteger 超出 JS 安全整数范围时转为字符串</li>
 *     <li>LocalDateTime/LocalDate/LocalTime 使用 {@link DateUtil} 标准格式</li>
 * </ul>
 * <p>
 * Instant/ZonedDateTime/OffsetDateTime 未单独注册序列化器，使用 Jackson java-time 模块的
 * 默认行为（ISO-8601 字符串）。
 */
public class ExtJacksonModule extends SimpleModule {

    public ExtJacksonModule() {
        super("extJacksonModule");
        // BigDecimal 输出为 JSON 数字（避免科学计数法）；Long/BigInteger 超出 JS 安全整数范围时转字符串
        addSerializer(BigDecimal.class, new BigDecimalPlainSerializer());
        addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
        // 时间：复用 DateUtil 的 Formatter 常量，避免重复编译
        addSerializer(new LocalDateTimeSerializer(DateUtil.FORMATTER_YMD_HMS));
        addSerializer(new LocalTimeSerializer(DateUtil.FORMATTER_HMS));
        addSerializer(new LocalDateSerializer(DateUtil.FORMATTER_YMD));
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateUtil.FORMATTER_YMD_HMS));
        addDeserializer(LocalDate.class, new LocalDateDeserializer(DateUtil.FORMATTER_YMD));
        addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateUtil.FORMATTER_HMS));
    }
}
