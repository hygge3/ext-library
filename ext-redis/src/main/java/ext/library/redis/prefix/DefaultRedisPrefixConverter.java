package ext.library.redis.prefix;

import ext.library.redis.config.properties.RedisProperties;
import ext.library.tool.util.StringUtil;
import lombok.RequiredArgsConstructor;

/**
 * redis key 前缀默认转换器
 */
@RequiredArgsConstructor
public class DefaultRedisPrefixConverter implements IRedisPrefixConverter {
    private final RedisProperties redisProperties;

    @Override
    public String prefix() {
        return redisProperties.getKeyPrefix();
    }

    @Override
    public boolean match() {
        return StringUtil.isNotEmpty(redisProperties.getKeyPrefix());
    }

}