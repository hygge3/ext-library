package ext.library.redis.serialize;

import java.nio.charset.StandardCharsets;

import ext.library.redis.prefix.IRedisPrefixConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 自定义 String Key 序列化工具，添加全局 key 前缀
 */
@Slf4j
public class PrefixStringRedisSerializer extends StringRedisSerializer {

    private final IRedisPrefixConverter iRedisPrefixConverter;

    public PrefixStringRedisSerializer(IRedisPrefixConverter iRedisPrefixConverter) {
        super(StandardCharsets.UTF_8);
        this.iRedisPrefixConverter = iRedisPrefixConverter;
    }

    @Override
    public String deserialize(byte[] bytes) {
        byte[] unwrap = this.iRedisPrefixConverter.unwrap(bytes);
        return super.deserialize(unwrap);
    }

    @Override
    public byte[] serialize(String key) {
        byte[] originBytes = super.serialize(key);
        return this.iRedisPrefixConverter.wrap(originBytes);
    }

}
