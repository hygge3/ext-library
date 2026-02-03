package ext.library.tool.constant;

import ext.library.tool.domain.SnowflakeId;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 常用单例对象持有器
 * <p>
 * 提供线程安全的共享实例，避免重复创建开销
 */
public final class Singletons {

    /**
     * 普通随机数生成器
     * <p>
     * 注意：非线程安全，高并发场景建议使用 {@link ThreadLocalRandom}
     */
    public static final Random RANDOM = new Random();
    /**
     * 安全随机数生成器
     * <p>
     * 线程安全，适用于密码学场景
     */
    public static final SecureRandom SECURE_RANDOM = new SecureRandom();
    /**
     * 雪花 ID 生成器（默认实例）
     * <p>
     * workerId=0, datacenterId=0，适用于单机环境
     * <p>
     * 分布式环境建议自行创建 {@link SnowflakeId} 实例并配置合适的 ID
     */
    public static final SnowflakeId SNOWFLAKE_ID = new SnowflakeId(0, 0);

    private Singletons() {
        // 防止实例化
    }

}
