package ext.library.redis.prefix;

import ext.library.redis.properties.RedisProperties;
import ext.library.tool.util.StringUtil;

/**
 * redis key 前缀默认转换器
 */
public class DefaultRedisPrefixConverter implements RedisPrefixConverter {
    private final RedisProperties redisProperties;

    public DefaultRedisPrefixConverter(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Override
    public String prefix() {
        return redisProperties.getKeyPrefix();
    }

    @Override
    public boolean enabled() {
        return StringUtil.isNotEmpty(redisProperties.getKeyPrefix());
    }

}
