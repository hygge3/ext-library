package ext.library.redis.prefix.impl;

import ext.library.redis.prefix.IRedisPrefixConverter;
import org.jetbrains.annotations.Contract;

/**
 * redis key 前缀默认转换器
 */
public record DefaultRedisPrefixConverter(String prefix) implements IRedisPrefixConverter {

    @Override
    @Contract(pure = true)
    public boolean match() {
        return true;
    }

}
