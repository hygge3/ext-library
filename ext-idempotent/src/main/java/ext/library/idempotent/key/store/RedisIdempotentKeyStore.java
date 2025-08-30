package ext.library.idempotent.key.store;

import ext.library.redis.util.RedisUtil;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * 基于 Redis 的幂等 Key 的存储器
 */
@RequiredArgsConstructor
public class RedisIdempotentKeyStore implements IdempotentKeyStore {

    @Override
    public boolean saveIfAbsent(String key, Duration duration) {
        return RedisUtil.setNxEx(key, String.valueOf(System.currentTimeMillis()), duration);
    }

    @Override
    public void remove(String key) {
        RedisUtil.del(key);
    }

}