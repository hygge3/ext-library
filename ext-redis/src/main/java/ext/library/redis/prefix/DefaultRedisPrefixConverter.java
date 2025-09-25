package ext.library.redis.prefix;

import ext.library.redis.config.properties.RedisProperties;
import ext.library.tool.util.StringUtil;

/**
 * redis key 前缀默认转换器
 */
public class DefaultRedisPrefixConverter implements IRedisPrefixConverter {
    private final RedisProperties redisProperties;

    public DefaultRedisPrefixConverter(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Override
    public String prefix() {
        return redisProperties.getKeyPrefix();
    }

    @Override
    public boolean match() {
        return StringUtil.isNotEmpty(redisProperties.getKeyPrefix());
    }

}