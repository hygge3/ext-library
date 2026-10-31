package ext.library.json.serializer;


import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.math.BigDecimal;

/**
 * BigDecimal 无科学计数法序列化
 * <p>
 * 输出为 JSON 数字（而非字符串），使用 {@link BigDecimal#toPlainString()} 避免科学计数法。
 * 不做 {@code stripTrailingZeros()}，保留原始 scale（金额场景 scale 有意义）。
 */
public class BigDecimalPlainSerializer extends ValueSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        // toPlainString() 避免科学计数法；不做 stripTrailingZeros()，保留原始 scale（金额场景 scale 有意义）
        gen.writeNumber(value.toPlainString());
    }
}
