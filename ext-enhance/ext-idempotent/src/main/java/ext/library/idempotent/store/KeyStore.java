package ext.library.idempotent.store;

import java.time.Duration;

/**
 * 幂等 Key 存储接口
 * <p>
 * 负责存储和管理已消费的幂等 key，用于在下次请求前校验 key 是否已存在，从而拒绝重复执行。
 * <p>
 * 内置实现：
 * <ul>
 *   <li>{@link MemoryKeyStore} - 基于内存（Caffeine）的存储</li>
 *   <li>{@link RedisKeyStore} - 基于 Redis 的分布式存储</li>
 *   <li>{@link PostgresKeyStore} - 基于 PostgreSQL 的存储</li>
 * </ul>
 */
public interface KeyStore {

    /**
     * 当不存在有效 key 时将其存储下来
     *
     * @param key      幂等 key
     * @param duration key 的有效时长
     *
     * @return true: 存储成功（key 不存在） false: 存储失败（key 已存在）
     */
    boolean saveIfAbsent(String key, Duration duration);

    /**
     * 删除指定的幂等 key
     *
     * @param key 幂等 key
     */
    void remove(String key);

}
