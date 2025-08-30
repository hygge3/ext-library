package ext.library.cache.annotion;

import ext.library.cache.enums.CacheType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    String cacheName();

    String key(); // 支持 springEl 表达式

    long timeout() default 120;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    CacheType type() default CacheType.FULL;

}