package ext.library.redis.serialize;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.ObjectMapper;
import ext.library.redis.serialize.CacheSerializer;
import lombok.RequiredArgsConstructor;

/**
 * Jackson 序列化器
 */
@RequiredArgsConstructor
public class JacksonSerializer implements ext.library.redis.serialize.CacheSerializer {

	private final ObjectMapper objectMapper;

	/**
	 * 反序列化方法
	 * @param cacheData 缓存中的数据
	 * @param type 反序列化目标类型
	 * @return 反序列化后的对象
	 * @throws IOException IO 异常
	 */
	@Override
	public Object deserialize(String cacheData, Type type) throws IOException {
		return this.objectMapper.readValue(cacheData, CacheSerializer.getJavaType(type));
	}

	/**
	 * 序列化方法
	 * @param cacheData 待缓存的数据
	 * @return 序列化后的数据
	 * @throws IOException IO 异常
	 */
	@Override
	public String serialize(Object cacheData) throws IOException {
		return this.objectMapper.writeValueAsString(cacheData);
	}

}
