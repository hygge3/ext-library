package ext.library.cache.annotion;

import ext.library.cache.core.CacheType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface L2Cache {
    String cacheName() default "cache";

    String key(); // 支持 springEl 表达式

    long timeOut() default 120;

    CacheType type() default CacheType.FULL;


}