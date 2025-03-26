package ext.library.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * 空数组序列化处理器 如果 Array 为 null，则序列化为 []
 */
public class NullArrayJsonSerializer extends JsonSerializer<Object> {

	@Override
	public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
		if (value == null) {
			jsonGenerator.writeStartArray();
			jsonGenerator.writeEndArray();
		}
		else {
			jsonGenerator.writeObject(value);
		}
	}

}
