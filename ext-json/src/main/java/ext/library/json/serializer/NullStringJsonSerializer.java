package ext.library.json.serializer;

import ext.library.tool.constant.Symbol;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * jackson NULL 值序列化为 ""
 */
public class NullStringJsonSerializer extends ValueSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        gen.writeString(Symbol.EMPTY);

    }
}