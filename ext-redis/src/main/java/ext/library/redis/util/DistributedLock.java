package ext.library.redis.util;

import ext.library.tool.constant.Symbol;
import ext.library.tool.core.Exceptions;
import ext.library.tool.core.Threads;
import ext.library.tool.util.INetUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 基于 Redis 的分布式锁 (线程内可重入)
 */
@Slf4j
public class DistributedLock implements Lock {

    /** 默认的锁超时时间 */
    private final static Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30L);
    /** 锁 key 前缀 */
    private final static String LOCK_PREFIX = "distributed_lock";
    /** 默认的获取锁超时时间 */
    private final static Duration DEFAULT_TRY_LOCK_TIMEOUT = Duration.ofSeconds(10L);
    /** 等待锁时，自旋尝试的周期，默认 10 毫秒 */
    private final static Duration DEFAULT_LOOP_INTERVAL = Duration.ofMillis(10L);
    /** 分布式锁过期时间 s 可以根据业务自己调节 */
    private static final Long LOCK_REDIS_TIMEOUT = 10L;
    /** 本机 host */
    private static final String CURRENT_HOST = INetUtil.getHostIp();
    /** 序列值，用于确保锁 value 的唯一性 */
    private static AtomicLong SERIAL_NUM;
    /** 最大序列值 */
    private static long MAX_SERIAL;
    /** 锁 Key */
    @Getter
    private final String lockKey;
    /** 锁超时时间 (单位毫秒) */
    @Getter
    private final Duration timeout;
    /** 等待锁时，自旋尝试的周期 (单位毫秒) */
    @Getter
    private final Duration loopInterval;
    /** 主机 + 线程 id */
    @Getter
    private final String hostThreadId;
    /** 锁定值 */
    @Getter
    private final String lockValue;
    /** 是否重入 */
    @Getter
    private boolean reentrant = false;
    /** 是否持有锁 */
    private boolean locked = false;


    public DistributedLock(String lockName) {
        this(lockName, DEFAULT_TIMEOUT, DEFAULT_LOOP_INTERVAL);
    }

    public DistributedLock(String lockName, Duration timeout) {
        this(lockName, timeout, DEFAULT_LOOP_INTERVAL);
    }

    public DistributedLock(String lockName, Duration timeout, Duration loopInterval) {
        if (lockName == null) {
            throw new IllegalArgumentException("lockName must assigned");
        }
        this.lockKey = LOCK_PREFIX + lockName;
        this.timeout = timeout;
        this.loopInterval = loopInterval;
        this.hostThreadId = CURRENT_HOST + Symbol.COLON + Thread.currentThread().threadId();
        this.lockValue = this.hostThreadId + Symbol.COLON + getNextSerial();
    }

    /**
     * @return 下一个序列值
     */
    private static synchronized long getNextSerial() {
        long serial = SERIAL_NUM.incrementAndGet();
        if (serial > MAX_SERIAL) {
            serial = serial - MAX_SERIAL;
            SERIAL_NUM.set(serial);
        }
        return serial;
    }

    private static DefaultRedisScript<Boolean> getBooleanDefaultRedisScript() {
        String script = "local val,ttl=ARGV[1],ARGV[2] if redis.call('EXISTS', KEYS[1])==1 then local curValue = redis.call('GET', KEYS[1]) if string.find(curValue, val)==1 then local curTtl = redis.call('TTL', KEYS[1]) redis.call('EXPIRE', KEYS[1], curTtl + ttl) return true else return false end else return false end";
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Boolean.class);
        redisScript.setScriptText(script);
        return redisScript;
    }

    /**
     * 获取锁，如果锁被持有，将一直等待，直到超出默认的的 DEFAULT_TRY_LOCK_TIMEOUT
     */
    @Override
    public void lock() {
        try {
            if (!tryLock(DEFAULT_TRY_LOCK_TIMEOUT)) {
                throw Exceptions.throwOut("try lock timeout, lockKey: {}", lockKey);
            }
        } catch (InterruptedException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 尝试获取锁，如果锁被持有，则等待相应的时间 (等待锁时可被中断)
     *
     * @throws InterruptedException 被中断等待
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        if (!tryLock(DEFAULT_TRY_LOCK_TIMEOUT, true)) {
            throw Exceptions.throwOut("try lock timeout, lockKey: {}", this.lockKey);
        }
    }

    /**
     * 尝试获取锁，只会立即获取一次，如果锁被占用，则返回 false, 获取成功则返回 true
     *
     * @return 是否成功获取锁
     */
    @Override
    public boolean tryLock() {
        try {
            Boolean success = setIfAbsent(lockKey, lockValue, timeout);
            if (success != null && success) {
                locked = true;
                log.debug("Lock success, lockKey: {}, lockValue: {}", lockKey, lockValue);
                return true;
            } else {
                // 如果持有锁的是当前线程，则重入
                // language=Redis
                DefaultRedisScript<Boolean> redisScript = getBooleanDefaultRedisScript();
                success = RedisUtil.execute(redisScript, List.of(lockKey), hostThreadId, String.valueOf(Math.max(timeout.toSeconds(), 1)));
                if (success != null && success) {
                    this.reentrant = true;
                    locked = true;
                    log.debug("Lock reentrant success, lockKey: {}, lockValue: {}", lockKey, lockValue);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("tryLock error, do unlock, lockKey: {}, lockValue: {}", lockKey, lockValue, e);
            unlock();
        }
        return false;
    }

    /**
     * 使用 lua 脚本的方式实现 setIfAbsent, 因为当业务应用使用了 redisson 时，直接使用 template 的 setIfAbsent 返回值为 null
     *
     * @param key     key
     * @param value   值
     * @param timeout 超时时间
     *
     * @return 是否成功设值
     */
    private Boolean setIfAbsent(String key, String value, Duration timeout) {
        // language=Redis
        String script = "local val,ttl=ARGV[1],ARGV[2] if redis.call('EXISTS', KEYS[1])==1 then return false else redis.call('SET', KEYS[1], ARGV[1]) redis.call('EXPIRE', KEYS[1], ARGV[2]) return true end";
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Boolean.class);
        redisScript.setScriptText(script);
        return RedisUtil.execute(redisScript, List.of(key), value, timeout.toSeconds());
    }

    /**
     * 尝试获取锁，如果锁被占用，则持续尝试获取，直到超过指定的 time 时间
     *
     * @param time 等待锁的时间
     *
     * @return 是否成功获取锁
     *
     * @throws InterruptedException 被中断
     */
    public boolean tryLock(Duration time) throws InterruptedException {
        return tryLock(time.toMillis(), TimeUnit.MILLISECONDS, false);
    }

    /**
     * 尝试获取锁，如果锁被占用，则持续尝试获取，直到超过指定的 time 时间
     *
     * @param time 等待锁的时间
     * @param unit time 的单位
     *
     * @return 是否成功获取锁
     *
     * @throws InterruptedException 被中断
     */
    @Override
    public boolean tryLock(long time, @NonNull TimeUnit unit) throws InterruptedException {
        return tryLock(time, unit, false);
    }

    /**
     * 尝试获取锁，如果锁被占用，则持续尝试获取，直到超过指定的 time 时间
     *
     * @param time          等待锁的时间
     * @param interruptibly 等待是否可被中断
     *
     * @return 是否成功获取锁
     *
     * @throws InterruptedException 被中断
     */
    private boolean tryLock(Duration time, boolean interruptibly) {
        long millis = time.toMillis();
        long current = System.currentTimeMillis();
        do {
            if (interruptibly && Thread.interrupted()) {
                throw Exceptions.throwOut("tryLock interrupted");
            }
            if (tryLock()) {
                return true;
            }
            Threads.sleep(loopInterval);
        } while (System.currentTimeMillis() - current < millis);
        return false;
    }

    /**
     * 尝试获取锁，如果锁被占用，则持续尝试获取，直到超过指定的 time 时间
     *
     * @param time          等待锁的时间
     * @param unit          time 的单位
     * @param interruptibly 等待是否可被中断
     *
     * @return 是否成功获取锁
     *
     * @throws InterruptedException 被中断
     */
    private boolean tryLock(long time, TimeUnit unit, boolean interruptibly) {
        long millis = unit.convert(time, TimeUnit.MILLISECONDS);
        long current = System.currentTimeMillis();
        do {
            if (interruptibly && Thread.interrupted()) {
                throw Exceptions.throwOut("tryLock interrupted");
            }
            if (tryLock()) {
                return true;
            }
            Threads.sleep(loopInterval);
        } while (System.currentTimeMillis() - current < millis);
        return false;
    }

    /**
     * 释放锁
     */
    @Override
    public void unlock() {
        try {
            if (!locked) {
                return;
            }
            if (this.reentrant) {
                log.debug("Unlock reentrant success, lockKey: {}, lockValue: {}", this.lockKey, this.lockValue);
                return;
            }
            // 使用 lua 脚本处理锁判断和释放
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) return true else return false end";
            DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
            redisScript.setResultType(Boolean.class);
            redisScript.setScriptText(script);
            Boolean res = RedisUtil.execute(redisScript, Collections.singletonList(this.lockKey), this.lockValue);
            if (res != null && res) {
                locked = false;
                log.debug("Unlock success, lockKey: {}, lockValue: {}", this.lockKey, this.lockValue);
                return;
            }
        } catch (Exception e) {
            log.error("Unlock error", e);
        }
        log.warn("Unlock failed, lockKey: {}, lockValue: {}", this.lockKey, this.lockValue);
    }

    @Override
    @NonNull
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}