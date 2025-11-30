package ext.library.redis.serialize;

import ext.library.redis.prefix.IRedisPrefixConverter;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

/**
 * 自定义 String Key 序列化工具，添加全局 key 前缀
 */
public class PrefixStringRedisSerializer extends StringRedisSerializer {

    private final IRedisPrefixConverter iRedisPrefixConverter;

    public PrefixStringRedisSerializer(IRedisPrefixConverter iRedisPrefixConverter) {
        super(StandardCharsets.UTF_8);
        this.iRedisPrefixConverter = iRedisPrefixConverter;
    }

    @Override
    public byte @NonNull [] serialize(String key) {
        byte[] originBytes = super.serialize(key);
        return this.iRedisPrefixConverter.wrap(originBytes);
    }

    @Override
    public String deserialize(byte[] bytes) {
        byte[] unwrap = this.iRedisPrefixConverter.unwrap(bytes);
        return super.deserialize(unwrap);
    }

}