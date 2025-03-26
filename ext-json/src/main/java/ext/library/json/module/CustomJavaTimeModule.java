package ext.library.json.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import ext.library.tool.util.DateUtil;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自定义 java8 新增时间类型的序列化
 *
 * @see com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
 */
public class CustomJavaTimeModule extends SimpleModule {

	public CustomJavaTimeModule() {
		super(PackageVersion.VERSION);
        // ======================= 时间序列化规则 ===============================
		this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateUtil.FORMATTER_YMD_HMS));
		this.addSerializer(LocalDate.class, new LocalDateSerializer(DateUtil.FORMATTER_YMD));
		this.addSerializer(LocalTime.class, new LocalTimeSerializer(DateUtil.FORMATTER_HMS));
		this.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        // ======================= 时间反序列化规则 ==============================
		this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateUtil.FORMATTER_YMD_HMS));
		this.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateUtil.FORMATTER_YMD));
		this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateUtil.FORMATTER_HMS));
		this.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
	}

}
