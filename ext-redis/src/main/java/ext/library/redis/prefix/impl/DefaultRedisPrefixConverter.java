package ext.library.redis.prefix.impl;

import ext.library.redis.prefix.IRedisPrefixConverter;

/**
 * redis key 前缀默认转换器
 */
public record DefaultRedisPrefixConverter(String prefix) implements IRedisPrefixConverter {

    @Override
    public boolean match() {
        return true;
    }

}
