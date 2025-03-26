package ext.library.json.serializer;

import jakarta.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ext.library.tool.constant.Symbol;
import java.io.IOException;

/**
 * jackson NULL 值序列化为 ""
 */
public class NullStringJsonSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, @Nonnull JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeString(Symbol.EMPTY);
    }

}
