package ext.library.ratelimit.store;

import ext.library.ratelimit.RateLimitResult;

import java.time.Duration;

/**
 * 限流存储 SPI
 * <p>
 * 抽象「按时间窗口计数」这一件事，具体实现可以是 Redis、PostgreSQL 或纯内存。
 */
public interface RateLimitStore {

    /**
     * 原子地检查并递增计数
     *
     * @param key    限流 key
     * @param limit  阈值
     * @param window 时间窗口
     * @return 限流结果
     */
    RateLimitResult increment(String key, int limit, Duration window);

    /**
     * 重置指定 key 的计数
     *
     * @param key 限流 key
     * @return 是否命中并清除了记录
     */
    boolean reset(String key);

    /**
     * 存储类型标识，用于自动装配时选择实现
     */
    enum Type {
        /**
         * 自动检测，由自动装配类解析为具体实现；不会出现在 SPI 实现上
         */
        AUTO,
        /** 纯内存，仅单实例有效 */
        MEMORY,
        /** Redis */
        REDIS,
        /** PostgreSQL */
        POSTGRES
    }

    /**
     * @return 存储类型
     */
    Type getType();

}
