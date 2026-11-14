package ext.library.core.backend;

/**
 * 已解析的存储后端
 * <p>
 * {@code AUTO} 不在其中——配置阶段选 {@code AUTO} 是为了"按类路径探测"，探测完成后必然落到
 * 这三个值之一。缓存、分布式锁、限流读的都是这个已解析结果，保证三处永远指向同一个中间件。
 *
 * @since 4.2.0
 */
public enum BackendType {

    /**
     * 进程内存，只适合单实例：缓存不共享，锁与限流只对本机生效
     */
    MEMORY,

    REDIS,

    POSTGRES

}
