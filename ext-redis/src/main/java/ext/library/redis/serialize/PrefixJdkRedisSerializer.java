package ext.library.redis.serialize;

import ext.library.redis.prefix.IRedisPrefixConverter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

/**
 * 自定义 JDK Key 序列化工具，添加全局 key 前缀
 */
@Slf4j
public class PrefixJdkRedisSerializer extends JdkSerializationRedisSerializer {

	private final IRedisPrefixConverter redisPrefixConverter;

	public PrefixJdkRedisSerializer(IRedisPrefixConverter redisPrefixConverter) {
		this.redisPrefixConverter = redisPrefixConverter;
	}

	@Override
	@NotNull
	public Object deserialize(byte[] bytes) {
		byte[] unwrap = this.redisPrefixConverter.unwrap(bytes);
		return super.deserialize(unwrap);
	}

	@Override
    public byte @NotNull [] serialize(Object object) {
		byte[] originBytes = super.serialize(object);
		return this.redisPrefixConverter.wrap(originBytes);
	}

}
