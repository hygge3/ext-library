package ext.library.redis.serialize;

import ext.library.redis.prefix.RedisPrefixConverter;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

/**
 * 自定义 JDK Key 序列化工具，添加全局 key 前缀
 */
public class PrefixJdkRedisSerializer extends JdkSerializationRedisSerializer {

    private final RedisPrefixConverter prefixConverter;

    public PrefixJdkRedisSerializer(RedisPrefixConverter prefixConverter) {
        this.prefixConverter = prefixConverter;
    }

    @Override
    public byte @NonNull [] serialize(Object object) {
        byte[] originBytes = super.serialize(object);
        return this.prefixConverter.wrap(originBytes);
    }

    @Override
    public Object deserialize(byte[] bytes) {
        byte[] unwrap = this.prefixConverter.unwrap(bytes);
        return super.deserialize(unwrap);
    }
}
