package ext.library.json.serializer;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 空数组序列化处理器 如果 Array 为 null，则序列化为 []
 */
public class NullArrayJsonSerializer extends ValueSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeStartArray().writeEndArray();
        } else {
            gen.writeStartArray(value).writeEndArray();
        }
    }
}