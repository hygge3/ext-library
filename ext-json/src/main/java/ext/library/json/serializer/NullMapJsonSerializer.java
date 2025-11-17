package ext.library.json.serializer;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 空 Map 序列化处理器 Map 为 null，则序列化为 {}
 *
 */
public class NullMapJsonSerializer extends ValueSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeStartObject().writeEndObject();
        } else {
            gen.writeStartObject(value).writeEndObject();
        }
    }
}