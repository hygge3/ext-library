package ext.library.json.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import ext.library.json.serializer.BigDecimalPlainSerializer;
import ext.library.json.serializer.BigNumberSerializer;
import ext.library.tool.util.DateUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 自定义 Jackson 模块
 * <p>
 * 该模块扩展了 Jackson 的 SimpleModule，用于自定义 JSON 序列化和反序列化行为。
 * 主要功能包括：
 * <ul>
 *   <li>自定义 BigDecimal 序列化格式（避免科学计数法）</li>
 *   <li>处理超出 JavaScript 精度的大数字类型（Long, BigInteger）</li>
 *   <li>统一日期时间类型的序列化/反序列化格式</li>
 * </ul>
 * <p>
 * 使用方式：
 * <pre>
 * ObjectMapper mapper = new ObjectMapper();
 * mapper.registerModule(new CustomModule());
 * </pre>
 *
 * @see SimpleModule
 * @see BigDecimalPlainSerializer
 * @see BigNumberSerializer
 */
public class CustomModule extends SimpleModule {

    /**
     * 构造函数，初始化自定义序列化器和反序列化器
     * <p>
     * 注册了以下自定义序列化器和反序列化器：
     * <ul>
     *   <li>BigDecimal: 使用 BigDecimalPlainSerializer 避免科学计数法</li>
     *   <li>Long/BigInteger: 使用 BigNumberSerializer 处理大数字精度问题</li>
     *   <li>LocalDateTime: 使用 yyyy-MM-dd HH:mm:ss 格式</li>
     *   <li>LocalDate: 使用 yyyy-MM-dd 格式</li>
     *   <li>LocalTime: 使用 HH:mm:ss 格式</li>
     * </ul>
     */
    public CustomModule() {
        super("CustomModule");

        // BigDecimal 处理 - 避免使用科学计数法，保持原始精度
        addSerializer(BigDecimal.class, new BigDecimalPlainSerializer());

        // 添加超出 JS 精度大数字处理 - 解决 JavaScript 数字精度限制问题
        // JavaScript Number 类型最大安全整数为 2^53-1，超过此精度会丢失精度
        addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);

        // 时间类型序列化器 - 统一日期时间格式
        addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_YMD_HMS)));
        addSerializer(new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_HMS)));
        addSerializer(new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_YMD)));

        // 时间类型反序列化器 - 与序列化器格式保持一致
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_YMD_HMS)));
        addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_YMD)));
        addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateUtil.STRING_FORMATTER_HMS)));
    }
}