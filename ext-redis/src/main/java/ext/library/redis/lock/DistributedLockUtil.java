package ext.library.redis.lock;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import ext.library.redis.util.RedisUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * 分布式锁业务接口
 */
@Slf4j
@UtilityClass
public class DistributedLockUtil {

    /**
     * 获取锁，默认 30 秒失效，失败一直等待直到获取锁
     *
     * @param key 锁的 key
     * @return 锁对象
     */
    public Boolean lock(String key) {
        return getLock(key, LOCK_VALUE);
    }

    /**
     * 获取锁，失败一直等待直到获取锁
     *
     * @param key      锁的 key
     * @param lockTime 加锁的时间，超过这个时间后锁便自动解锁；如果 lockTime 为 -1，则保持锁定直到显式解锁
     * @param unit     {@code lockTime} 参数的时间单位
     * @return 锁对象
     */
    public Boolean lock(String key, long lockTime, TimeUnit unit) {
        return RedisUtil.setNxEx(key, LOCK_VALUE, lockTime, unit);
    }

    /**
     * 解锁
     *
     * @param key 锁
     */
    public void unLock(String key) {
        releaseLock(key, LOCK_VALUE);
    }

    /** 分布式锁过期时间 s 可以根据业务自己调节 */
    private static final Long LOCK_REDIS_TIMEOUT = 10L;

    /** 分布式锁休眠 至 再次尝试获取 的等待时间 ms 可以根据业务自己调节 */
    public static final Long LOCK_REDIS_WAIT = 500L;

    /** 分布式锁休眠 至 再次尝试获取 的等待时间 ms 可以根据业务自己调节 */
    public static final String LOCK_VALUE = "lock";

    /**
     * 加锁
     **/
    public Boolean getLock(String key, String value) {
        return RedisUtil.setNxEx(key, value, LOCK_REDIS_TIMEOUT);
    }

    /**
     * 释放锁
     **/
    public void releaseLock(String key, String value) {
        @Language("Redis") String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        RedisUtil.execute(redisScript, Collections.singletonList(key), value);
    }

}
