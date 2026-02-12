package ext.library.redis.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import ext.library.tool.runtime.Threads;
import ext.library.tool.util.NetUtil;
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
public final class DistributedLock implements Lock, AutoCloseable {
    /** 默认的锁超时时间 */
    private static final Duration defaultTimeout = Duration.ofSeconds(30L);
    /** 锁 key 前缀 */
    private static final String lockPrefix = "distributed_lock";
    /** 默认的获取锁超时时间 */
    private static final Duration defaultTryLockTimeout = Duration.ofSeconds(10L);
    /** 等待锁时，自旋尝试的周期，默认 10 毫秒 */
    private static final Duration defaultLoopInterval = Duration.ofMillis(10L);
    /** 本机 host */
    private static final String currentHost = NetUtil.getHostIp();
    /** 序列值，用于确保锁 value 的唯一性 */
    private static final AtomicLong serialNum = new AtomicLong(0);
    /** 最大序列值，预留一定空间防止溢出 */
    private static final long maxSerial = Long.MAX_VALUE - 1_000_000;
    /** 锁 Key */
    private final String lockKey;
    /** 锁超时时间 (单位毫秒) */
    private final Duration timeout;
    /** 等待锁时，自旋尝试的周期 (单位毫秒) */
    private final Duration loopInterval;
    /** 主机 + 线程 id */
    private final String hostThreadId;
    /** 锁定值 */
    private final String lockValue;
    /** 是否重入 */
    private boolean reentrant = false;
    /** 是否持有锁 */
    private boolean locked = false;


    public DistributedLock(String lockName) {
        this(lockName, defaultTimeout, defaultLoopInterval);
    }

    public DistributedLock(String lockName, Duration timeout) {
        this(lockName, timeout, defaultLoopInterval);
    }

    public DistributedLock(String lockName, Duration timeout, Duration loopInterval) {
        if (lockName == null) {
            throw new ExtException(EmojiSymbol.LOCK, "lockName 必须分配");
        }
        this.lockKey = lockPrefix + lockName;
        this.timeout = timeout;
        this.loopInterval = loopInterval;
        this.hostThreadId = currentHost + ":" + Thread.currentThread().threadId();
        this.lockValue = this.hostThreadId + ":" + getNextSerial();
    }

    /**
     * @return 下一个序列值
     */
    private static synchronized long getNextSerial() {
        long serial = serialNum.incrementAndGet();
        if (serial > maxSerial) {
            serial = serial - maxSerial;
            serialNum.set(serial);
        }
        return serial;
    }

    private static DefaultRedisScript<Boolean> getBooleanDefaultRedisScript() {
        // 使用文本块提高可读性
        // language=redis
        String script = """
                -- 获取传入的参数
                local val = ARGV[1]
                local ttl = tonumber(ARGV[2]) -- 确保是数字
                -- 检查键是否存在
                if redis.call('EXISTS', KEYS[1]) == 1 then
                    -- 获取当前值
                    local curValue = redis.call('GET', KEYS[1])
                    -- 修改点 1: 使用精确匹配，而不是前缀查找
                    if curValue == val then
                        -- 获取当前 TTL
                        local curTtl = redis.call('TTL', KEYS[1])
                        -- 修改点 2: 严谨处理 TTL 返回值
                        if curTtl == -1 then
                            -- 如果键是永久有效的，直接设置新的 TTL
                            redis.call('EXPIRE', KEYS[1], ttl)
                        else
                            -- 如果键已经有 TTL，则延长
                            redis.call('EXPIRE', KEYS[1], curTtl + ttl)
                        end
                        return true
                    else
                        return false
                    end
                else
                    -- 键不存在
                    return false
                end
                """;
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
            if (!tryLock(defaultTryLockTimeout)) {
                throw new ExtException(EmojiSymbol.LOCK, "尝试加锁超时，key:{}", lockKey);
            }
        } catch (InterruptedException e) {
            throw new ExtException(EmojiSymbol.LOCK, "获取锁失败，请先解锁，lockKey:{}, lockValue:{}", lockKey, lockValue);
        }
    }

    /**
     * 尝试获取锁，如果锁被持有，则等待相应的时间 (等待锁时可被中断)
     *
     * @throws InterruptedException 被中断等待
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        if (!tryLock(defaultTryLockTimeout, true)) {
            throw new ExtException(EmojiSymbol.LOCK, "尝试加锁超时，key:{}", this.lockKey);
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
                Logs.debug(EmojiSymbol.LOCK, "加锁成功，lockKey: {}, lockValue: {}", lockKey, lockValue);
                return true;
            } else {
                // 如果持有锁的是当前线程，则重入
                // language=Redis
                DefaultRedisScript<Boolean> redisScript = getBooleanDefaultRedisScript();
                success = RedisUtil.execute(redisScript, List.of(lockKey), hostThreadId, String.valueOf(Math.max(timeout.toSeconds(), 1)));
                if (success != null && success) {
                    this.reentrant = true;
                    locked = true;
                    Logs.debug(EmojiSymbol.LOCK, "锁重入成功，lockKey: {}, lockValue: {}", lockKey, lockValue);
                    return true;
                }
            }
        } catch (Exception e) {
            Logs.error(EmojiSymbol.LOCK, e, "尝试加锁错误，请先解锁，lockKey: {}, lockValue: {}", lockKey, lockValue);
            unlock();
        }
        return false;
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
     * 释放锁
     */
    @Override
    public void unlock() {
        try {
            if (!locked) {
                return;
            }
            if (this.reentrant) {
                Logs.debug(EmojiSymbol.REDIS, "解锁重入成功，lockKey: {}, lockValue: {}", lockKey, lockValue);
                return;
            }
            // 使用 lua 脚本处理锁判断和释放
            // language=Redis
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) return true else return false end";
            DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
            redisScript.setResultType(Boolean.class);
            redisScript.setScriptText(script);
            Boolean res = RedisUtil.execute(redisScript, Collections.singletonList(this.lockKey), this.lockValue);
            if (res != null && res) {
                locked = false;
                Logs.debug(EmojiSymbol.LOCK, "解锁成功，lockKey: {}, lockValue: {}", lockKey, lockValue);
                return;
            }
        } catch (Exception e) {
            Logs.error(EmojiSymbol.LOCK, e, "解锁错误");
        }
        Logs.warn(EmojiSymbol.LOCK, "解锁失败，lockKey: {}, lockValue: {}", this.lockKey, this.lockValue);
    }

    @Override
    @NonNull
    public Condition newCondition() {
        throw new UnsupportedOperationException();
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
        return RedisUtil.execute(redisScript, List.of(key), value, String.valueOf(timeout.toSeconds()));
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
                throw new ExtException(EmojiSymbol.REDIS, "尝试加锁定中断");
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
                throw new ExtException(EmojiSymbol.REDIS, "尝试加锁中断");
            }
            if (tryLock()) {
                return true;
            }
            Threads.sleep(loopInterval);
        } while (System.currentTimeMillis() - current < millis);
        return false;
    }

    // Getter methods
    public String getLockKey() {
        return lockKey;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Duration getLoopInterval() {
        return loopInterval;
    }

    public String getHostThreadId() {
        return hostThreadId;
    }

    public String getLockValue() {
        return lockValue;
    }

    public boolean isReentrant() {
        return reentrant;
    }

    public boolean isLocked() {
        return locked;
    }

    /**
     * 实现 AutoCloseable，支持 try-with-resources 语法
     * <p>
     * 示例：
     * <pre>{@code
     * try (DistributedLock lock = new DistributedLock("myLock")) {
     *     lock.lock();
     *     // 执行业务逻辑
     * }
     * }</pre>
     */
    @Override
    public void close() {
        unlock();
    }

}
