package ext.library.lock.enums;

/**
 * 锁存储后端类型
 * <p>
 * 配置项：{@code ext.lock.store-type}
 *
 * @since 4.0.0
 */
public enum LockStoreType {

    /**
     * 自动检测：按类路径优先 Redis，其次 PostgreSQL；一个都没有则装配阶段直接报错
     */
    AUTO,

    /**
     * Redis 后端（锁自带 TTL，进程崩溃后自动过期）
     */
    REDIS,

    /**
     * PostgreSQL 咨询锁后端（会话级锁，随连接释放，不依赖 Redis）
     */
    POSTGRES

}
