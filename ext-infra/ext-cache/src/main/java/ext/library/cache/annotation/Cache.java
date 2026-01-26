package ext.library.cache.annotation;

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
 * 用于标记方法，表示该方法的结果需要被缓存。支持指定缓存名称、键、超时时间、时间单位和缓存类型。
 * <p>
 * 缓存类型包括：
 * <ul>
 *     <li>{@link CacheType#FULL} - 存取模式，先查缓存，未命中则执行方法并缓存结果（默认）</li>
 *     <li>{@link CacheType#PUT} - 强制更新模式，执行方法并强制更新缓存</li>
 *     <li>{@link CacheType#DELETE} - 删除模式，删除缓存后执行方法</li>
 * </ul>
 *
 * @since 2025.10.24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    /**
     * 缓存名称
     * <p>
     * 用于区分不同业务场景的缓存命名空间
     *
     * @return 缓存名称
     */
    String cacheName();

    /**
     * 缓存键
     * <p>
     * 支持 SpEL 表达式，如：{@code #id}、{@code #user.name}
     *
     * @return 缓存键
     */
    String key();

    /**
     * 超时时间
     * <p>
     * 缓存过期时间，配合 {@link #timeUnit()} 使用
     *
     * @return 超时时间，默认值为 120
     */
    long timeout() default 120;

    /**
     * 时间单位
     * <p>
     * 与 {@link #timeout()} 配合使用，指定超时时间的单位
     *
     * @return 时间单位，默认为 {@link TimeUnit#SECONDS}
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 缓存操作类型
     *
     * @return 缓存类型，默认为 {@link CacheType#FULL}
     */
    CacheType type() default CacheType.FULL;

}
