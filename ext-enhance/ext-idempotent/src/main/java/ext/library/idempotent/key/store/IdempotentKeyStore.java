package ext.library.idempotent.key.store;

import java.time.Duration;

/**
 * <p>
 * 幂等 Key 存储
 * </p>
 * <p>
 * 消费过的幂等 key 记录下来，再下次消费前校验 key 是否已记录，从而拒绝执行
 *
 */
public interface IdempotentKeyStore {

    /**
     * 当不存在有效 key 时将其存储下来
     *
     * @param key      idempotentKey
     * @param duration key 的有效时长
     *
     * @return boolean true: 存储成功 false: 存储失败
     */
    boolean saveIfAbsent(String key, Duration duration);

    /**
     * 删除 key
     *
     * @param key idempotentKey
     */
    void remove(String key);

}