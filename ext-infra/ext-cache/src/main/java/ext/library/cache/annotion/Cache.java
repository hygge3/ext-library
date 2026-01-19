package ext.library.cache.annotion;

import ext.library.cache.enums.CacheType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解
 * <p>
 * 用于标记方法，表示该方法的结果需要被缓存。支持指定缓存名称，键，超时时间，时间单位和缓存类型。
 * <p>
 * 缓存类型包括 FULL(全缓存) 和 PARTIAL(部分缓存), 默认为 FULL.
 *
 * @date 2025.10.24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    /**
     * 获取缓存名称
     * <p>
     * 返回当前操作所对应的缓存名称
     *
     * @return 缓存名称
     */
    String cacheName();

    /**
     * 获取键值
     * <p>
     * 返回一个键值字符串，具体实现由子类定义
     *
     * @return 键值
     */
    String key(); // 支持 springEl 表达式

    /**
     * 获取超时时间
     * <p>
     * 返回默认的超时时间值，单位为秒
     *
     * @return 超时时间，单位为秒，默认值为 120
     */
    long timeout() default 120;

    /**
     * 获取默认的时间单位
     * <p>
     * 返回一个默认的时间单位，该时间单位为 {@link TimeUnit#SECONDS}.
     *
     * @return 默认的时间单位
     *
     * @since 1.0
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 获取缓存类型
     * <p>
     * 返回当前配置的缓存类型，默认为 CacheType.FULL
     *
     * @return 缓存类型
     */
    CacheType type() default CacheType.FULL;

}